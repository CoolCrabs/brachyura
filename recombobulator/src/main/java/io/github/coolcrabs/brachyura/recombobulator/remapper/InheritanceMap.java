package io.github.coolcrabs.brachyura.recombobulator.remapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
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
    public ConcurrentHashMap<Mutf8Slice, Map<NameDescPair, InheritanceGroup>> inheritanceMap;
    public ConcurrentHashMap<Mutf8Slice, Object2IntMap<NameDescPair>> inheritanceInfoMap;
    public ConcurrentHashMap<Mutf8Slice, Collection<Mutf8Slice>> belowMap;

    public int curId = 1;

    public void initCapacity(int size) {
        if (inheritanceMap == null) {
            inheritanceMap = new ConcurrentHashMap<>(size);
            inheritanceInfoMap = new ConcurrentHashMap<>(size);
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
                    Object2IntMap<NameDescPair> iinfo = new Object2IntOpenHashMap<>(ci.methods.size());
                    HashMap<NameDescPair, InheritanceGroup> imap = new HashMap<>(ci.methods.size());
                    for (MethodInfo mi : ci.methods) {
                        NameDescPair ndp = new NameDescPair(RecombobulatorRemapper.utf8(ci.pool, mi.name_index), RecombobulatorRemapper.utf8(ci.pool, mi.descriptor_index));
                        iinfo.put(ndp, mi.access_flags);
                        imap.put(ndp, new InheritanceGroup(name, ndp));
                    }
                    inheritanceInfoMap.compute(name, (k, v) -> {
                        if (v == null) {
                            return iinfo;
                        } else {
                            v.putAll(iinfo);
                            return v;
                        }
                    });
                    inheritanceMap.compute(name, (k, v) -> {
                        if (v == null) {
                            return imap;
                        } else {
                            v.putAll(imap);
                            return v;
                        }
                    });
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
        for (Entry<Mutf8Slice, Supplier<ClassInfo>> e : classes) {
            futures.add(
                threadpool.submit(() -> {
                    ClassInfo ci = e.getValue().get();
                    Mutf8Slice clsName = e.getKey();
                    Map<NameDescPair, InheritanceGroup> mIgroupMap = inheritanceMap.get(clsName);
                    Object2IntMap<NameDescPair> mIinfoMap = getIInfoMap(clsName);
                    for (MethodInfo methodInfo : ci.methods) {
                        NameDescPair methodKey = new NameDescPair(RecombobulatorRemapper.utf8(ci.pool, methodInfo.name_index), RecombobulatorRemapper.utf8(ci.pool, methodInfo.descriptor_index));
                        InheritanceGroup igroup = mIgroupMap.get(methodKey);
                        int acc = mIinfoMap.getInt(methodKey); // Will always be present
                        if (canPropogate(acc)) { // Can propogate    
                            ConcurrentHashMap<Mutf8Slice, ? extends Collection<Mutf8Slice>> directionMap = belowMap;
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
                                    Map<NameDescPair, InheritanceGroup> laiIgroupMap = inheritanceMap.getOrDefault(lai, Collections.emptyMap());
                                    int laiAcc = laiIinfoMap.getOrDefault(methodKey, AccessFlags.ACC_PRIVATE); // private can't propogate so use as default
                                    if (canPropogate(laiAcc)) {
                                        igroup.join(laiIgroupMap.get(methodKey));
                                    }
                                }
                            }
                        }
                    }
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
        for (Map<NameDescPair, InheritanceGroup> a : inheritanceMap.values()) {
            for (InheritanceGroup b : a.values()) {
                InheritanceGroup c = b;
                while (c.id == 0 && c.parent != null) {
                    c = c.parent;
                }
                int id = c.id == 0 ? curId++ : c.id;
                c = b;
                while (c != null && c.id == 0) {
                    c.id = id;
                    c = c.parent;
                }
            }
        }
    }
    
    static final boolean canPropogate(int acc) {
        return (acc & AccessFlags.ACC_STATIC) == 0 && (acc & AccessFlags.ACC_PRIVATE) == 0;
    }

    Object2IntMap<NameDescPair> getIInfoMap(Mutf8Slice clsName) {
        return inheritanceInfoMap.getOrDefault(clsName, Object2IntMaps.emptyMap());
    }

    public static class InheritanceGroup {
        public int id;
        public boolean canRemap;

        final Mutf8Slice cls;
        final NameDescPair method;

        public InheritanceGroup(Mutf8Slice cls, NameDescPair method) {
            this.cls = cls;
            this.method = method;
        }

        volatile InheritanceGroup parent;

        public void join(InheritanceGroup o) {
            InheritanceGroup a = o;
            InheritanceGroup b = this;
            // Establish a "canonical ordering" through sorting by class and method
            // This should avoid deadlocks without needing to spin lock in userspace (bad)
            // In practice there is no significant performance improvment over the spinning version
            // https://dzone.com/articles/deadlock-free-synchronization-in-java
            for (;;) {
                a = getTop(a);
                b = getTop(b);
                if (a == b) return;
                InheritanceGroup high;
                InheritanceGroup low;
                int c = a.cls.compareTo(b.cls);
                if (c == 0) c = a.method.name.compareTo(b.method.name);
                if (c == 0) c = a.method.desc.compareTo(b.method.desc);
                if (c == 0) throw new RuntimeException("Duplicate class and method " + a.cls + " " + a.method);
                if (c > 0) {
                    high = a;
                    low = b;
                } else {
                    high = b;
                    low = a;
                }
                synchronized (high) {
                    if (high.parent == null) {
                        synchronized (low) {
                            if (low.parent == null) {
                                low.parent = high;
                            }
                        }
                    }
                }
            }
        }

        public static InheritanceGroup getTop(InheritanceGroup g) {
            InheritanceGroup r = g;
            for (;;) {
                InheritanceGroup p = r.parent;
                if (p == null) {
                    break;
                }
                r = p;
            }
            return r;
        }
    }
}
