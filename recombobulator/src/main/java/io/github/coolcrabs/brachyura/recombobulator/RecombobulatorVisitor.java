package io.github.coolcrabs.brachyura.recombobulator;

// GENERATED CLASS :)

import io.github.coolcrabs.brachyura.recombobulator.attribute.RecordComponentInfo;
import io.github.coolcrabs.brachyura.recombobulator.attribute.Annotation;
import io.github.coolcrabs.brachyura.recombobulator.attribute.ElementValuePair;
import io.github.coolcrabs.brachyura.recombobulator.attribute.ElementValueConst;
import io.github.coolcrabs.brachyura.recombobulator.attribute.ElementValueEnum;
import io.github.coolcrabs.brachyura.recombobulator.attribute.ElementValueClass;
import io.github.coolcrabs.brachyura.recombobulator.attribute.ElementValueAnnotation;
import io.github.coolcrabs.brachyura.recombobulator.attribute.ElementValueArray;
import io.github.coolcrabs.brachyura.recombobulator.attribute.TypeAnnotation;
import io.github.coolcrabs.brachyura.recombobulator.attribute.TargetTypeParameter;
import io.github.coolcrabs.brachyura.recombobulator.attribute.TargetSupertype;
import io.github.coolcrabs.brachyura.recombobulator.attribute.TargetTypeParameterBound;
import io.github.coolcrabs.brachyura.recombobulator.attribute.TargetEmpty;
import io.github.coolcrabs.brachyura.recombobulator.attribute.TargetFormalParameter;
import io.github.coolcrabs.brachyura.recombobulator.attribute.TargetThrows;
import io.github.coolcrabs.brachyura.recombobulator.attribute.TargetLocalvar;
import io.github.coolcrabs.brachyura.recombobulator.attribute.TargetLocalvar.LocalvarTableEntry;
import io.github.coolcrabs.brachyura.recombobulator.attribute.TargetCatch;
import io.github.coolcrabs.brachyura.recombobulator.attribute.TargetOffset;
import io.github.coolcrabs.brachyura.recombobulator.attribute.TargetTypeArgument;
import io.github.coolcrabs.brachyura.recombobulator.attribute.TypePath;
import io.github.coolcrabs.brachyura.recombobulator.attribute.EntryPath;
import io.github.coolcrabs.brachyura.recombobulator.attribute.SMFrameSame;
import io.github.coolcrabs.brachyura.recombobulator.attribute.SMFrameSameLocals1StackItem;
import io.github.coolcrabs.brachyura.recombobulator.attribute.SMFrameExtendedSameLocals1StackItem;
import io.github.coolcrabs.brachyura.recombobulator.attribute.SMFrameChop;
import io.github.coolcrabs.brachyura.recombobulator.attribute.SMFrameExtendedSame;
import io.github.coolcrabs.brachyura.recombobulator.attribute.SMFrameAppend;
import io.github.coolcrabs.brachyura.recombobulator.attribute.SMFrameFull;
import io.github.coolcrabs.brachyura.recombobulator.attribute.VerificationTypeNonreference;
import io.github.coolcrabs.brachyura.recombobulator.attribute.VerificationTypeObject;
import io.github.coolcrabs.brachyura.recombobulator.attribute.VerificationTypeUninitialized;
import io.github.coolcrabs.brachyura.recombobulator.attribute.AttributeUnknown;
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
import io.github.coolcrabs.brachyura.recombobulator.attribute.EntryParameters;
import io.github.coolcrabs.brachyura.recombobulator.attribute.EntryExceptionTable;
import io.github.coolcrabs.brachyura.recombobulator.attribute.EntryBootstrapMethods;
import io.github.coolcrabs.brachyura.recombobulator.attribute.EntryParameterAnnotations;
import io.github.coolcrabs.brachyura.recombobulator.attribute.EntryLocalVariableTypeTable;
import io.github.coolcrabs.brachyura.recombobulator.attribute.EntryProvides;
import io.github.coolcrabs.brachyura.recombobulator.attribute.EntryExports;
import io.github.coolcrabs.brachyura.recombobulator.attribute.EntryClasses;
import io.github.coolcrabs.brachyura.recombobulator.attribute.EntryLineNumberTable;
import io.github.coolcrabs.brachyura.recombobulator.attribute.EntryLocalVariableTable;
import io.github.coolcrabs.brachyura.recombobulator.attribute.EntryOpens;
import io.github.coolcrabs.brachyura.recombobulator.attribute.EntryRequires;

public interface RecombobulatorVisitor {
    void visitClassInfo(ClassInfo el);
    void visitMethodInfo(MethodInfo el);
    void visitFieldInfo(FieldInfo el);
    void visitRecordComponentInfo(RecordComponentInfo el);
    void visitAnnotation(Annotation el);
    void visitElementValuePair(ElementValuePair el);
    void visitElementValueConst(ElementValueConst el);
    void visitElementValueEnum(ElementValueEnum el);
    void visitElementValueClass(ElementValueClass el);
    void visitElementValueAnnotation(ElementValueAnnotation el);
    void visitElementValueArray(ElementValueArray el);
    void visitTypeAnnotation(TypeAnnotation el);
    void visitTargetTypeParameter(TargetTypeParameter el);
    void visitTargetSupertype(TargetSupertype el);
    void visitTargetTypeParameterBound(TargetTypeParameterBound el);
    void visitTargetEmpty(TargetEmpty el);
    void visitTargetFormalParameter(TargetFormalParameter el);
    void visitTargetThrows(TargetThrows el);
    void visitTargetLocalvar(TargetLocalvar el);
    void visitLocalvarTableEntry(LocalvarTableEntry el);
    void visitTargetCatch(TargetCatch el);
    void visitTargetOffset(TargetOffset el);
    void visitTargetTypeArgument(TargetTypeArgument el);
    void visitTypePath(TypePath el);
    void visitEntryPath(EntryPath el);
    void visitSMFrameSame(SMFrameSame el);
    void visitSMFrameSameLocals1StackItem(SMFrameSameLocals1StackItem el);
    void visitSMFrameExtendedSameLocals1StackItem(SMFrameExtendedSameLocals1StackItem el);
    void visitSMFrameChop(SMFrameChop el);
    void visitSMFrameExtendedSame(SMFrameExtendedSame el);
    void visitSMFrameAppend(SMFrameAppend el);
    void visitSMFrameFull(SMFrameFull el);
    void visitVerificationTypeNonreference(VerificationTypeNonreference el);
    void visitVerificationTypeObject(VerificationTypeObject el);
    void visitVerificationTypeUninitialized(VerificationTypeUninitialized el);
    void visitAttributeUnknown(AttributeUnknown el);
    void visitAttributeAnnotationDefault(AttributeAnnotationDefault el);
    void visitAttributeBootstrapMethods(AttributeBootstrapMethods el);
    void visitAttributeCode(AttributeCode el);
    void visitAttributeConstantValue(AttributeConstantValue el);
    void visitAttributeDeprecated(AttributeDeprecated el);
    void visitAttributeEnclosingMethod(AttributeEnclosingMethod el);
    void visitAttributeExceptions(AttributeExceptions el);
    void visitAttributeInnerClasses(AttributeInnerClasses el);
    void visitAttributeLineNumberTable(AttributeLineNumberTable el);
    void visitAttributeLocalVariableTable(AttributeLocalVariableTable el);
    void visitAttributeLocalVariableTypeTable(AttributeLocalVariableTypeTable el);
    void visitAttributeMethodParameters(AttributeMethodParameters el);
    void visitAttributeModule(AttributeModule el);
    void visitAttributeModuleMainClass(AttributeModuleMainClass el);
    void visitAttributeModulePackages(AttributeModulePackages el);
    void visitAttributeNestHost(AttributeNestHost el);
    void visitAttributeNestMembers(AttributeNestMembers el);
    void visitAttributePermittedSubclasses(AttributePermittedSubclasses el);
    void visitAttributeRecord(AttributeRecord el);
    void visitAttributeRuntimeInvisibleAnnotations(AttributeRuntimeInvisibleAnnotations el);
    void visitAttributeRuntimeInvisibleParameterAnnotations(AttributeRuntimeInvisibleParameterAnnotations el);
    void visitAttributeRuntimeInvisibleTypeAnnotations(AttributeRuntimeInvisibleTypeAnnotations el);
    void visitAttributeRuntimeVisibleAnnotations(AttributeRuntimeVisibleAnnotations el);
    void visitAttributeRuntimeVisibleParameterAnnotations(AttributeRuntimeVisibleParameterAnnotations el);
    void visitAttributeRuntimeVisibleTypeAnnotations(AttributeRuntimeVisibleTypeAnnotations el);
    void visitAttributeSignature(AttributeSignature el);
    void visitAttributeSourceDebugExtension(AttributeSourceDebugExtension el);
    void visitAttributeSourceFile(AttributeSourceFile el);
    void visitAttributeStackMapTable(AttributeStackMapTable el);
    void visitAttributeSynthetic(AttributeSynthetic el);
    void visitEntryParameters(EntryParameters el);
    void visitEntryExceptionTable(EntryExceptionTable el);
    void visitEntryBootstrapMethods(EntryBootstrapMethods el);
    void visitEntryParameterAnnotations(EntryParameterAnnotations el);
    void visitEntryLocalVariableTypeTable(EntryLocalVariableTypeTable el);
    void visitEntryProvides(EntryProvides el);
    void visitEntryExports(EntryExports el);
    void visitEntryClasses(EntryClasses el);
    void visitEntryLineNumberTable(EntryLineNumberTable el);
    void visitEntryLocalVariableTable(EntryLocalVariableTable el);
    void visitEntryOpens(EntryOpens el);
    void visitEntryRequires(EntryRequires el);
}
