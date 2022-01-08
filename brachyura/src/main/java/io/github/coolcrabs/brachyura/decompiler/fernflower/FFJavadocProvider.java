package io.github.coolcrabs.brachyura.decompiler.fernflower;

import io.github.coolcrabs.fernutil.FernUtil.JavadocProvider;
import net.fabricmc.mappingio.tree.MappingTree;
import net.fabricmc.mappingio.tree.MappingTree.ClassMapping;
import net.fabricmc.mappingio.tree.MappingTree.FieldMapping;
import net.fabricmc.mappingio.tree.MappingTree.MethodMapping;

class FFJavadocProvider implements JavadocProvider {
    private final MappingTree tree;
    private final int namespace;

    FFJavadocProvider(MappingTree tree, int namespace) {
        this.tree = tree;
        this.namespace = namespace;
    }

    @Override
    public String clazzDoc(String clazz) {
        ClassMapping classMapping = tree.getClass(clazz, namespace);
        return classMapping == null ? null : classMapping.getComment();
    }

    @Override
    public String methodDoc(String clazz, String desc, String method) {
        ClassMapping classMapping = tree.getClass(clazz, namespace);
        if (classMapping != null) {
            MethodMapping methodMapping = classMapping.getMethod(method, desc, namespace);
            if (methodMapping != null) {
                return methodMapping.getComment();
            }
        }
        return null;
    }

    @Override
    public String fieldDoc(String clazz, String desc, String field) {
        ClassMapping classMapping = tree.getClass(clazz, namespace);
        if (classMapping != null) {
            FieldMapping fieldMapping = classMapping.getField(field, desc, namespace);
            if (fieldMapping != null) {
                return fieldMapping.getComment();
            }
        }
        return null;
    }
    
}
