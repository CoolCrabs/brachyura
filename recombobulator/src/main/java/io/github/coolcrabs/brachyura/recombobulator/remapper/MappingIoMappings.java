package io.github.coolcrabs.brachyura.recombobulator.remapper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;

import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;

import io.github.coolcrabs.brachyura.recombobulator.AccessFlags;
import io.github.coolcrabs.brachyura.recombobulator.Mutf8Slice;
import io.github.coolcrabs.brachyura.recombobulator.remapper.InheritanceMap.InheritanceGroup;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.fabricmc.mappingio.tree.MappingTree;
import net.fabricmc.mappingio.tree.MappingTree.ClassMapping;
import net.fabricmc.mappingio.tree.MappingTree.FieldMapping;
import net.fabricmc.mappingio.tree.MappingTree.MethodArgMapping;
import net.fabricmc.mappingio.tree.MappingTree.MethodMapping;

public class MappingIoMappings implements Mappings {
    public static final boolean LOG_PARAM_CONFLICS = Boolean.getBoolean("recombobulator.logparamconflicts");

    private final Map<Mutf8Slice, Mutf8Slice> classMap;
    final Int2ObjectOpenHashMap<NameDescPair> methodMap;
    final Int2ObjectOpenHashMap<Int2ObjectOpenHashMap<Mutf8Slice>> methodArgMap;
    private final InheritanceMap inheritanceMap;
    private final ConcurrentHashMap<Mutf8Slice, HashMap<NameDescPair, NameDescPair>> fieldMap;

    public MappingIoMappings(MappingTree tree, int src, int dst, InheritanceMap inheritanceMap) {
        this.inheritanceMap = inheritanceMap;
        ExecutorService threadpool = ForkJoinPool.commonPool();
        Collection<? extends ClassMapping> classes = tree.getClasses();
        classMap = new HashMap<>(classes.size());
        fieldMap = new ConcurrentHashMap<>(classes.size());
        ArrayList<Future<?>> futures = new ArrayList<>(classes.size());
        ArrayList<Runnable> runs = new ArrayList<>(classes.size());
        for (ClassMapping c : classes) {
            String srcn = c.getName(src);
            String dstn = c.getName(dst);
            if (srcn == null) continue;
            Mutf8Slice ssrcn = new Mutf8Slice(srcn);
            if (dstn != null) classMap.put(ssrcn, new Mutf8Slice(dstn));
            runs.add(() -> {
                Collection<? extends FieldMapping> fmaps = c.getFields();
                HashMap<NameDescPair, NameDescPair> m = new HashMap<>(fmaps.size());
                for (FieldMapping fmap : fmaps) {
                    String fsrcn = fmap.getName(src);
                    String fsrcd = fmap.getDesc(src);
                    String fdstn = fmap.getName(dst);
                    if (fsrcn != null && fsrcd != null) {
                        Mutf8Slice sfsrcn = new Mutf8Slice(fsrcn);
                        Mutf8Slice sfdstn = fdstn == null ? sfsrcn : new Mutf8Slice(fdstn);
                        Mutf8Slice sfsrcd = new Mutf8Slice(fsrcd);
                        m.put(new NameDescPair(sfsrcn, sfsrcd), new NameDescPair(sfdstn, Mappings.remapFieldDescriptor(this, sfsrcd)));
                    }
                }
                fieldMap.put(ssrcn, m);
            });
        }
        for (Runnable r : runs) futures.add(threadpool.submit(r));
        for (Future<?> f : futures) {
            try {
                f.get();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        }
        class IntNameDescPairPair {
            int key;
            NameDescPair value;
            Int2ObjectOpenHashMap<Mutf8Slice> params;
        }
        int size = inheritanceMap.curId;
        methodMap = new Int2ObjectOpenHashMap<>(size);
        methodArgMap = new Int2ObjectOpenHashMap<>(size);
        boolean conflict = false;
        ArrayList<Future<List<IntNameDescPairPair>>> futures1 = new ArrayList<>(size);
        for (ClassMapping c : tree.getClasses()) {
            futures1.add(threadpool.submit(() -> {
                String name = c.getName(src);
                if (name == null) return Collections.emptyList();
                Map<NameDescPair, InheritanceGroup> igroups = inheritanceMap.inheritanceMap.get(new Mutf8Slice(name));
                if (igroups == null) {
                    Logger.warn("Mapping for unknown class {}", name);
                    return Collections.emptyList();
                }
                Collection<? extends MethodMapping> methods = c.getMethods();
                ArrayList<IntNameDescPairPair> r = new ArrayList<>(methods.size());
                for (MethodMapping m : methods) {
                    String mname = m.getName(src);
                    String mdesc = m.getDesc(src);
                    String mname2 = m.getName(dst);
                    String mdesc2 = m.getDesc(dst);
                    if (mname == null || mdesc == null || mname2 == null || mdesc2 == null) continue;
                    NameDescPair msrc = new NameDescPair(new Mutf8Slice(mname), new Mutf8Slice(mdesc));
                    InheritanceGroup igroup = igroups.get(msrc);
                    if (igroup == null) continue;
                    NameDescPair mdst = new NameDescPair(new Mutf8Slice(mname2), new Mutf8Slice(mdesc2));
                    IntNameDescPairPair indpp = new IntNameDescPairPair();
                    indpp.key = igroup.id;
                    indpp.value = mdst;
                    int s = 0;
                    for (MethodArgMapping mam : m.getArgs()) {
                        if (mam.getName(dst) != null) s++;
                    }
                    if (s > 0) {
                        indpp.params = new Int2ObjectOpenHashMap<>(s);
                        for (MethodArgMapping mam : m.getArgs()) {
                            String mamn = mam.getName(dst);
                            if (mamn != null) {
                                indpp.params.put(mam.getLvIndex(), new Mutf8Slice(mamn));
                            }
                        }
                    }
                    r.add(indpp);
                }
                return r;
            }));
        }
        for (Future<List<IntNameDescPairPair>> f : futures1) {
            try {
                List<IntNameDescPairPair> indpps = f.get();
                for (IntNameDescPairPair indpp : indpps) {
                    NameDescPair old = methodMap.put(indpp.key, indpp.value);
                    if (old != null && !old.equals(indpp.value)) {
                        Logger.error("Method mapping confllict {} {} {}", indpp.value, old, indpp.value);
                        conflict = true;
                    }
                    if (indpp.params != null) {
                        methodArgMap.compute(indpp.key, (k, v) -> {
                            if (v == null) return indpp.params;
                            for (Int2ObjectMap.Entry<Mutf8Slice> e : indpp.params.int2ObjectEntrySet()) {
                                Mutf8Slice o = v.put(e.getIntKey(), e.getValue());
                                if (o != null && !o.equals(e.getValue())) {
                                    if (LOG_PARAM_CONFLICS) Logger.warn("Param conflict {} {} {}", indpp.value, o, e.getValue());
                                    v.put(e.getIntKey(), o.compareTo(e.getValue()) > 0 ? o : e.getValue());
                                }
                            }
                            return v;
                        });
                    }
                }
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        }
        if (conflict) throw new RuntimeException("Mapping conflict");
    }

    //3am hack
    public void addClassMapping(Mutf8Slice src, Mutf8Slice dst) {
        classMap.put(src, dst);
    }

    @Override
    public Mutf8Slice mapClass(Mutf8Slice srcCls) {
        return classMap.getOrDefault(srcCls, srcCls);
    }

    @Override
    public NameDescPair mapMethod(Mutf8Slice srcCls, NameDescPair srcMethod) {
        InheritanceGroup ig = mapMethod0(srcCls, srcMethod, true);
        if (ig != null) {
            NameDescPair r = methodMap.get(ig.id);
            if (r != null) return r;
        }
        return new NameDescPair(srcMethod.name, Mappings.remapMethodDescriptor(this, srcMethod.desc));
    }

    @Nullable InheritanceGroup mapMethod0(Mutf8Slice srcCls, NameDescPair srcMethod, boolean first) {
        Map<NameDescPair, InheritanceGroup> inherMap = inheritanceMap.inheritanceMap.get(srcCls);
        InheritanceGroup r = null;
        int flags = inheritanceMap.getIInfoMap(srcCls).getOrDefault(srcMethod, AccessFlags.ACC_PRIVATE);
        if (inherMap != null && (first || (flags & AccessFlags.ACC_PRIVATE) == 0)) {
            r = inherMap.get(srcMethod);
            if (r != null) return r;
        }
        Collection<Mutf8Slice> below = inheritanceMap.belowMap.get(srcCls);
        if (below != null) {
            for (Mutf8Slice b : below) {
                InheritanceGroup r0 = mapMethod0(b, srcMethod, false);
                if (r == null) {
                    r = r0;
                } else if (r0 != null) {
                    if (r.id != r0.id && !Objects.equals(methodMap.get(r.id), methodMap.get(r0.id))) {
                        throw new RuntimeException("Mapping conflict " + srcCls + " " + methodMap.get(r.id) + " " + methodMap.get(r0.id));
                    }
                }
            }
        }
        return r;
    }

    @Override
    public NameDescPair mapField(Mutf8Slice srcCls, NameDescPair srcField) {
        HashMap<NameDescPair, NameDescPair> fmap = fieldMap.get(srcCls);
        if (fmap != null) {
            NameDescPair r = fmap.get(srcField);
            if (r != null) return r;
        }
        Collection<Mutf8Slice> b = inheritanceMap.belowMap.get(srcCls);
        while (b != null && !b.isEmpty()) {
            for (Mutf8Slice c : b) {
                HashMap<NameDescPair, NameDescPair> fmap2 = fieldMap.get(c);
                if (fmap2 != null) {
                    NameDescPair r = fmap2.get(srcField);
                    if (r != null) return r;
                }
            }
            int size = 0;
            for (Mutf8Slice c : b) {
                size += inheritanceMap.belowMap.getOrDefault(c, Collections.emptyList()).size();
            }
            if (size == 0) break;
            ArrayList<Mutf8Slice> b0 = new ArrayList<>(size);
            for (Mutf8Slice c : b) {
                b0.addAll(inheritanceMap.belowMap.getOrDefault(c, Collections.emptyList()));
            }
            b = b0;
        }
        return new NameDescPair(srcField.name, Mappings.remapFieldDescriptor(this, srcField.desc));
    }

    @Override
    public @Nullable Mutf8Slice mapParam(Mutf8Slice srcCls, NameDescPair srcMethod, int lvtIndex) {
        InheritanceGroup ig = mapMethod0(srcCls, srcMethod, true);
        if (ig != null) {
            Int2ObjectOpenHashMap<Mutf8Slice> r1 = methodArgMap.get(ig.id);
            if (r1 == null) return null;
            return r1.get(lvtIndex);
        }
        return null;
    }
}
