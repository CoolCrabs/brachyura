package io.github.coolcrabs.brachyura.recombobulator.remapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.function.Supplier;

import io.github.coolcrabs.brachyura.recombobulator.AccessFlags;
import io.github.coolcrabs.brachyura.recombobulator.ClassInfo;
import io.github.coolcrabs.brachyura.recombobulator.MethodInfo;
import io.github.coolcrabs.brachyura.recombobulator.Mutf8Slice;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

public class InheritanceMap {
    public HashMap<Mutf8Slice, HashMap<NameDescPair, InheritanceGroup>> inheritanceMap;
    public ConcurrentHashMap<Mutf8Slice, Object2IntMap<NameDescPair>> inheritanceInfoMap;
    public ConcurrentHashMap<Mutf8Slice, ConcurrentLinkedQueue<Mutf8Slice>> aboveMap;
    public ConcurrentHashMap<Mutf8Slice, Collection<Mutf8Slice>> belowMap;

    int curId = 0;

    public void initCapacity(int size) {
        if (inheritanceMap == null) {
            inheritanceMap = new HashMap<>(size);
            inheritanceInfoMap = new ConcurrentHashMap<>(size);
            aboveMap = new ConcurrentHashMap<>(size);
            belowMap = new ConcurrentHashMap<>(size);
        }
    }

    public void load(Collection<Entry<Mutf8Slice, Supplier<ClassInfo>>> classes, boolean canRemap) {
        ExecutorService threadpool = ForkJoinPool.commonPool();
        ArrayList<Future<?>> futures = new ArrayList<>(classes.size());
        initCapacity(classes.size());
        // Setup inheritance
        for (Entry<Mutf8Slice, Supplier<ClassInfo>> e : classes) {
            futures.add(
                threadpool.submit(() -> {
                    Mutf8Slice name = e.getKey();
                    ClassInfo ci = e.getValue().get();
                    int size = ci.interfaces.length;
                    int off = 0;
                    if (ci.super_class != 0) {
                        size += 1;
                        off = 1;
                    }
                    Mutf8Slice[] below = new Mutf8Slice[size];
                    if (ci.super_class != 0) below[0] = RecombobulatorRemapper.cls2Utf8(ci.pool, ci.super_class);
                    for (int i = 0; i < ci.interfaces.length; i++) {
                        below[off + i] = RecombobulatorRemapper.cls2Utf8(ci.pool, ci.interfaces[i]);
                    }
                    
                    belowMap.put(name, Arrays.asList(below));
                    for (Mutf8Slice b : below) {
                        aboveMap.computeIfAbsent(b, k -> new ConcurrentLinkedQueue<>()).add(name);
                    }
                    Object2IntMap<NameDescPair> iinfo = new Object2IntOpenHashMap<>(ci.methods.size());
                    for (MethodInfo mi : ci.methods) {
                        iinfo.put(new NameDescPair(RecombobulatorRemapper.utf8(ci.pool, mi.name_index), RecombobulatorRemapper.utf8(ci.pool, mi.descriptor_index)), mi.access_flags);
                    }
                    inheritanceInfoMap.put(name, iinfo);
                })
            );
        }
        for (Future<?> f : futures) {
            try {
                f.get();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        }
        futures.clear();
        // Build method inheritance groups
        //TODO: was hard to multithread but might be worth it
        for (Entry<Mutf8Slice, Supplier<ClassInfo>> e : classes) {
            ClassInfo ci = e.getValue().get();
            Mutf8Slice clsName = e.getKey();
            HashMap<NameDescPair, InheritanceGroup> mIgroupMap = getMethodMap(clsName, ci.methods.size());
            Object2IntMap<NameDescPair> mIinfoMap = getIInfoMap(clsName);
            for (MethodInfo methodInfo : ci.methods) {
                NameDescPair methodKey = new NameDescPair(RecombobulatorRemapper.utf8(ci.pool, methodInfo.name_index), RecombobulatorRemapper.utf8(ci.pool, methodInfo.descriptor_index));
                InheritanceGroup igroup = mIgroupMap.computeIfAbsent(methodKey, k -> new InheritanceGroup(curId++, canRemap));
                int acc = mIinfoMap.getInt(methodKey); // Will always be present
                if (canPropogate(acc)) { // Can propogate    
                    ConcurrentHashMap<Mutf8Slice, ? extends Collection<Mutf8Slice>> directionMap = aboveMap;
                    for (int i = 0; i < 2; i++) { // Loop twice once for up once for down
                        ArrayList<Mutf8Slice> la = new ArrayList<>();
                        ArrayList<Mutf8Slice> lb = new ArrayList<>();
                        la.add(clsName);
                        while (!la.isEmpty()) {
                            for (Mutf8Slice lai : la) {
                                Collection<Mutf8Slice> nextInDirection = directionMap.get(lai);
                                if (nextInDirection != null) lb.addAll(nextInDirection);
                            }
                            ArrayList<Mutf8Slice> temp = la;
                            la = lb;
                            lb = temp;
                            lb.clear();
                            for (Mutf8Slice lai : la) {
                                Object2IntMap<NameDescPair> laiIinfoMap = getIInfoMap(lai);
                                HashMap<NameDescPair, InheritanceGroup> laiIgroupMap = getMethodMap(lai, laiIinfoMap.size());
                                int laiAcc = laiIinfoMap.getOrDefault(methodKey, AccessFlags.ACC_PRIVATE); // private can't propogate so use as default
                                if (canPropogate(laiAcc)) {
                                    laiIgroupMap.compute(methodKey, (k, v) -> {
                                        if (v != null) {
                                            igroup.join(v);
                                        }
                                        return igroup;
                                    });
                                }
                            }
                        }
                        directionMap = belowMap;
                    }
                }
            }
        }
    }
    
    static final boolean canPropogate(int acc) {
        return (acc & AccessFlags.ACC_STATIC) == 0 && (acc & AccessFlags.ACC_PRIVATE) == 0;
    }

    private HashMap<NameDescPair, InheritanceGroup> getMethodMap(Mutf8Slice clsName, int size) {
        return inheritanceMap.computeIfAbsent(clsName, c -> new HashMap<>(size));
    }

    Object2IntMap<NameDescPair> getIInfoMap(Mutf8Slice clsName) {
        return inheritanceInfoMap.getOrDefault(clsName, Object2IntMaps.emptyMap());
    }

    public static class InheritanceGroup {
        public int id;
        public boolean canRemap;
        InheritanceGroup down;
        InheritanceGroup up;

        public InheritanceGroup(int id, boolean canRemap) {
            this.id = id;
            this.canRemap = canRemap;
        }

        public void join(InheritanceGroup o) {
            if (o.id == this.id) return;
            InheritanceGroup a = this;
            while (a.down != null) a = a.down;
            InheritanceGroup b = o;
            while (b.up != null) b = b.up;
            a.down = b;
            b.up = a;
            if (a.canRemap != b.canRemap) {
                if (a.canRemap) {
                    InheritanceGroup a0 = a;
                    for (;;) {
                        a0.canRemap = false;
                        a0 = a0.up;
                        if (a0 == null) break;
                    }
                } else { // b.canRemap
                    InheritanceGroup b0 = b;
                    for (;;) {
                        b0.canRemap = false;
                        b0 = b0.down;
                        if (b0 == null) break;
                    }
                }
            }
            InheritanceGroup c = b;
            for (;;) {
                c.id = id;
                c = c.down;
                if (c == null) break;
            }
        }
    }
}
