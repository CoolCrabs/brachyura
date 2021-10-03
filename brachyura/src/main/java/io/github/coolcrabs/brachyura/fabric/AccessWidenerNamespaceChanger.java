package io.github.coolcrabs.brachyura.fabric;

import net.fabricmc.accesswidener.AccessWidenerVisitor;
import net.fabricmc.accesswidener.AccessWidenerReader.AccessType;
import net.fabricmc.mappingio.tree.MappingTree;
import net.fabricmc.mappingio.tree.MappingTree.ClassMapping;
import net.fabricmc.mappingio.tree.MappingTree.FieldMapping;
import net.fabricmc.mappingio.tree.MappingTree.MethodMapping;

public class AccessWidenerNamespaceChanger implements AccessWidenerVisitor {
    final AccessWidenerVisitor p;
    final MappingTree m;
    final int t;
    int s = -2;

    public AccessWidenerNamespaceChanger(AccessWidenerVisitor parent, MappingTree mappings, int target) {
        this.p = parent;
        this.m = mappings;
        this.t = target;
    }

    @Override
    public void visitHeader(String namespace) {
        s = m.getNamespaceId(namespace);
        if (s == -2) {
            throw new UnsupportedOperationException(namespace);
        }
        p.visitHeader(m.getNamespaceName(t));
    }

    @Override
    public void visitClass(String name, AccessType access, boolean transitive) {
        p.visitClass(m.getClass(name, s).getName(t), access, transitive);
    }

    @Override
    public void visitMethod(String owner, String name, String descriptor, AccessType access, boolean transitive) {
        ClassMapping clazz = m.getClass(owner, s);
        MethodMapping method = clazz.getMethod(name, descriptor, s);
        p.visitMethod(clazz.getName(t), method.getName(t), method.getDesc(t), access, transitive);
    }

    @Override
    public void visitField(String owner, String name, String descriptor, AccessType access, boolean transitive) {
        ClassMapping clazz = m.getClass(owner, s);
        FieldMapping field = clazz.getField(name, descriptor, s);
        p.visitField(clazz.getName(t), field.getName(t), field.getDesc(t), access, transitive);
    }
}
