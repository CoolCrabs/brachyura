package io.github.coolcrabs.brachyura.recombobulator.util;

import java.nio.ByteBuffer;

import io.github.coolcrabs.brachyura.recombobulator.ByteBufferUtil;
import io.github.coolcrabs.brachyura.recombobulator.ClassDecodeException;
import io.github.coolcrabs.brachyura.recombobulator.ClassInfo;
import io.github.coolcrabs.brachyura.recombobulator.ConstantClass;
import io.github.coolcrabs.brachyura.recombobulator.ConstantDouble;
import io.github.coolcrabs.brachyura.recombobulator.ConstantDynamic;
import io.github.coolcrabs.brachyura.recombobulator.ConstantFieldref;
import io.github.coolcrabs.brachyura.recombobulator.ConstantInterfaceMethodref;
import io.github.coolcrabs.brachyura.recombobulator.ConstantInvokeDynamic;
import io.github.coolcrabs.brachyura.recombobulator.ConstantLong;
import io.github.coolcrabs.brachyura.recombobulator.ConstantMethodHandle;
import io.github.coolcrabs.brachyura.recombobulator.ConstantMethodType;
import io.github.coolcrabs.brachyura.recombobulator.ConstantMethodref;
import io.github.coolcrabs.brachyura.recombobulator.ConstantModule;
import io.github.coolcrabs.brachyura.recombobulator.ConstantNameAndType;
import io.github.coolcrabs.brachyura.recombobulator.ConstantPackage;
import io.github.coolcrabs.brachyura.recombobulator.ConstantPool;
import io.github.coolcrabs.brachyura.recombobulator.ConstantPoolEntry;
import io.github.coolcrabs.brachyura.recombobulator.ConstantString;
import io.github.coolcrabs.brachyura.recombobulator.ConstantUtf8;
import io.github.coolcrabs.brachyura.recombobulator.FieldInfo;
import io.github.coolcrabs.brachyura.recombobulator.MethodInfo;
import io.github.coolcrabs.brachyura.recombobulator.RecombobulatorVisitor;
import io.github.coolcrabs.brachyura.recombobulator.U2Slice;
import io.github.coolcrabs.brachyura.recombobulator.attribute.Annotation;
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
import io.github.coolcrabs.brachyura.recombobulator.remapper.NameDescPair;
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

public class ConstantPoolRefCounter implements RecombobulatorVisitor {
    ConstantPool pool;
    int[] refconts;

    public ConstantPoolRefCounter(ConstantPool pool) {
        this.pool = pool;
        refconts = new int[pool.size()];
    }

    public int getRef(int poolIndex) {
        return refconts[poolIndex - 1];
    }

    public void ref(int poolIndex) {
        ref(poolIndex, 1);
    }

    /**
     * Ref/Deref pool index (and referenced entries)
     */
    public void ref(int poolIndex, int amount) {
        if (poolIndex == 0) return; // 0 is used as null in some attributes
        refconts[poolIndex - 1] += amount;
        ConstantPoolEntry e = pool.getEntry(poolIndex);
        if (e instanceof ConstantClass) {
            ConstantClass o = (ConstantClass) e;
            ref(o.name_index, amount);
        } else if (e instanceof ConstantDynamic) {
            ConstantDynamic o = (ConstantDynamic) e;
            // bootstrap_method_attr_index is in bootstrap table not cp
            ref(o.name_and_type_index, amount);
        } else if (e instanceof ConstantFieldref) {
            ConstantFieldref o = (ConstantFieldref) e;
            ref(o.class_index, amount);
            ref(o.name_and_type_index, amount);
        } else if (e instanceof ConstantInterfaceMethodref) {
            ConstantInterfaceMethodref o = (ConstantInterfaceMethodref) e;
            ref(o.class_index, amount);
            ref(o.name_and_type_index, amount);
        } else if (e instanceof ConstantInvokeDynamic) {
            ConstantInvokeDynamic o = (ConstantInvokeDynamic) e;
            // bootstrap_method_attr_index is in bootstrap table not cp
            ref(o.name_and_type_index, amount);
        } else if (e instanceof ConstantMethodHandle) {
            ConstantMethodHandle o = (ConstantMethodHandle) e;
            ref(o.reference_index, amount);
        } else if (e instanceof ConstantMethodref) {
            ConstantMethodref o = (ConstantMethodref) e;
            ref(o.class_index, amount);
            ref(o.name_and_type_index, amount);
        } else if (e instanceof ConstantMethodType) {
            ConstantMethodType o = (ConstantMethodType) e;
            ref(o.descriptor_index, amount);
        } else if (e instanceof ConstantModule) {
            ConstantModule o = (ConstantModule) e;
            ref(o.name_index, amount);
        } else if (e instanceof ConstantNameAndType) {
            ConstantNameAndType o = (ConstantNameAndType) e;
            ref(o.descriptor_index, amount);
            ref(o.name_index, amount);
        } else if (e instanceof ConstantPackage) {
            ConstantPackage o = (ConstantPackage) e;
            ref(o.name_index, amount);
        } else if (e instanceof ConstantString) {
            ConstantString o = (ConstantString) e;
            ref(o.string_index, amount);
        } else if (e instanceof ConstantDouble || e instanceof ConstantLong) {
            ref(poolIndex + 1, amount);
        }
    }

    @Override
    public void visitClassInfo(ClassInfo el) {
        ref(el.this_class);
        ref(el.super_class);
        for (int i : el.interfaces) {
            ref(i);
        }
    }

    MethodInfo meth;

    @Override
    public void visitMethodInfo(MethodInfo el) {
        ref(el.name_index);
        ref(el.descriptor_index);
        meth = el;
    }

    @Override
    public void visitFieldInfo(FieldInfo el) {
        ref(el.name_index);
        ref(el.descriptor_index);
    }

    @Override
    public void visitRecordComponentInfo(RecordComponentInfo el) {
        ref(el.name_index);
        ref(el.descriptor_index);
    }

    @Override
    public void visitAnnotation(Annotation el) {
        ref(el.type_index);
    }

    @Override
    public void visitElementValuePair(ElementValuePair el) {
        ref(el.element_name_index);
    }

    @Override
    public void visitElementValueConst(ElementValueConst el) {
        ref(el.const_value_index);
    }

    @Override
    public void visitElementValueEnum(ElementValueEnum el) {
        ref(el.const_name_index);
        ref(el.type_name_index);
    }

    @Override
    public void visitElementValueClass(ElementValueClass el) {
        ref(el.class_info_index);
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
        ref(el.type_index);
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
        ref(el.cpool_index);
    }

    @Override
    public void visitVerificationTypeUninitialized(VerificationTypeUninitialized el) {
        //noop
    }

    @Override
    public void visitAttributeUnknown(AttributeUnknown el) {
        ref(el.attribute_name_index);
    }

    @Override
    public void visitAttributeAnnotationDefault(AttributeAnnotationDefault el) {
        ref(el.attribute_name_index);
    }

    @Override
    public void visitAttributeBootstrapMethods(AttributeBootstrapMethods el) {
        ref(el.attribute_name_index);
    }

    @Override
    public void visitAttributeCode(AttributeCode el) {
        ref(el.attribute_name_index);
        ByteBuffer b = el.code;
        int offset = b.position();
        int pos = 0;
        int end = b.remaining();
        while (pos < end) {
            byte opcode = b.get(offset + pos);
            // String ophex = Integer.toHexString(opcode & 0xFF);
            pos += 1;
            switch (opcode) {
                case (byte) 0xAA:
                    pos = (pos + 3) & -4; // Round to nearest power of 4 https://stackoverflow.com/a/9194117
                    pos += 4; // int default
                    int low = b.getInt(offset + pos);
                    pos += 4;
                    int high = b.getInt(offset + pos);
                    pos += 4;
                    pos += (high - low + 1) * 4;
                    break;
                case (byte) 0xAB:
                    pos = (pos + 3) & -4; // Round to nearest power of 4 https://stackoverflow.com/a/9194117
                    pos += 4; // int default
                    int npairs = b.getInt(offset + pos);
                    pos += 4;
                    pos += 8 * npairs;
                    break;
                case (byte) 0xB9:
                case (byte) 0xBA:
                    {
                        int index = ByteBufferUtil.u2(b, offset + pos);
                        ref(index);
                        pos += 2;
                        pos += 2; // count + 0 or 0 + 0
                    }
                    break;
                case (byte) 0xC8:
                case (byte) 0xC9:
                    pos += 4;
                    break;
                case (byte) 0xC4: // wide
                    byte op = b.get(offset + pos);
                    pos += 1;
                    if (op == (byte) 0x84) {
                        pos += 4;
                    } else {
                        pos += 2;
                    }
                    break;
                case (byte) 0xC5:
                    {
                        int index = ByteBufferUtil.u2(b, offset + pos);
                        ref(index);
                        pos += 2;
                        pos += 1; // dimensions
                    }
                    break;
                case (byte) 0xBD:
                case (byte) 0xC0:
                case (byte) 0xB4:
                case (byte) 0xB2:
                case (byte) 0xC1:
                case (byte) 0xB7:
                case (byte) 0xB8:
                case (byte) 0xB6:
                case (byte) 0x13:
                case (byte) 0x14:
                case (byte) 0xBB:
                case (byte) 0xB5:
                case (byte) 0xB3:
                    {
                        int index = ByteBufferUtil.u2(b, offset + pos);
                        ref(index);
                        pos += 2;
                    }
                    break;
                case (byte) 0x12:
                    {
                        int index = ByteBufferUtil.u1(b, offset + pos);
                        ref(index);
                        pos += 1;
                    }
                    break;
                case (byte) 0x84:
                case (byte) 0x11:
                case (byte) 0xA7:
                case (byte) 0xA5:
                case (byte) 0xA6:
                case (byte) 0x9F:
                case (byte) 0xA2:
                case (byte) 0xA3:
                case (byte) 0xA4:
                case (byte) 0xA1:
                case (byte) 0xA0:
                case (byte) 0x99:
                case (byte) 0x9C:
                case (byte) 0x9D:
                case (byte) 0x9E:
                case (byte) 0x9B:
                case (byte) 0x9A:
                case (byte) 0xC7:
                case (byte) 0xC6:
                case (byte) 0xA8:
                    pos += 2;
                    break;
                case (byte) 0x19:
                case (byte) 0x3A:
                case (byte) 0x18:
                case (byte) 0x39:
                case (byte) 0x17:
                case (byte) 0x38:
                case (byte) 0x15:
                case (byte) 0x36:
                case (byte) 0x16:
                case (byte) 0x37:
                case (byte) 0xA9:
                case (byte) 0x10:
                case (byte) 0xBC:
                    pos += 1;
                    break;
                case (byte) 0x32:
                case (byte) 0x53:
                case (byte) 0x1:
                case (byte) 0x2A:
                case (byte) 0x2B:
                case (byte) 0x2C:
                case (byte) 0x2D:
                case (byte) 0xB0:
                case (byte) 0xBE:
                case (byte) 0x4B:
                case (byte) 0x4C:
                case (byte) 0x4D:
                case (byte) 0x4E:
                case (byte) 0xBF:
                case (byte) 0x33:
                case (byte) 0x54:
                case (byte) 0xCA:
                case (byte) 0x34:
                case (byte) 0x55:
                case (byte) 0x90:
                case (byte) 0x8E:
                case (byte) 0x8F:
                case (byte) 0x63:
                case (byte) 0x31:
                case (byte) 0x52:
                case (byte) 0x98:
                case (byte) 0x97:
                case (byte) 0x0E:
                case (byte) 0x0F:
                case (byte) 0x6F:
                case (byte) 0x26:
                case (byte) 0x27:
                case (byte) 0x28:
                case (byte) 0x29:
                case (byte) 0x6B:
                case (byte) 0x77:
                case (byte) 0x73:
                case (byte) 0xAF:
                case (byte) 0x47:
                case (byte) 0x48:
                case (byte) 0x49:
                case (byte) 0x4A:
                case (byte) 0x67:
                case (byte) 0x59:
                case (byte) 0x5A:
                case (byte) 0x5B:
                case (byte) 0x5C:
                case (byte) 0x5D:
                case (byte) 0x5E:
                case (byte) 0x8D:
                case (byte) 0x8B:
                case (byte) 0x8C:
                case (byte) 0x62:
                case (byte) 0x30:
                case (byte) 0x51:
                case (byte) 0x96:
                case (byte) 0x95:
                case (byte) 0x0B:
                case (byte) 0x0C:
                case (byte) 0x0D:
                case (byte) 0x6E:
                case (byte) 0x22:
                case (byte) 0x23:
                case (byte) 0x24:
                case (byte) 0x25:
                case (byte) 0x6A:
                case (byte) 0x76:
                case (byte) 0x72:
                case (byte) 0xAE:
                case (byte) 0x43:
                case (byte) 0x44:
                case (byte) 0x45:
                case (byte) 0x46:
                case (byte) 0x66:
                case (byte) 0x91:
                case (byte) 0x92:
                case (byte) 0x87:
                case (byte) 0x86:
                case (byte) 0x85:
                case (byte) 0x93:
                case (byte) 0x60:
                case (byte) 0x2E:
                case (byte) 0x7E:
                case (byte) 0x4F:
                case (byte) 0x2:
                case (byte) 0x3:
                case (byte) 0x4:
                case (byte) 0x5:
                case (byte) 0x6:
                case (byte) 0x7:
                case (byte) 0x8:
                case (byte) 0x6C:
                case (byte) 0x1A:
                case (byte) 0x1B:
                case (byte) 0x1C:
                case (byte) 0x1D:
                case (byte) 0xFE:
                case (byte) 0xFF:
                case (byte) 0x68:
                case (byte) 0x74:
                case (byte) 0x80:
                case (byte) 0x70:
                case (byte) 0xAC:
                case (byte) 0x78:
                case (byte) 0x7A:
                case (byte) 0x3B:
                case (byte) 0x3C:
                case (byte) 0x3D:
                case (byte) 0x3E:
                case (byte) 0x64:
                case (byte) 0x7C:
                case (byte) 0x82:
                case (byte) 0x8A:
                case (byte) 0x89:
                case (byte) 0x88:
                case (byte) 0x61:
                case (byte) 0x2F:
                case (byte) 0x7F:
                case (byte) 0x50:
                case (byte) 0x94:
                case (byte) 0x9:
                case (byte) 0x0A:
                case (byte) 0x6D:
                case (byte) 0x1E:
                case (byte) 0x1F:
                case (byte) 0x20:
                case (byte) 0x21:
                case (byte) 0x69:
                case (byte) 0x75:
                case (byte) 0x81:
                case (byte) 0x71:
                case (byte) 0xAD:
                case (byte) 0x79:
                case (byte) 0x7B:
                case (byte) 0x3F:
                case (byte) 0x40:
                case (byte) 0x41:
                case (byte) 0x42:
                case (byte) 0x65:
                case (byte) 0x7D:
                case (byte) 0x83:
                case (byte) 0xC2:
                case (byte) 0xC3:
                case (byte) 0x0:
                case (byte) 0x57:
                case (byte) 0x58:
                case (byte) 0xB1:
                case (byte) 0x35:
                case (byte) 0x56:
                case (byte) 0x5F:                    
                    break;
                default:
                    throw new ClassDecodeException("Unknown opcode 0x" + Integer.toHexString(opcode & 0xFF) + " at position " + (pos - 1) + " in method " + new NameDescPair(((ConstantUtf8)pool.getEntry(meth.name_index)).slice, ((ConstantUtf8)pool.getEntry(meth.descriptor_index)).slice));
            }
        }
    }

    @Override
    public void visitAttributeConstantValue(AttributeConstantValue el) {
        ref(el.attribute_name_index);
        ref(el.constantvalue_index);
    }

    @Override
    public void visitAttributeDeprecated(AttributeDeprecated el) {
        ref(el.attribute_name_index);
    }

    @Override
    public void visitAttributeEnclosingMethod(AttributeEnclosingMethod el) {
        ref(el.attribute_name_index);
        ref(el.class_index);
        ref(el.method_index);
    }

    @Override
    public void visitAttributeExceptions(AttributeExceptions el) {
        ref(el.attribute_name_index);
        for (int i = 0; i < el.exception_index_table.size(); i++) {
            ref(el.exception_index_table.get(i));
        }
    }

    @Override
    public void visitAttributeInnerClasses(AttributeInnerClasses el) {
        ref(el.attribute_name_index);
    }

    @Override
    public void visitAttributeLineNumberTable(AttributeLineNumberTable el) {
        ref(el.attribute_name_index);
    }

    @Override
    public void visitAttributeLocalVariableTable(AttributeLocalVariableTable el) {
        ref(el.attribute_name_index);
    }

    @Override
    public void visitAttributeLocalVariableTypeTable(AttributeLocalVariableTypeTable el) {
        ref(el.attribute_name_index);
    }

    @Override
    public void visitAttributeMethodParameters(AttributeMethodParameters el) {
        ref(el.attribute_name_index);
    }

    @Override
    public void visitAttributeModule(AttributeModule el) {
        ref(el.attribute_name_index);
        ref(el.module_name_index);
        ref(el.module_version_index);
        U2Slice uses_index = el.uses_index;
        for (int i = 0; i < uses_index.size(); i++) {
            ref(uses_index.get(i));
        }
    }

    @Override
    public void visitAttributeModuleMainClass(AttributeModuleMainClass el) {
        ref(el.attribute_name_index);
        ref(el.main_class_index);
    }

    @Override
    public void visitAttributeModulePackages(AttributeModulePackages el) {
        ref(el.attribute_name_index);
        U2Slice package_index = el.package_index;
        for (int i = 0; i < package_index.size(); i++) {
            ref(package_index.get(i));
        }
    }

    @Override
    public void visitAttributeNestHost(AttributeNestHost el) {
        ref(el.attribute_name_index);
        ref(el.host_class_index);
    }

    @Override
    public void visitAttributeNestMembers(AttributeNestMembers el) {
        ref(el.attribute_name_index);
        U2Slice classes = el.classes;
        for (int i = 0; i < classes.size(); i++) {
            ref(classes.get(i));
        }
    }

    @Override
    public void visitAttributePermittedSubclasses(AttributePermittedSubclasses el) {
        ref(el.attribute_name_index);
        U2Slice classes = el.classes;
        for (int i = 0; i < classes.size(); i++) {
            ref(classes.get(i));
        }
    }

    @Override
    public void visitAttributeRecord(AttributeRecord el) {
        ref(el.attribute_name_index);
    }

    @Override
    public void visitAttributeRuntimeInvisibleAnnotations(AttributeRuntimeInvisibleAnnotations el) {
        ref(el.attribute_name_index);
    }

    @Override
    public void visitAttributeRuntimeInvisibleParameterAnnotations(AttributeRuntimeInvisibleParameterAnnotations el) {
        ref(el.attribute_name_index);
    }

    @Override
    public void visitAttributeRuntimeInvisibleTypeAnnotations(AttributeRuntimeInvisibleTypeAnnotations el) {
        ref(el.attribute_name_index);
    }

    @Override
    public void visitAttributeRuntimeVisibleAnnotations(AttributeRuntimeVisibleAnnotations el) {
        ref(el.attribute_name_index);
    }

    @Override
    public void visitAttributeRuntimeVisibleParameterAnnotations(AttributeRuntimeVisibleParameterAnnotations el) {
        ref(el.attribute_name_index);
    }

    @Override
    public void visitAttributeRuntimeVisibleTypeAnnotations(AttributeRuntimeVisibleTypeAnnotations el) {
        ref(el.attribute_name_index);
    }

    @Override
    public void visitAttributeSignature(AttributeSignature el) {
        ref(el.attribute_name_index);
        ref(el.signature_index);
    }

    @Override
    public void visitAttributeSourceDebugExtension(AttributeSourceDebugExtension el) {
        ref(el.attribute_name_index);
    }

    @Override
    public void visitAttributeSourceFile(AttributeSourceFile el) {
        ref(el.attribute_name_index);
        ref(el.sourcefile_index);
    }

    @Override
    public void visitAttributeStackMapTable(AttributeStackMapTable el) {
        ref(el.attribute_name_index);
    }

    @Override
    public void visitAttributeSynthetic(AttributeSynthetic el) {
        ref(el.attribute_name_index);
    }

    @Override
    public void visitEntryParameters(EntryParameters el) {
        ref(el.name_index);
    }

    @Override
    public void visitEntryExceptionTable(EntryExceptionTable el) {
        ref(el.catch_type);
    }

    @Override
    public void visitEntryBootstrapMethods(EntryBootstrapMethods el) {
        ref(el.bootstrap_method_ref);
        U2Slice bootstrap_arguments = el.bootstrap_arguments;
        for (int i = 0; i < bootstrap_arguments.size(); i++) {
            ref(bootstrap_arguments.get(i));
        }
    }

    @Override
    public void visitEntryParameterAnnotations(EntryParameterAnnotations el) {
        //noop
    }

    @Override
    public void visitEntryLocalVariableTypeTable(EntryLocalVariableTypeTable el) {
        ref(el.name_index);
        ref(el.signature_index);
    }

    @Override
    public void visitEntryProvides(EntryProvides el) {
        ref(el.provides_index);
        U2Slice provides_with_index = el.provides_with_index;
        for (int i = 0; i < provides_with_index.size(); i++) {
            ref(provides_with_index.get(i));
        }
    }

    @Override
    public void visitEntryExports(EntryExports el) {
        ref(el.exports_index);
        U2Slice exports_to_index = el.exports_to_index;
        for (int i = 0; i < exports_to_index.size(); i++) {
            ref(exports_to_index.get(i));
        }
    }

    @Override
    public void visitEntryClasses(EntryClasses el) {
        ref(el.inner_class_info_index);
        ref(el.outer_class_info_index);
        ref(el.inner_name_index);
    }

    @Override
    public void visitEntryLineNumberTable(EntryLineNumberTable el) {
        //noop
    }

    @Override
    public void visitEntryLocalVariableTable(EntryLocalVariableTable el) {
        ref(el.name_index);
        ref(el.descriptor_index);
    }

    @Override
    public void visitEntryOpens(EntryOpens el) {
        ref(el.opens_index);
        U2Slice opens_to_index = el.opens_to_index;
        for (int i = 0; i < opens_to_index.size(); i++) {
            ref(opens_to_index.get(i));
        }
    }

    @Override
    public void visitEntryRequires(EntryRequires el) {
        ref(el.requires_index);
        ref(el.requires_version_index);
    }
}
