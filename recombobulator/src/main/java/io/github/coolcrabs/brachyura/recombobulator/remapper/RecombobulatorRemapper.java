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
import io.github.coolcrabs.brachyura.recombobulator.RecombobulatorVisitor;
import io.github.coolcrabs.brachyura.recombobulator.attribute.Annotation;
import io.github.coolcrabs.brachyura.recombobulator.attribute.Attribute;
import io.github.coolcrabs.brachyura.recombobulator.attribute.AttributeAnnotationDefault;
import io.github.coolcrabs.brachyura.recombobulator.attribute.AttributeBootstrapMethods;
import io.github.coolcrabs.brachyura.recombobulator.attribute.AttributeCode;
import io.github.coolcrabs.brachyura.recombobulator.attribute.AttributeConstantValue;
import io.github.coolcrabs.brachyura.recombobulator.attribute.AttributeDeprecated;
import io.github.coolcrabs.brachyura.recombobulator.attribute.AttributeEnclosingMethod;
import io.github.coolcrabs.brachyura.recombobulator.attribute.AttributeExceptions;
import io.github.coolcrabs.brachyura.recombobulator.attribute.AttributeInnerClasses;
import io.github.coolcrabs.brachyura.recombobulator.attribute.AttributeLineNumberTable;
import io.github.coolcrabs.brachyura.recombobulator.attribute.AttributeLocalVariableTable;
import io.github.coolcrabs.brachyura.recombobulator.attribute.AttributeLocalVariableTypeTable;
import io.github.coolcrabs.brachyura.recombobulator.attribute.AttributeMethodParameters;
import io.github.coolcrabs.brachyura.recombobulator.attribute.AttributeModule;
import io.github.coolcrabs.brachyura.recombobulator.attribute.AttributeModuleMainClass;
import io.github.coolcrabs.brachyura.recombobulator.attribute.AttributeModulePackages;
import io.github.coolcrabs.brachyura.recombobulator.attribute.AttributeNestHost;
import io.github.coolcrabs.brachyura.recombobulator.attribute.AttributeNestMembers;
import io.github.coolcrabs.brachyura.recombobulator.attribute.AttributePermittedSubclasses;
import io.github.coolcrabs.brachyura.recombobulator.attribute.AttributeRecord;
import io.github.coolcrabs.brachyura.recombobulator.attribute.AttributeRuntimeInvisibleAnnotations;
import io.github.coolcrabs.brachyura.recombobulator.attribute.AttributeRuntimeInvisibleParameterAnnotations;
import io.github.coolcrabs.brachyura.recombobulator.attribute.AttributeRuntimeInvisibleTypeAnnotations;
import io.github.coolcrabs.brachyura.recombobulator.attribute.AttributeRuntimeVisibleAnnotations;
import io.github.coolcrabs.brachyura.recombobulator.attribute.AttributeRuntimeVisibleParameterAnnotations;
import io.github.coolcrabs.brachyura.recombobulator.attribute.AttributeRuntimeVisibleTypeAnnotations;
import io.github.coolcrabs.brachyura.recombobulator.attribute.AttributeSignature;
import io.github.coolcrabs.brachyura.recombobulator.attribute.AttributeSourceDebugExtension;
import io.github.coolcrabs.brachyura.recombobulator.attribute.AttributeSourceFile;
import io.github.coolcrabs.brachyura.recombobulator.attribute.AttributeStackMapTable;
import io.github.coolcrabs.brachyura.recombobulator.attribute.AttributeSynthetic;
import io.github.coolcrabs.brachyura.recombobulator.attribute.AttributeUnknown;
import io.github.coolcrabs.brachyura.recombobulator.attribute.Attributes;
import io.github.coolcrabs.brachyura.recombobulator.attribute.ElementValueAnnotation;
import io.github.coolcrabs.brachyura.recombobulator.attribute.ElementValueArray;
import io.github.coolcrabs.brachyura.recombobulator.attribute.ElementValueClass;
import io.github.coolcrabs.brachyura.recombobulator.attribute.ElementValueConst;
import io.github.coolcrabs.brachyura.recombobulator.attribute.ElementValueEnum;
import io.github.coolcrabs.brachyura.recombobulator.attribute.ElementValuePair;
import io.github.coolcrabs.brachyura.recombobulator.attribute.EntryBootstrapMethods;
import io.github.coolcrabs.brachyura.recombobulator.attribute.EntryClasses;
import io.github.coolcrabs.brachyura.recombobulator.attribute.EntryExceptionTable;
import io.github.coolcrabs.brachyura.recombobulator.attribute.EntryExports;
import io.github.coolcrabs.brachyura.recombobulator.attribute.EntryLineNumberTable;
import io.github.coolcrabs.brachyura.recombobulator.attribute.EntryLocalVariableTable;
import io.github.coolcrabs.brachyura.recombobulator.attribute.EntryLocalVariableTypeTable;
import io.github.coolcrabs.brachyura.recombobulator.attribute.EntryOpens;
import io.github.coolcrabs.brachyura.recombobulator.attribute.EntryParameterAnnotations;
import io.github.coolcrabs.brachyura.recombobulator.attribute.EntryParameters;
import io.github.coolcrabs.brachyura.recombobulator.attribute.EntryPath;
import io.github.coolcrabs.brachyura.recombobulator.attribute.EntryProvides;
import io.github.coolcrabs.brachyura.recombobulator.attribute.EntryRequires;
import io.github.coolcrabs.brachyura.recombobulator.attribute.RecordComponentInfo;
import io.github.coolcrabs.brachyura.recombobulator.attribute.SMFrameAppend;
import io.github.coolcrabs.brachyura.recombobulator.attribute.SMFrameChop;
import io.github.coolcrabs.brachyura.recombobulator.attribute.SMFrameExtendedSame;
import io.github.coolcrabs.brachyura.recombobulator.attribute.SMFrameExtendedSameLocals1StackItem;
import io.github.coolcrabs.brachyura.recombobulator.attribute.SMFrameFull;
import io.github.coolcrabs.brachyura.recombobulator.attribute.SMFrameSame;
import io.github.coolcrabs.brachyura.recombobulator.attribute.SMFrameSameLocals1StackItem;
import io.github.coolcrabs.brachyura.recombobulator.attribute.TargetCatch;
import io.github.coolcrabs.brachyura.recombobulator.attribute.TargetEmpty;
import io.github.coolcrabs.brachyura.recombobulator.attribute.TargetFormalParameter;
import io.github.coolcrabs.brachyura.recombobulator.attribute.TargetLocalvar;
import io.github.coolcrabs.brachyura.recombobulator.attribute.TargetLocalvar.LocalvarTableEntry;
import io.github.coolcrabs.brachyura.recombobulator.attribute.TargetOffset;
import io.github.coolcrabs.brachyura.recombobulator.attribute.TargetSupertype;
import io.github.coolcrabs.brachyura.recombobulator.attribute.TargetThrows;
import io.github.coolcrabs.brachyura.recombobulator.attribute.TargetTypeArgument;
import io.github.coolcrabs.brachyura.recombobulator.attribute.TargetTypeParameter;
import io.github.coolcrabs.brachyura.recombobulator.attribute.TargetTypeParameterBound;
import io.github.coolcrabs.brachyura.recombobulator.attribute.TypeAnnotation;
import io.github.coolcrabs.brachyura.recombobulator.attribute.TypePath;
import io.github.coolcrabs.brachyura.recombobulator.attribute.VerificationTypeNonreference;
import io.github.coolcrabs.brachyura.recombobulator.attribute.VerificationTypeObject;
import io.github.coolcrabs.brachyura.recombobulator.attribute.VerificationTypeUninitialized;
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

    public static class DerefVisitor implements RecombobulatorVisitor {
        final ConstantPoolRefCounter refs;
        final ConstantPool cp;
        int utf8Size = 0;
        int nameAndTypeSize = 0;

        public DerefVisitor(ConstantPool cp, ConstantPoolRefCounter refs) {
            this.refs = refs;
            this.cp = cp;
        }

        @Override
        public void visitClassInfo(ClassInfo el) {
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
        }

        @Override
        public void visitMethodInfo(MethodInfo el) {
            refs.ref(el.name_index, -1);
            refs.ref(el.descriptor_index, -1);
        }

        @Override
        public void visitFieldInfo(FieldInfo el) {
            refs.ref(el.name_index, -1);
            refs.ref(el.descriptor_index, -1);
        }

        @Override
        public void visitRecordComponentInfo(RecordComponentInfo el) {
            //noop
        }

        @Override
        public void visitAnnotation(Annotation el) {
            refs.ref(el.type_index, -1);
        }

        @Override
        public void visitElementValuePair(ElementValuePair el) {
            //noop
        }

        @Override
        public void visitElementValueConst(ElementValueConst el) {
            //noop
        }

        @Override
        public void visitElementValueEnum(ElementValueEnum el) {
            //noop
        }

        @Override
        public void visitElementValueClass(ElementValueClass el) {
            refs.ref(el.class_info_index, -1);
        }

        @Override
        public void visitElementValueAnnotation(ElementValueAnnotation el) {
            //noop
        }

        @Override
        public void visitElementValueArray(ElementValueArray el) {
            //noop
        }

        @Override
        public void visitTypeAnnotation(TypeAnnotation el) {
            //noop
        }

        @Override
        public void visitTargetTypeParameter(TargetTypeParameter el) {
            //noop
        }

        @Override
        public void visitTargetSupertype(TargetSupertype el) {
            //noop
        }

        @Override
        public void visitTargetTypeParameterBound(TargetTypeParameterBound el) {
            //noop
        }

        @Override
        public void visitTargetEmpty(TargetEmpty el) {
            //noop
        }

        @Override
        public void visitTargetFormalParameter(TargetFormalParameter el) {
            //noop
        }

        @Override
        public void visitTargetThrows(TargetThrows el) {
            //noop
        }

        @Override
        public void visitTargetLocalvar(TargetLocalvar el) {
            //noop
        }

        @Override
        public void visitLocalvarTableEntry(LocalvarTableEntry el) {
            //noop
        }

        @Override
        public void visitTargetCatch(TargetCatch el) {
            //noop
        }

        @Override
        public void visitTargetOffset(TargetOffset el) {
            //noop
        }

        @Override
        public void visitTargetTypeArgument(TargetTypeArgument el) {
            //noop
        }

        @Override
        public void visitTypePath(TypePath el) {
            //noop
        }

        @Override
        public void visitEntryPath(EntryPath el) {
            //noop
        }

        @Override
        public void visitSMFrameSame(SMFrameSame el) {
            //noop
        }

        @Override
        public void visitSMFrameSameLocals1StackItem(SMFrameSameLocals1StackItem el) {
            //noop
        }

        @Override
        public void visitSMFrameExtendedSameLocals1StackItem(SMFrameExtendedSameLocals1StackItem el) {
            //noop
        }

        @Override
        public void visitSMFrameChop(SMFrameChop el) {
            //noop
        }

        @Override
        public void visitSMFrameExtendedSame(SMFrameExtendedSame el) {
            //noop
        }

        @Override
        public void visitSMFrameAppend(SMFrameAppend el) {
            //noop
        }

        @Override
        public void visitSMFrameFull(SMFrameFull el) {
            //noop
        }

        @Override
        public void visitVerificationTypeNonreference(VerificationTypeNonreference el) {
            //noop
        }

        @Override
        public void visitVerificationTypeObject(VerificationTypeObject el) {
            //noop
        }

        @Override
        public void visitVerificationTypeUninitialized(VerificationTypeUninitialized el) {
            //noop
        }

        @Override
        public void visitAttributeUnknown(AttributeUnknown el) {
            //noop
        }

        @Override
        public void visitAttributeAnnotationDefault(AttributeAnnotationDefault el) {
            //noop
        }

        @Override
        public void visitAttributeBootstrapMethods(AttributeBootstrapMethods el) {
            //noop
        }

        @Override
        public void visitAttributeCode(AttributeCode el) {
            //noop
        }

        @Override
        public void visitAttributeConstantValue(AttributeConstantValue el) {
            //noop
        }

        @Override
        public void visitAttributeDeprecated(AttributeDeprecated el) {
            //noop
        }

        @Override
        public void visitAttributeEnclosingMethod(AttributeEnclosingMethod el) {
            //noop
        }

        @Override
        public void visitAttributeExceptions(AttributeExceptions el) {
            //noop
        }

        @Override
        public void visitAttributeInnerClasses(AttributeInnerClasses el) {
            //noop
        }

        @Override
        public void visitAttributeLineNumberTable(AttributeLineNumberTable el) {
            //noop
        }

        @Override
        public void visitAttributeLocalVariableTable(AttributeLocalVariableTable el) {
            //noop
        }

        @Override
        public void visitAttributeLocalVariableTypeTable(AttributeLocalVariableTypeTable el) {
            //noop
        }

        @Override
        public void visitAttributeMethodParameters(AttributeMethodParameters el) {
            //noop
        }

        @Override
        public void visitAttributeModule(AttributeModule el) {
            //noop
        }

        @Override
        public void visitAttributeModuleMainClass(AttributeModuleMainClass el) {
            //noop
        }

        @Override
        public void visitAttributeModulePackages(AttributeModulePackages el) {
            //noop
        }

        @Override
        public void visitAttributeNestHost(AttributeNestHost el) {
            //noop
        }

        @Override
        public void visitAttributeNestMembers(AttributeNestMembers el) {
            //noop
        }

        @Override
        public void visitAttributePermittedSubclasses(AttributePermittedSubclasses el) {
            //noop
        }

        @Override
        public void visitAttributeRecord(AttributeRecord el) {
            //noop
        }

        @Override
        public void visitAttributeRuntimeInvisibleAnnotations(AttributeRuntimeInvisibleAnnotations el) {
            //noop
        }

        @Override
        public void visitAttributeRuntimeInvisibleParameterAnnotations(AttributeRuntimeInvisibleParameterAnnotations el) {
            //noop
        }

        @Override
        public void visitAttributeRuntimeInvisibleTypeAnnotations(AttributeRuntimeInvisibleTypeAnnotations el) {
            //noop
        }

        @Override
        public void visitAttributeRuntimeVisibleAnnotations(AttributeRuntimeVisibleAnnotations el) {
            //noop
        }

        @Override
        public void visitAttributeRuntimeVisibleParameterAnnotations(AttributeRuntimeVisibleParameterAnnotations el) {
            //noop
        }

        @Override
        public void visitAttributeRuntimeVisibleTypeAnnotations(AttributeRuntimeVisibleTypeAnnotations el) {
            //noop
        }

        @Override
        public void visitAttributeSignature(AttributeSignature el) {
            refs.ref(el.signature_index, -1);
        }

        @Override
        public void visitAttributeSourceDebugExtension(AttributeSourceDebugExtension el) {
            //noop
        }

        @Override
        public void visitAttributeSourceFile(AttributeSourceFile el) {
            //noop
        }

        @Override
        public void visitAttributeStackMapTable(AttributeStackMapTable el) {
            //noop
        }

        @Override
        public void visitAttributeSynthetic(AttributeSynthetic el) {
            //noop
        }

        @Override
        public void visitEntryParameters(EntryParameters el) {
            //noop
        }

        @Override
        public void visitEntryExceptionTable(EntryExceptionTable el) {
            //noop
        }

        @Override
        public void visitEntryBootstrapMethods(EntryBootstrapMethods el) {
            //noop
        }

        @Override
        public void visitEntryParameterAnnotations(EntryParameterAnnotations el) {
            //noop
        }

        @Override
        public void visitEntryLocalVariableTypeTable(EntryLocalVariableTypeTable el) {
            refs.ref(el.signature_index, -1);
        }

        @Override
        public void visitEntryProvides(EntryProvides el) {
            //noop
        }

        @Override
        public void visitEntryExports(EntryExports el) {
            //noop
        }

        @Override
        public void visitEntryClasses(EntryClasses el) {
            refs.ref(el.inner_name_index, -1);
        }

        @Override
        public void visitEntryLineNumberTable(EntryLineNumberTable el) {
            //noop
        }

        @Override
        public void visitEntryLocalVariableTable(EntryLocalVariableTable el) {
            refs.ref(el.descriptor_index, -1);
        }

        @Override
        public void visitEntryOpens(EntryOpens el) {
            //noop
        }

        @Override
        public void visitEntryRequires(EntryRequires el) {
            //noop
        }
    }

    public static class RemapVisitor implements RecombobulatorVisitor {
        final ConstantPool cp;
        final ConstantPool newCp;
        final Mappings mappings;
        final Object2IntOpenHashMap<Mutf8Slice> utf8Map;
        final Object2IntOpenHashMap<NameDescPair> nameAndTypeMap;
        final IntArrayFIFOQueue openSlots;

        Mutf8Slice clsName;

        public RemapVisitor(ConstantPool cp, ConstantPool newCp, Mappings mappings, Object2IntOpenHashMap<Mutf8Slice> utf8Map, Object2IntOpenHashMap<NameDescPair> nameAndTypeMap, IntArrayFIFOQueue openSlots) {
            this.cp = cp;
            this.newCp = newCp;
            this.mappings = mappings;
            this.utf8Map = utf8Map;
            this.nameAndTypeMap = nameAndTypeMap;
            this.openSlots = openSlots;
        }

        @Override
        public void visitClassInfo(ClassInfo el) {
            clsName = cls2Utf8(cp, el.this_class);
            AttributeBootstrapMethods bootstrap = null;
            Attributes attributes = el.attributes;
            for (int i = 0 ; i < attributes.size(); i++) {
                Attribute a = attributes.get(i);
                if (a instanceof AttributeBootstrapMethods) {
                    bootstrap = (AttributeBootstrapMethods) a;
                    break;
                }
            }
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
        }

        @Override
        public void visitMethodInfo(MethodInfo el) {
            NameDescPair unmapped = new NameDescPair(utf8(cp, el.name_index), utf8(cp, el.descriptor_index));
            NameDescPair mapped = mappings.mapMethod(clsName, unmapped);
            if (mapped == null) {
                el.name_index = getUtf8Index(cp, newCp, utf8Map, openSlots, unmapped.name);
                el.descriptor_index = getUtf8Index(cp, newCp, utf8Map, openSlots, Mappings.remapMethodDescriptor(mappings, unmapped.desc));
            } else {
                el.name_index = getUtf8Index(cp, newCp, utf8Map, openSlots, mapped.name);
                el.descriptor_index = getUtf8Index(cp, newCp, utf8Map, openSlots, mapped.desc);
            }
        }

        @Override
        public void visitFieldInfo(FieldInfo el) {
            NameDescPair mapped = mappings.mapField(clsName, new NameDescPair(utf8(cp, el.name_index), utf8(cp, el.descriptor_index)));
            el.name_index = getUtf8Index(cp, newCp, utf8Map, openSlots, mapped.name);
            el.descriptor_index = getUtf8Index(cp, newCp, utf8Map, openSlots, mapped.desc);
        }

        @Override
        public void visitRecordComponentInfo(RecordComponentInfo el) {
            //noop
        }

        @Override
        public void visitAnnotation(Annotation el) {
            el.type_index = getUtf8Index(cp, newCp, utf8Map, openSlots, Mappings.remapFieldDescriptor(mappings, utf8(cp, el.type_index)));
        }

        @Override
        public void visitElementValuePair(ElementValuePair el) {
            //noop
        }

        @Override
        public void visitElementValueConst(ElementValueConst el) {
            //noop
        }

        @Override
        public void visitElementValueEnum(ElementValueEnum el) {
            //noop
        }

        @Override
        public void visitElementValueClass(ElementValueClass el) {
            el.class_info_index = getUtf8Index(cp, newCp, utf8Map, openSlots, Mappings.remapFieldDescriptor(mappings, utf8(cp, el.class_info_index)));
        }

        @Override
        public void visitElementValueAnnotation(ElementValueAnnotation el) {
            //noop
        }

        @Override
        public void visitElementValueArray(ElementValueArray el) {
            //noop
        }

        @Override
        public void visitTypeAnnotation(TypeAnnotation el) {
            //noop
        }

        @Override
        public void visitTargetTypeParameter(TargetTypeParameter el) {
            //noop
        }

        @Override
        public void visitTargetSupertype(TargetSupertype el) {
            //noop
        }

        @Override
        public void visitTargetTypeParameterBound(TargetTypeParameterBound el) {
            //noop
        }

        @Override
        public void visitTargetEmpty(TargetEmpty el) {
            //noop
        }

        @Override
        public void visitTargetFormalParameter(TargetFormalParameter el) {
            //noop
        }

        @Override
        public void visitTargetThrows(TargetThrows el) {
            //noop
        }

        @Override
        public void visitTargetLocalvar(TargetLocalvar el) {
            //noop
        }

        @Override
        public void visitLocalvarTableEntry(LocalvarTableEntry el) {
            //noop
        }

        @Override
        public void visitTargetCatch(TargetCatch el) {
            //noop
        }

        @Override
        public void visitTargetOffset(TargetOffset el) {
            //noop
        }

        @Override
        public void visitTargetTypeArgument(TargetTypeArgument el) {
            //noop
        }

        @Override
        public void visitTypePath(TypePath el) {
            //noop
        }

        @Override
        public void visitEntryPath(EntryPath el) {
            //noop
        }

        @Override
        public void visitSMFrameSame(SMFrameSame el) {
            //noop
        }

        @Override
        public void visitSMFrameSameLocals1StackItem(SMFrameSameLocals1StackItem el) {
            //noop
        }

        @Override
        public void visitSMFrameExtendedSameLocals1StackItem(SMFrameExtendedSameLocals1StackItem el) {
            //noop
        }

        @Override
        public void visitSMFrameChop(SMFrameChop el) {
            //noop
        }

        @Override
        public void visitSMFrameExtendedSame(SMFrameExtendedSame el) {
            //noop
        }

        @Override
        public void visitSMFrameAppend(SMFrameAppend el) {
            //noop
        }

        @Override
        public void visitSMFrameFull(SMFrameFull el) {
            //noop
        }

        @Override
        public void visitVerificationTypeNonreference(VerificationTypeNonreference el) {
            //noop
        }

        @Override
        public void visitVerificationTypeObject(VerificationTypeObject el) {
            //noop
        }

        @Override
        public void visitVerificationTypeUninitialized(VerificationTypeUninitialized el) {
            //noop
        }

        @Override
        public void visitAttributeUnknown(AttributeUnknown el) {
            //noop
        }

        @Override
        public void visitAttributeAnnotationDefault(AttributeAnnotationDefault el) {
            //noop
        }

        @Override
        public void visitAttributeBootstrapMethods(AttributeBootstrapMethods el) {
            //noop
        }

        @Override
        public void visitAttributeCode(AttributeCode el) {
            //noop
        }

        @Override
        public void visitAttributeConstantValue(AttributeConstantValue el) {
            //noop
        }

        @Override
        public void visitAttributeDeprecated(AttributeDeprecated el) {
            //noop
        }

        @Override
        public void visitAttributeEnclosingMethod(AttributeEnclosingMethod el) {
            //noop
        }

        @Override
        public void visitAttributeExceptions(AttributeExceptions el) {
            //noop
        }

        @Override
        public void visitAttributeInnerClasses(AttributeInnerClasses el) {
            //noop
        }

        @Override
        public void visitAttributeLineNumberTable(AttributeLineNumberTable el) {
            //noop
        }

        @Override
        public void visitAttributeLocalVariableTable(AttributeLocalVariableTable el) {
            //noop
        }

        @Override
        public void visitAttributeLocalVariableTypeTable(AttributeLocalVariableTypeTable el) {
            //noop
        }

        @Override
        public void visitAttributeMethodParameters(AttributeMethodParameters el) {
            //noop
        }

        @Override
        public void visitAttributeModule(AttributeModule el) {
            //noop
        }

        @Override
        public void visitAttributeModuleMainClass(AttributeModuleMainClass el) {
            //noop
        }

        @Override
        public void visitAttributeModulePackages(AttributeModulePackages el) {
            //noop
        }

        @Override
        public void visitAttributeNestHost(AttributeNestHost el) {
            //noop
        }

        @Override
        public void visitAttributeNestMembers(AttributeNestMembers el) {
            //noop
        }

        @Override
        public void visitAttributePermittedSubclasses(AttributePermittedSubclasses el) {
            //noop
        }

        @Override
        public void visitAttributeRecord(AttributeRecord el) {
            //noop
        }

        @Override
        public void visitAttributeRuntimeInvisibleAnnotations(AttributeRuntimeInvisibleAnnotations el) {
            //noop
        }

        @Override
        public void visitAttributeRuntimeInvisibleParameterAnnotations(AttributeRuntimeInvisibleParameterAnnotations el) {
            //noop
        }

        @Override
        public void visitAttributeRuntimeInvisibleTypeAnnotations(AttributeRuntimeInvisibleTypeAnnotations el) {
            //noop
        }

        @Override
        public void visitAttributeRuntimeVisibleAnnotations(AttributeRuntimeVisibleAnnotations el) {
            //noop
        }

        @Override
        public void visitAttributeRuntimeVisibleParameterAnnotations(AttributeRuntimeVisibleParameterAnnotations el) {
            //noop
        }

        @Override
        public void visitAttributeRuntimeVisibleTypeAnnotations(AttributeRuntimeVisibleTypeAnnotations el) {
            //noop
        }

        @Override
        public void visitAttributeSignature(AttributeSignature el) {
            el.signature_index = getUtf8Index(cp, newCp, utf8Map, openSlots, Mappings.remapSignature(mappings, utf8(cp, el.signature_index)));
        }

        @Override
        public void visitAttributeSourceDebugExtension(AttributeSourceDebugExtension el) {
            //noop
        }

        @Override
        public void visitAttributeSourceFile(AttributeSourceFile el) {
            //noop
        }

        @Override
        public void visitAttributeStackMapTable(AttributeStackMapTable el) {
            //noop
        }

        @Override
        public void visitAttributeSynthetic(AttributeSynthetic el) {
            //noop
        }

        @Override
        public void visitEntryParameters(EntryParameters el) {
            //noop
        }

        @Override
        public void visitEntryExceptionTable(EntryExceptionTable el) {
            //noop
        }

        @Override
        public void visitEntryBootstrapMethods(EntryBootstrapMethods el) {
            //noop
        }

        @Override
        public void visitEntryParameterAnnotations(EntryParameterAnnotations el) {
            //noop
        }

        @Override
        public void visitEntryLocalVariableTypeTable(EntryLocalVariableTypeTable el) {
            el.signature_index = getUtf8Index(cp, newCp, utf8Map, openSlots, Mappings.remapSignature(mappings, utf8(cp, el.signature_index)));
        }

        @Override
        public void visitEntryProvides(EntryProvides el) {
            //noop
        }

        @Override
        public void visitEntryExports(EntryExports el) {
            //noop
        }

        @Override
        public void visitEntryClasses(EntryClasses el) {
            Mutf8Slice mapped = mappings.mapClass(cls2Utf8(cp, el.inner_class_info_index));
            ByteBuffer b = mapped.b;
            int i = b.limit();
            while (i > 0) {
                --i;
                if (b.get(i) == '$') {
                    ++i;
                    break;
                }
            }
            ByteBuffer b0 = ByteBufferUtil.slice(b, i, b.limit());
            Mutf8Slice m = new Mutf8Slice(b0);
            el.inner_name_index = getUtf8Index(cp, newCp, utf8Map, openSlots, m);
        }

        @Override
        public void visitEntryLineNumberTable(EntryLineNumberTable el) {
            //noop
        }

        @Override
        public void visitEntryLocalVariableTable(EntryLocalVariableTable el) {
            el.descriptor_index = getUtf8Index(cp, newCp, utf8Map, openSlots, Mappings.remapFieldDescriptor(mappings, utf8(cp, el.descriptor_index)));
        }

        @Override
        public void visitEntryOpens(EntryOpens el) {
            //noop
        }

        @Override
        public void visitEntryRequires(EntryRequires el) {
            //noop
        }

    }

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
                    DerefVisitor drefv = new DerefVisitor(cp, refs);
                    ci.accept(drefv);
                    // Build maps & freelist
                    Object2IntOpenHashMap<Mutf8Slice> utf8Map = new Object2IntOpenHashMap<>(drefv.utf8Size);
                    Object2IntOpenHashMap<NameDescPair> nameAndTypeMap = new Object2IntOpenHashMap<>(drefv.nameAndTypeSize);
                    IntArrayFIFOQueue openSlots = new IntArrayFIFOQueue(drefv.utf8Size + drefv.nameAndTypeSize); // TODO is this a reasonable sizing
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
                    RemapVisitor remapVisitor = new RemapVisitor(cp, newCp, mappings, utf8Map, nameAndTypeMap, openSlots);
                    ci.accept(remapVisitor);
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
