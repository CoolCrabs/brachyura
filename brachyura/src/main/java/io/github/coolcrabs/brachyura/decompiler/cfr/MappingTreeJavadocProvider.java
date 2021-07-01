package io.github.coolcrabs.brachyura.decompiler.cfr;

import org.tinylog.Logger;

import io.github.coolcrabs.cfr.api.BrachyuraCFRJavadocProvider;
import net.fabricmc.mappingio.tree.MappingTree;
import net.fabricmc.mappingio.tree.MappingTree.ClassMapping;
import net.fabricmc.mappingio.tree.MappingTree.FieldMapping;
import net.fabricmc.mappingio.tree.MappingTree.MethodMapping;

class MappingTreeJavadocProvider implements BrachyuraCFRJavadocProvider {
    private final MappingTree tree;
    private final int namespace;

    public MappingTreeJavadocProvider(MappingTree tree, int namespace) {
        this.tree = tree;
        this.namespace = namespace;
    }

    @Override
    public String getClassJavadoc(String clazz) {
        ClassMapping classMapping = tree.getClass(clazz, namespace);
        return classMapping == null ? null : classMapping.getComment();
    }

    @Override
    public String getMethodJavadoc(String clazz, String signature, String methodName) {
        ClassMapping classMapping = tree.getClass(clazz, namespace);
        if (classMapping != null) {
            MethodMapping methodMapping = classMapping.getMethod(methodName, signature, namespace);
            if (methodMapping != null) {
                return methodMapping.getComment();
            }
        }
        return null;
    }

    @Override
    public String getFieldJavadoc(String clazz, String signature, String fieldName) {
        ClassMapping classMapping = tree.getClass(clazz, namespace);
        if (classMapping != null) {
            FieldMapping fieldMapping = classMapping.getField(fieldName, signature, namespace);
            if (fieldMapping != null) {
                return fieldMapping.getComment();
            }
        }
        return null;
    }
    
}
