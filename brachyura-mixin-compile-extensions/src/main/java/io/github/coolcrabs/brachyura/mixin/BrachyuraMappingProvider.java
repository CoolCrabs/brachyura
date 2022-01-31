package io.github.coolcrabs.brachyura.mixin;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
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
        if (clazz == null) { // Mod class
            String newdesc = TinyTree.mapDesc(method.getDesc(), tree, src, dst);
            if (method.getDesc().equals(newdesc)) {
                return null;
            } else {
                return new MappingMethod(method.getOwner(), method.getSimpleName(), newdesc);
            }
        } else { // MC Class
            TinyMethod method2 = getMethod(method.getOwner(), method.getSimpleName(), method.getDesc());
            if (method2 == null) return null;
            String owner = clazz.names[dst];
            if (owner.isEmpty())
                owner = method.getOwner();
            return new MappingMethod(owner, method2.name[dst], method2.getDesc(tree, dst));
        }
    }

    TinyMethod getMethod(String cls, String name, String desc) {
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
        try {
            try (DataInputStream in = new DataInputStream(new BufferedInputStream(BrachyuraMappingWriter.class.getClassLoader().getResourceAsStream(cls + ".class")))) {
                int magic = in.readInt();
                int minor_version = in.readUnsignedShort();
                int major_version = in.readUnsignedShort();
                int cp_count = in.readUnsignedShort();
                Object[] cp = new Object[cp_count];
                for (int i = 1; i < cp_count; i++) {
                    byte tag = in.readByte();
                    switch (tag) {
                        case 1:
                            cp[i] = in.readUTF();
                            break;
                        case 3:
                        case 4:
                            in.readInt();
                            break;
                        case 5:
                        case 6:
                            in.readLong();
                            i++;
                            break;
                        case 7:
                            cp[i] = Integer.valueOf(in.readUnsignedShort());
                            break;
                        case 8:
                            in.skip(2);
                            break;
                        case 9:
                        case 10:
                        case 11:
                        case 12:
                            in.readUnsignedShort();
                            in.readUnsignedShort();
                            break;
                        case 15:
                            in.readByte();
                            in.readUnsignedShort();
                            break;
                        case 16:
                            in.readUnsignedShort();
                            break;
                        case 17:
                        case 18:
                            in.readUnsignedShort();
                            in.readUnsignedShort();
                            break;
                        default:
                            throw new UnsupportedOperationException("Unknown cp entry: " + tag + " in class " + cls);
                    }
                }
                int access_flags = in.readUnsignedShort();
                int this_class = in.readUnsignedShort();
                int super_class = in.readUnsignedShort();
                int interfaces_count = in.readUnsignedShort();
                for (int i = 0; i < interfaces_count; i++) {
                    int inter = in.readUnsignedShort();
                    TinyMethod method = getMethod((String)cp[(Integer) cp[inter]], name, desc);
                    if (method != null && !method.name[dst].isEmpty()) return method;
                }
                TinyMethod method = getMethod((String)cp[(Integer) cp[super_class]], name, desc);
                if (method != null && !method.name[dst].isEmpty()) return method;
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
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
