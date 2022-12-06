package io.github.coolcrabs.brachyura.recombobulator.remapper;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.function.Supplier;

import org.tinylog.Logger;

import io.github.coolcrabs.brachyura.recombobulator.ByteBufferUtil;
import io.github.coolcrabs.brachyura.recombobulator.ClassInfo;
import io.github.coolcrabs.brachyura.recombobulator.ConstantClass;
import io.github.coolcrabs.brachyura.recombobulator.ConstantDynamic;
import io.github.coolcrabs.brachyura.recombobulator.ConstantFieldref;
import io.github.coolcrabs.brachyura.recombobulator.ConstantInteger;
import io.github.coolcrabs.brachyura.recombobulator.ConstantInterfaceMethodref;
import io.github.coolcrabs.brachyura.recombobulator.ConstantInvokeDynamic;
import io.github.coolcrabs.brachyura.recombobulator.ConstantMethodHandle;
import io.github.coolcrabs.brachyura.recombobulator.ConstantMethodType;
import io.github.coolcrabs.brachyura.recombobulator.ConstantMethodref;
import io.github.coolcrabs.brachyura.recombobulator.ConstantNameAndType;
import io.github.coolcrabs.brachyura.recombobulator.ConstantPool;
import io.github.coolcrabs.brachyura.recombobulator.ConstantPoolEntry;
import io.github.coolcrabs.brachyura.recombobulator.ConstantUtf8;
import io.github.coolcrabs.brachyura.recombobulator.FieldInfo;
import io.github.coolcrabs.brachyura.recombobulator.MethodInfo;
import io.github.coolcrabs.brachyura.recombobulator.Mutf8Slice;
import io.github.coolcrabs.brachyura.recombobulator.attribute.Attribute;
import io.github.coolcrabs.brachyura.recombobulator.attribute.AttributeBootstrapMethods;
import io.github.coolcrabs.brachyura.recombobulator.attribute.AttributeCode;
import io.github.coolcrabs.brachyura.recombobulator.attribute.AttributeLocalVariableTable;
import io.github.coolcrabs.brachyura.recombobulator.attribute.AttributeLocalVariableTypeTable;
import io.github.coolcrabs.brachyura.recombobulator.attribute.AttributeRecord;
import io.github.coolcrabs.brachyura.recombobulator.attribute.AttributeSignature;
import io.github.coolcrabs.brachyura.recombobulator.attribute.Attributes;
import io.github.coolcrabs.brachyura.recombobulator.attribute.EntryBootstrapMethods;
import io.github.coolcrabs.brachyura.recombobulator.attribute.EntryLocalVariableTable;
import io.github.coolcrabs.brachyura.recombobulator.attribute.EntryLocalVariableTypeTable;
import io.github.coolcrabs.brachyura.recombobulator.attribute.RecordComponentInfo;
import io.github.coolcrabs.brachyura.recombobulator.util.ConstantPoolRefCounter;
import it.unimi.dsi.fastutil.ints.IntArrayFIFOQueue;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

public class RecombobulatorRemapper {
    RemapperOutputConsumer output;
    Collection<Input> toRead = new ArrayList<>();
    Collection<OutputFile> toOutput = new ArrayList<>();
    Mappings mappings;

    public void setClasses(Collection<Input> classes) {
        toRead = classes;
    }

    public void setFiles(Collection<OutputFile> files) {
        toOutput = files;
    }

    public void setMappings(Mappings mappings) {
        this.mappings = mappings;
    }

    public void setOutput(RemapperOutputConsumer out) {
        this.output = out;
    }

    public static class Input {
        Supplier<ClassInfo> readSup;
        String path;
        Object tag;
        
        public Input(Supplier<ClassInfo> readSup, String path, Object tag) {
            this.readSup = readSup;
            this.path = path;
            this.tag = tag;
        }
    }

    public static class OutputFile {
        Supplier<InputStream> readSup;
        String path;
        Object tag;

        public OutputFile(Supplier<InputStream> readSup, String path, Object tag) {
            this.readSup = readSup;
            this.path = path;
            this.tag = tag;
        }
    }

    static final Mutf8Slice LAMBDA_META_FACTORY = new Mutf8Slice("java/lang/invoke/LambdaMetafactory");
    static final NameDescPair METAFACTORY = new NameDescPair(new Mutf8Slice("metafactory"), new Mutf8Slice("(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;"));
    static final NameDescPair ALT_METAFACTORY = new NameDescPair(new Mutf8Slice("altMetafactory"), new Mutf8Slice("(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;[Ljava/lang/Object;)Ljava/lang/invoke/CallSite;"));
    static final Mutf8Slice STRING_CONCAT_FACTORY = new Mutf8Slice("java/lang/invoke/StringConcatFactory");
    static final Mutf8Slice OBJECT_METHODS = new Mutf8Slice("java/lang/runtime/ObjectMethods");

    public void run() {
        int totalClasses = toRead.size();
        ExecutorService threadpool = ForkJoinPool.commonPool();
        ArrayList<Future<?>> futures = new ArrayList<>(totalClasses + toOutput.size());
        // Transform and output ClassInfo
        for (Input in : toRead) {
            futures.add(
                threadpool.submit(() -> {
                    ClassInfo ci = in.readSup.get();
                    ConstantPool cp = ci.pool;
                    ConstantPoolRefCounter refs = new ConstantPoolRefCounter(cp);
                    ci.accept(refs);
                    // locate bootstrap methods attribute
                    AttributeBootstrapMethods bootstrap = null;
                    Attributes attributes = ci.attributes;
                    for (int i = 0 ; i < attributes.size(); i++) {
                        Attribute a = attributes.get(i);
                        if (a instanceof AttributeBootstrapMethods) {
                            bootstrap = (AttributeBootstrapMethods) a;
                            break;
                        }
                    }
                    Mutf8Slice clsName = cls2Utf8(cp, ci.this_class);
                    // unref everything + size maps & freelist
                    // + extract methodinfo and fieldinfos
                    // CONSTANT_MethodHandle_info can be ignored since it references another ref
                    int utf8Size = 0;
                    int nameAndTypeSize = 0;
                    for (int i = 1; i <= cp.size(); i++) {
                        ConstantPoolEntry e = cp.getEntry(i);
                        int ref = refs.getRef(i);
                        if (e instanceof ConstantClass) {
                            ConstantClass o = (ConstantClass) e;
                            refs.ref(o.name_index, -ref);
                        } else if (e instanceof ConstantFieldref) {
                            ConstantFieldref o = (ConstantFieldref) e;
                            refs.ref(o.name_and_type_index, -ref);
                        } else if (e instanceof ConstantMethodref) {
                            ConstantMethodref o = (ConstantMethodref) e;
                            refs.ref(o.name_and_type_index, -ref);
                        } else if (e instanceof ConstantInterfaceMethodref) {
                            ConstantInterfaceMethodref o = (ConstantInterfaceMethodref) e;
                            refs.ref(o.name_and_type_index, -ref);
                        } else if (e instanceof ConstantMethodType) {
                            ConstantMethodType o = (ConstantMethodType) e;
                            refs.ref(o.descriptor_index, -ref);
                        } else if (e instanceof ConstantDynamic) {
                            ConstantDynamic o = (ConstantDynamic) e;
                            refs.ref(o.name_and_type_index, -ref);
                        } else if (e instanceof ConstantInvokeDynamic) {
                            ConstantInvokeDynamic o = (ConstantInvokeDynamic) e;
                            refs.ref(o.name_and_type_index, -ref);
                        } else if (e instanceof ConstantUtf8) {
                            utf8Size += 1;
                        } else if (e instanceof ConstantNameAndType) {
                            nameAndTypeSize += 1;
                        }
                    }
                    for (Attribute a : ci.attributes) {
                        if (a instanceof AttributeRecord) {
                            AttributeRecord r = (AttributeRecord) a;
                            for (RecordComponentInfo rci : r.components) {
                                for (Attribute a0 : rci.attributes) {
                                    if (a0 instanceof AttributeSignature) {
                                        AttributeSignature s = (AttributeSignature) a0;
                                        refs.ref(s.signature_index, -1);
                                    }
                                }
                            }
                        } else if (a instanceof AttributeSignature) {
                            AttributeSignature s = (AttributeSignature) a;
                            refs.ref(s.signature_index, -1);
                        }
                    }
                    for (FieldInfo fi : ci.fields) {
                        for (Attribute a : fi.attributes) {
                            if (a instanceof AttributeSignature) {
                                AttributeSignature s = (AttributeSignature) a;
                                refs.ref(s.signature_index, -1);
                            }
                        }
                    }
                    for (MethodInfo mi : ci.methods) {
                        for (Attribute a : mi.attributes) {
                            if (a instanceof AttributeSignature) {
                                AttributeSignature s = (AttributeSignature) a;
                                refs.ref(s.signature_index, -1);
                            } else if (a instanceof AttributeCode) {
                                AttributeCode c = (AttributeCode) a;
                                for (Attribute a0 : c.attributes) {
                                    if (a0 instanceof AttributeLocalVariableTable) {
                                        AttributeLocalVariableTable lvt = (AttributeLocalVariableTable) a0;
                                        for (EntryLocalVariableTable lve : lvt.local_variable_table) {
                                            refs.ref(lve.descriptor_index, -1);
                                        }
                                    } else if (a0 instanceof AttributeLocalVariableTypeTable) {
                                        AttributeLocalVariableTypeTable lvt = (AttributeLocalVariableTypeTable) a0;
                                        for (EntryLocalVariableTypeTable lve : lvt.local_variable_type_table) {
                                            refs.ref(lve.signature_index, -1);
                                        }
                                    }
                                }
                            }
                        }
                    }
                    NameDescPair[] fields = new NameDescPair[ci.fields.size()];
                    NameDescPair[] methods = new NameDescPair[ci.methods.size()];
                    for (int i = 0; i < ci.fields.size(); i++) {
                        FieldInfo fi = ci.fields.get(i);
                        fields[i] = new NameDescPair(utf8(cp, fi.name_index), utf8(cp, fi.descriptor_index));
                        refs.ref(fi.name_index, -1);
                        refs.ref(fi.descriptor_index, -1);
                    }
                    for (int i = 0; i < ci.methods.size(); i++) {
                        MethodInfo mi = ci.methods.get(i);
                        methods[i] = new NameDescPair(utf8(cp, mi.name_index), utf8(cp, mi.descriptor_index));
                        refs.ref(mi.name_index, -1);
                        refs.ref(mi.descriptor_index, -1);
                    }
                    // Build maps & freelist
                    Object2IntOpenHashMap<Mutf8Slice> utf8Map = new Object2IntOpenHashMap<>(utf8Size);
                    Object2IntOpenHashMap<NameDescPair> nameAndTypeMap = new Object2IntOpenHashMap<>(nameAndTypeSize);
                    IntArrayFIFOQueue openSlots = new IntArrayFIFOQueue(utf8Size + nameAndTypeSize); // TODO is this a reasonable sizing
                    for (int i = 1; i <= cp.size(); i++) {
                        if (refs.getRef(i) == 0) {
                            openSlots.enqueue(i);
                        } else {
                            ConstantPoolEntry e = cp.getEntry(i);
                            if (e instanceof ConstantUtf8) {
                                utf8Map.put(((ConstantUtf8)e).slice, i);
                            } else if (e instanceof ConstantNameAndType) {
                                ConstantNameAndType o = (ConstantNameAndType) e;
                                nameAndTypeMap.put(new NameDescPair(utf8(cp, o.name_index), utf8(cp, o.descriptor_index)), i);
                            }
                        }
                    }
                    // Remap everything
                    ConstantPool newCp = cp.duplicate();
                    for (int i = 1; i <= cp.size(); i++) {
                        ConstantPoolEntry e = cp.getEntry(i);
                        if (e instanceof ConstantFieldref) {
                            ConstantFieldref o = (ConstantFieldref) e;
                            Mutf8Slice oclsname = cls2Utf8(cp, o.class_index);
                            ConstantNameAndType nt = (ConstantNameAndType) cp.getEntry(o.name_and_type_index);
                            NameDescPair field = new NameDescPair(utf8(cp, nt.name_index), utf8(cp, nt.descriptor_index));
                            NameDescPair nfield = mappings.mapField(oclsname, field);
                            newCp.setEntry(i, new ConstantFieldref(o.class_index, getNameAndType(cp, newCp, utf8Map, nameAndTypeMap, openSlots, nfield)));
                        } else if (e instanceof ConstantMethodref) {
                            ConstantMethodref o = (ConstantMethodref) e;
                            Mutf8Slice oclsname = cls2Utf8(cp, o.class_index);
                            ConstantNameAndType nt = (ConstantNameAndType) cp.getEntry(o.name_and_type_index);
                            NameDescPair method = new NameDescPair(utf8(cp, nt.name_index), utf8(cp, nt.descriptor_index));
                            NameDescPair nmethod = mappings.mapMethod(oclsname, method);
                            newCp.setEntry(i, new ConstantMethodref(o.class_index, getNameAndType(cp, newCp, utf8Map, nameAndTypeMap, openSlots, nmethod)));
                        } else if (e instanceof ConstantInterfaceMethodref) {
                            ConstantInterfaceMethodref o = (ConstantInterfaceMethodref) e;
                            Mutf8Slice oclsname = cls2Utf8(cp, o.class_index);
                            ConstantNameAndType nt = (ConstantNameAndType) cp.getEntry(o.name_and_type_index);
                            NameDescPair method = new NameDescPair(utf8(cp, nt.name_index), utf8(cp, nt.descriptor_index));
                            NameDescPair nmethod = mappings.mapMethod(oclsname, method);
                            newCp.setEntry(i, new ConstantInterfaceMethodref(o.class_index, getNameAndType(cp, newCp, utf8Map, nameAndTypeMap, openSlots, nmethod)));
                        } else if (e instanceof ConstantMethodType) {
                            ConstantMethodType o = (ConstantMethodType) e;
                            Mutf8Slice mdesc = utf8(cp, o.descriptor_index);
                            newCp.setEntry(i, new ConstantMethodType(getUtf8Index(cp, newCp, utf8Map, openSlots, Mappings.remapMethodDescriptor(mappings, mdesc))));
                        } else if (e instanceof ConstantInvokeDynamic) {
                            ConstantInvokeDynamic o = (ConstantInvokeDynamic) e;
                            EntryBootstrapMethods bm = bootstrap.bootstrap_methods.get(o.bootstrap_method_attr_index);
                            ConstantNameAndType nt = (ConstantNameAndType) cp.getEntry(o.name_and_type_index);
                            Mutf8Slice ntName = utf8(cp, nt.name_index);
                            Mutf8Slice ntdesc = utf8(cp, nt.descriptor_index);
                            ConstantMethodHandle mh = (ConstantMethodHandle) cp.getEntry(bm.bootstrap_method_ref);
                            Mutf8Slice ori = null;
                            Mutf8Slice refclass = null;
                            NameDescPair refmethod = null;
                            ConstantPoolEntry e2 = cp.getEntry(mh.reference_index);
                            if (e2 instanceof ConstantMethodref) {
                                ConstantMethodref o2 = (ConstantMethodref) e2;
                                refclass = cls2Utf8(cp, o2.class_index);
                                ConstantNameAndType nt2 = (ConstantNameAndType) cp.getEntry(o2.name_and_type_index);
                                refmethod = new NameDescPair(utf8(cp, nt2.name_index), utf8(cp, nt2.descriptor_index));
                            } else if (e2 instanceof ConstantInterfaceMethodref) {
                                ConstantInterfaceMethodref o2 = (ConstantInterfaceMethodref) e2;
                                refclass = cls2Utf8(cp, o2.class_index);
                                ConstantNameAndType nt2 = (ConstantNameAndType) cp.getEntry(o2.name_and_type_index);
                                refmethod = new NameDescPair(utf8(cp, nt2.name_index), utf8(cp, nt2.descriptor_index));
                            }
                            // Avoid null pointers, Yoda does
                            if (
                                mh.reference_kind == ConstantMethodHandle.REF_invokeStatic &&
                                LAMBDA_META_FACTORY.equals(refclass) &&
                                (METAFACTORY.equals(refmethod) || ALT_METAFACTORY.equals(refmethod))
                            ) {
                                int lastL = -1;
                                ByteBuffer b = ntdesc.b;
                                for (int j = b.position(); j < b.limit(); j++) {
                                    if (b.get(j) == 'L') {
                                        lastL = j;
                                    }
                                }
                                // Return value of method descriptor is the implemented class
                                ori = new Mutf8Slice(ByteBufferUtil.slice(b, lastL + 1, b.limit() - 1));
                            } else if (!(STRING_CONCAT_FACTORY.equals(refclass) || OBJECT_METHODS.equals(refclass))) {
                                Logger.warn("Unknown invokedynamic {} . {}", refclass, refmethod.name);
                            }
                            if (ori == null) {
                                NameDescPair ndp = new NameDescPair(ntName, Mappings.remapMethodDescriptor(mappings, ntdesc));
                                newCp.setEntry(i, new ConstantInvokeDynamic(o.bootstrap_method_attr_index, getNameAndType(cp, newCp, utf8Map, nameAndTypeMap, openSlots, ndp)));
                            } else {
                                Mutf8Slice desc = utf8(cp, ((ConstantMethodType)cp.getEntry(bm.bootstrap_arguments.get(0))).descriptor_index);
                                NameDescPair newMethod = mappings.mapMethod(ori, new NameDescPair(ntName, desc)); // was mapping against ri instead of ori?
                                NameDescPair ndp = new NameDescPair(newMethod.name, Mappings.remapMethodDescriptor(mappings, ntdesc));
                                newCp.setEntry(i, new ConstantInvokeDynamic(o.bootstrap_method_attr_index, getNameAndType(cp, newCp, utf8Map, nameAndTypeMap, openSlots, ndp)));
                            }
                        } else if (e instanceof ConstantDynamic) {
                            ConstantDynamic o = (ConstantDynamic) e;
                            Logger.warn("Unknown constantdynamic");
                            ConstantNameAndType nt = (ConstantNameAndType) cp.getEntry(o.name_and_type_index);
                            Mutf8Slice ntName = utf8(cp, nt.name_index);
                            Mutf8Slice ntdesc = utf8(cp, nt.descriptor_index);
                            NameDescPair ndp = new NameDescPair(ntName, Mappings.remapFieldDescriptor(mappings, ntdesc));
                            newCp.setEntry(i, new ConstantDynamic(getNameAndType(cp, newCp, utf8Map, nameAndTypeMap, openSlots, ndp), o.name_and_type_index));
                        } else if (e instanceof ConstantClass) {
                            ConstantClass o = (ConstantClass) e;
                            Mutf8Slice oclsname = utf8(cp, o.name_index);
                            if (oclsname.b.get(oclsname.b.position()) == '[') { // https://docs.oracle.com/javase/specs/jvms/se18/html/jvms-4.html#jvms-4.4.1
                                newCp.setEntry(i, new ConstantClass(getUtf8Index(cp, newCp, utf8Map, openSlots, Mappings.remapFieldDescriptor(mappings, oclsname))));
                            } else {
                                newCp.setEntry(i, new ConstantClass(getUtf8Index(cp, newCp, utf8Map, openSlots, mappings.mapClass(oclsname))));
                            }
                        }
                    }
                    for (int i = 0; i < fields.length; i++) {
                        NameDescPair unmapped = fields[i];
                        NameDescPair mapped = mappings.mapField(clsName, unmapped);
                        FieldInfo fi = ci.fields.get(i);
                        fi.name_index = getUtf8Index(cp, newCp, utf8Map, openSlots, mapped.name);
                        fi.descriptor_index = getUtf8Index(cp, newCp, utf8Map, openSlots, mapped.desc);
                    }
                    for (int i = 0; i < methods.length; i++) {
                        NameDescPair unmapped = methods[i];
                        NameDescPair mapped = mappings.mapMethod(clsName, unmapped);
                        MethodInfo mi = ci.methods.get(i);
                        if (mapped == null) {
                            mi.name_index = getUtf8Index(cp, newCp, utf8Map, openSlots, unmapped.name);
                            mi.descriptor_index = getUtf8Index(cp, newCp, utf8Map, openSlots, Mappings.remapMethodDescriptor(mappings, unmapped.desc));
                        } else {
                            mi.name_index = getUtf8Index(cp, newCp, utf8Map, openSlots, mapped.name);
                            mi.descriptor_index = getUtf8Index(cp, newCp, utf8Map, openSlots, mapped.desc);
                        }
                    }
                    for (Attribute a : ci.attributes) {
                        if (a instanceof AttributeRecord) {
                            AttributeRecord r = (AttributeRecord) a;
                            for (RecordComponentInfo rci : r.components) {
                                for (Attribute a0 : rci.attributes) {
                                    if (a0 instanceof AttributeSignature) {
                                        AttributeSignature s = (AttributeSignature) a0;
                                        s.signature_index = getUtf8Index(cp, newCp, utf8Map, openSlots, Mappings.remapSignature(mappings, utf8(cp, s.signature_index)));
                                    }
                                }
                            }
                        } else if (a instanceof AttributeSignature) {
                            AttributeSignature s = (AttributeSignature) a;
                            s.signature_index = getUtf8Index(cp, newCp, utf8Map, openSlots, Mappings.remapSignature(mappings, utf8(cp, s.signature_index)));
                        }
                    }
                    for (FieldInfo fi : ci.fields) {
                        for (Attribute a : fi.attributes) {
                            if (a instanceof AttributeSignature) {
                                AttributeSignature s = (AttributeSignature) a;
                                s.signature_index = getUtf8Index(cp, newCp, utf8Map, openSlots, Mappings.remapSignature(mappings, utf8(cp, s.signature_index)));
                            }
                        }
                    }
                    for (MethodInfo mi : ci.methods) {
                        for (Attribute a : mi.attributes) {
                            if (a instanceof AttributeSignature) {
                                AttributeSignature s = (AttributeSignature) a;
                                s.signature_index = getUtf8Index(cp, newCp, utf8Map, openSlots, Mappings.remapSignature(mappings, utf8(cp, s.signature_index)));
                            } else if (a instanceof AttributeCode) {
                                AttributeCode c = (AttributeCode) a;
                                for (Attribute a0 : c.attributes) {
                                    if (a0 instanceof AttributeLocalVariableTable) {
                                        AttributeLocalVariableTable lvt = (AttributeLocalVariableTable) a0;
                                        for (EntryLocalVariableTable lve : lvt.local_variable_table) {
                                            lve.descriptor_index = getUtf8Index(cp, newCp, utf8Map, openSlots, Mappings.remapFieldDescriptor(mappings, utf8(cp, lve.descriptor_index)));
                                        }
                                    } else if (a0 instanceof AttributeLocalVariableTypeTable) {
                                        AttributeLocalVariableTypeTable lvt = (AttributeLocalVariableTypeTable) a0;
                                        for (EntryLocalVariableTypeTable lve : lvt.local_variable_type_table) {
                                            lve.signature_index = getUtf8Index(cp, newCp, utf8Map, openSlots, Mappings.remapSignature(mappings, utf8(cp, lve.signature_index)));
                                        }
                                    }
                                }
                            }
                        }
                    }
                    while (!openSlots.isEmpty()) {
                        newCp.setEntry(openSlots.dequeueInt(), new ConstantInteger(1337)); // B)
                    }
                    ci.pool = newCp;
                    output.outputClass(cls2Utf8(newCp, ci.this_class) + ".class", ci, in.tag);
                })
            );
        }
        for (OutputFile of : toOutput) {
            futures.add(
                threadpool.submit(() -> {
                    output.outputFile(of.path, of.readSup, of.tag);
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
    }

    static int getUtf8Index(ConstantPool cp, ConstantPool newCp, Object2IntOpenHashMap<Mutf8Slice> utf8Map, IntArrayFIFOQueue openSlots, Mutf8Slice utf8) {
        int index = utf8Map.getOrDefault(utf8, 0);
        if (index == 0) {
            if (!openSlots.isEmpty()) {
                index = openSlots.dequeueInt();
            } else {
                index = newCp.size() + 1;
            }
            newCp.setEntry(index, new ConstantUtf8(utf8));
            utf8Map.put(utf8, index);
        }
        return index;
    }

    static int getNameAndType(ConstantPool cp, ConstantPool newCp, Object2IntOpenHashMap<Mutf8Slice> utf8Map, Object2IntOpenHashMap<NameDescPair> nameAndTypeMap, IntArrayFIFOQueue openSlots, NameDescPair ndp) {
        int index = nameAndTypeMap.getOrDefault(ndp, 0);
        if (index == 0) {
            int nameIndex = getUtf8Index(cp, newCp, utf8Map, openSlots, ndp.name);
            int descIndex = getUtf8Index(cp, newCp, utf8Map, openSlots, ndp.desc);
            if (!openSlots.isEmpty()) {
                index = openSlots.dequeueInt();
            } else {
                index = newCp.size() + 1;
            }
            newCp.setEntry(index, new ConstantNameAndType(nameIndex, descIndex));
            nameAndTypeMap.put(ndp, index);
        }
        return index;
    }

    static Mutf8Slice cls2Utf8(ConstantPool pool, int index) {
        return ((ConstantUtf8)pool.getEntry(((ConstantClass)pool.getEntry(index)).name_index)).slice;
    }

    static Mutf8Slice utf8(ConstantPool pool, int index) {
        return ((ConstantUtf8)pool.getEntry(index)).slice;
    }
}
