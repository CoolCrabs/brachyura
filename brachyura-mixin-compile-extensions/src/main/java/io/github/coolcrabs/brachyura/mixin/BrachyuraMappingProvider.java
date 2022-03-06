package io.github.coolcrabs.brachyura.mixin;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayDeque;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic.Kind;

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
    final Messager messager;
    final ProcessingEnvironment env;

    TinyTree tree;
    int src = -1;
    int dst = -1;

    public BrachyuraMappingProvider(String inNamespace, String outNamespace, Filer filer, Messager messager, ProcessingEnvironment env) {
        this.inNamespace = inNamespace;
        this.outNamespace = outNamespace;
        this.filer = filer;
        this.messager = messager;
        this.env = env;
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
        if (clazz == null) { // Mod class
            String newdesc = TinyTree.mapDesc(method.getDesc(), tree, src, dst);
            if (method.getDesc().equals(newdesc)) {
                return null;
            } else {
                return new MappingMethod(method.getOwner(), method.getSimpleName(), newdesc);
            }
        } else { // MC Class
            TinyMethod method2 = getMethod(method.getOwner(), null, method.getSimpleName(), method.getDesc());
            if (method2 == null) return null;
            String owner = clazz.names[dst];
            if (owner.isEmpty())
                owner = method.getOwner();
            return new MappingMethod(owner, method2.name[dst], method2.getDesc(tree, dst));
        }
    }

    TinyMethod getMethod(String cls, TypeElement clsType, String name, String desc) {
        if ("java/lang/Object".equals(cls)) return null;
        TinyClass clazz = tree.classmaps[src].get(cls);
        if (clazz != null) {
            for (TinyMethod m : clazz.methods) {
                if (m.name[src].equals(name) && desc.equals(m.getDesc(tree, src)) && !m.name[dst].isEmpty()) {
                    return m;
                }
            }
        }
        // Scan super classes
        if (clsType == null) clsType = getClassType(cls);
        if (clsType != null) {
            for (TypeMirror tm : clsType.getInterfaces()) {
                TypeElement te = asTe(tm);
                if (te == null) continue;
                TinyMethod method = getMethod(env.getElementUtils().getBinaryName(te).toString().replace('.', '/'), te, name, desc);
                if (method != null && !method.name[dst].isEmpty()) return method;
            }
            TypeElement parent = asTe(clsType.getSuperclass());
            if (parent != null) {
                TinyMethod method = getMethod(env.getElementUtils().getBinaryName(parent).toString().replace('.', '/'), parent, name, desc);
                if (method != null && !method.name[dst].isEmpty()) return method;
            }
        }
        
        return null;
    }

    TypeElement asTe(TypeMirror tm) {
        if (!(tm instanceof DeclaredType)) return null;
        DeclaredType dt = (DeclaredType) tm;
        Element e = dt.asElement();
        if (!(e instanceof TypeElement)) return null;
        return (TypeElement) e;
    }

    TypeElement getClassType(String cls) {
        String dotted = cls.replace('/', '.');
        int dotIndex = dotted.lastIndexOf('.');
        int dollarIndex = dotted.indexOf('$');
        if (dollarIndex > dotIndex) { // $ in class name indicates potential inner class
            // Slower fallback scan for inner classes
            // Won't be used too often
            String pkg = dotted.substring(0, dotIndex);
            ArrayDeque<Element> elements = new ArrayDeque<>(env.getElementUtils().getPackageElement(pkg).getEnclosedElements());
            while (!elements.isEmpty()) {
                Element e = elements.pop();
                if (e instanceof TypeElement) {
                    TypeElement te = (TypeElement) e;
                    elements.addAll(te.getEnclosedElements());
                    if (env.getElementUtils().getBinaryName(te).contentEquals(dotted)) {
                        return te;
                    }
                }
            }
        } else {
            TypeElement te = env.getElementUtils().getTypeElement(dotted);
            if (te != null) return te;
        }
        
        messager.printMessage(Kind.WARNING, "[Brachyura Mixin Ext] Unable to find " + cls);
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
            if (field2 == null || field2.name[dst].isEmpty())
                return null;
            String owner = clazz.names[dst];
            if (owner.isEmpty())
                owner = field.getOwner();
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
