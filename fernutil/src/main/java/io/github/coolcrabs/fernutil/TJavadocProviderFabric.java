package io.github.coolcrabs.fernutil;

import org.jetbrains.java.decompiler.struct.StructClass;
import org.jetbrains.java.decompiler.struct.StructField;
import org.jetbrains.java.decompiler.struct.StructMethod;

import io.github.coolcrabs.fernutil.FernUtil.JavadocProvider;
import net.fabricmc.fernflower.api.IFabricJavadocProvider;

class TJavadocProviderFabric implements IFabricJavadocProvider {
    JavadocProvider provider;

    TJavadocProviderFabric(JavadocProvider provider) {
        this.provider = provider;
    }

    @Override
    public String getClassDoc(StructClass structClass) {
        return provider.clazzDoc(structClass.qualifiedName);
    }

    @Override
    public String getFieldDoc(StructClass structClass, StructField structField) {
        return provider.fieldDoc(structClass.qualifiedName, structField.getDescriptor(), structField.getName());
    }

    @Override
    public String getMethodDoc(StructClass structClass, StructMethod structMethod) {
        return provider.methodDoc(structClass.qualifiedName, structMethod.getDescriptor(), structMethod.getName());
    }
    
}
