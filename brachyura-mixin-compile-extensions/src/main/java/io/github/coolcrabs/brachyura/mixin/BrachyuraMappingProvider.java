package io.github.coolcrabs.brachyura.mixin;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import javax.annotation.processing.Filer;

import org.spongepowered.asm.obfuscation.mapping.common.MappingField;
import org.spongepowered.asm.obfuscation.mapping.common.MappingMethod;
import org.spongepowered.tools.obfuscation.mapping.IMappingProvider;

import io.github.coolcrabs.brachyura.mixin.TinyTinyMappingsReader.TinyTree;
import io.github.coolcrabs.brachyura.mixin.TinyTinyMappingsReader.TinyTree.TinyClass;
import io.github.coolcrabs.brachyura.mixin.TinyTinyMappingsReader.TinyTree.TinyClass.TinyField;
import io.github.coolcrabs.brachyura.mixin.TinyTinyMappingsReader.TinyTree.TinyClass.TinyMethod;

class BrachyuraMappingProvider implements IMappingProvider {
    final String inNamespace;
    final String outNamespace;
    final Filer filer;

    TinyTree tree;
    int src = -1;
    int dst = -1;

    public BrachyuraMappingProvider(String inNamespace, String outNamespace, Filer filer) {
        this.inNamespace = inNamespace;
        this.outNamespace = outNamespace;
        this.filer = filer;
    }

    @Override
    public void read(File input) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(input.toPath())) {
            tree = TinyTinyMappingsReader.read(reader);
        }
        src = tree.getNamespace(inNamespace);
        dst = tree.getNamespace(outNamespace);
    }

    @Override
    public void clear() {
        tree = null;
        src = -1;
        dst = -1;
    }

    @Override
    public boolean isEmpty() {
        return tree == null;
    }

    @Override
    public MappingMethod getMethodMapping(MappingMethod method) {
        TinyClass clazz = tree.classmaps[src].get(method.getOwner());
        if (clazz != null) {
            TinyMethod method2 = null;
            for (TinyMethod m : clazz.methods) {
                if (m.name[src].equals(method.getSimpleName()) && method.getDesc().equals(m.getDesc(tree, src))) {
                    method2 = m;
                    break;
                }
            }
            if (method2 == null || method2.name[dst].isEmpty()) return null;
            String owner = clazz.names[dst];
            if (owner.isEmpty()) owner = method.getOwner();
            return new MappingMethod(owner, method2.name[dst], method2.getDesc(tree, dst));
        }
        return null;
    }

    // Ignores field descriptors b/c they aren't well supported in some versions and whatnot
    @Override
    public MappingField getFieldMapping(MappingField field) {
        TinyClass clazz = tree.classmaps[src].get(field.getOwner());
        if (clazz != null) {
            TinyField field2 = null;
            for (TinyField m : clazz.fields) {
                if (m.name[src].equals(field.getSimpleName())) {
                    field2 = m;
                    break;
                }
            }
            if (field2 == null || field2.name[dst].isEmpty()) return null;
            String owner = clazz.names[dst];
            if (owner.isEmpty()) owner = field.getOwner();
            return new MappingField(owner, field2.name[dst]);
        }
        return null;
    }

    @Override
    public String getClassMapping(String className) {
        TinyClass clazz = tree.classmaps[src].get(className);
        if (clazz != null && !clazz.names[dst].isEmpty()) {
            return clazz.names[dst];
        }
        return null;
    }

    @Override
    public String getPackageMapping(String packageName) {
        return null; // Seemingly unused anyways?
    }

}
