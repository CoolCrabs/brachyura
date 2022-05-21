package io.github.coolcrabs.accesswidener;

import java.util.Set;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import io.github.coolcrabs.accesswidener.AccessWidener.ClassAwData;
import io.github.coolcrabs.accesswidener.AccessWidenerReader.AccessType;

// See https://github.com/FabricMC/access-widener/blob/master/src/main/java/io.github.coolcrabs.accesswidener/AccessWidenerClassVisitor.java
public class AccessWidenerClassVisitor extends ClassVisitor {
    final AccessWidener aw;
    private ClassAwData clsData;
    private int classAccess;

    enum WidenedType {
        CLAZZ,
        METHOD,
        FIELD
    }

    static int widen(int access, String targetName, int ownerAccess, Set<AccessType> wtypes, WidenedType type) {
        int result = access;
        if (wtypes.contains(AccessType.ACCESSIBLE)) {
            result = makePublic(result);
            // makeFinalIfPrivate
            if (
                type == WidenedType.METHOD &&
                !targetName.equals("<init>") &&
                (access & Opcodes.ACC_PRIVATE) != 0 &&
                !((ownerAccess & Opcodes.ACC_INTERFACE) != 0 || (access & Opcodes.ACC_STATIC) != 0)
            ) {
                access |= Opcodes.ACC_FINAL;
            }
        }
        if (wtypes.contains(AccessType.EXTENDABLE)) {
            if (type == WidenedType.CLAZZ) {
                result = makePublic(result);
                
            } else if (type == WidenedType.METHOD && (result & Opcodes.ACC_PUBLIC) != 0) {
                result = (result & ~Opcodes.ACC_PRIVATE) | Opcodes.ACC_PROTECTED;
            }
            result &= ~Opcodes.ACC_FINAL;
        }
        if (wtypes.contains(AccessType.MUTABLE)) {
            if ((ownerAccess & Opcodes.ACC_INTERFACE) != 0 && (access & Opcodes.ACC_STATIC) != 0) {
                throw new IllegalArgumentException("Cannot make static interface field mutable");
            }
            result &= ~Opcodes.ACC_FINAL;
        }
        return result;
    }

    static int makePublic(int acc) {
        return (acc & ~(Opcodes.ACC_PRIVATE | Opcodes.ACC_PROTECTED)) | Opcodes.ACC_PUBLIC;
    }

    public AccessWidenerClassVisitor(int api, ClassVisitor classVisitor, AccessWidener aw) {
        super(api, classVisitor);
        this.aw = aw;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        clsData = aw.clsMap.get(name);
        classAccess = access;
        super.visit(version, clsData == null ? access : widen(access, name, classAccess, clsData.access, WidenedType.CLAZZ), name, signature, superName, interfaces);
    }

    @Override
    public void visitPermittedSubclass(String permittedSubclass) {
        if (!(clsData != null && clsData.access.contains(AccessType.EXTENDABLE))) {
            super.visitPermittedSubclass(permittedSubclass);
        }
    }

    @Override
    public void visitInnerClass(String name, String outerName, String innerName, int access) {
        ClassAwData icd = aw.clsMap.get(name);
        super.visitInnerClass(name, outerName, innerName, icd == null ? access : widen(access, name, classAccess, icd.access, WidenedType.CLAZZ));
    }

    @Override
    public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
        Set<AccessType> w = clsData == null ? null : clsData.fields.get(new AccessWidener.Member(name, descriptor));
        return super.visitField(w == null ? access : widen(access, name, classAccess, w, WidenedType.FIELD), name, descriptor, signature, value); 
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        Set<AccessType> w = clsData == null ? null : clsData.methods.get(new AccessWidener.Member(name, descriptor));
        if (w == null) {
            return super.visitMethod(access, name, descriptor, signature, exceptions);
        }
        int newAccess = widen(access, name, classAccess, w, WidenedType.METHOD);
        return new MethodVisitor(AccessWidenerClassVisitor.this.api, super.visitMethod(newAccess, name, descriptor, signature, exceptions)) {
            @Override
            public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
                if (opcode == Opcodes.INVOKESPECIAL && !name.equals("<init>")) {
                    opcode = Opcodes.INVOKEVIRTUAL;
                }
                super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
            }
        };
    }
    
}
