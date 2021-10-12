package io.github.coolcrabs.brachyura.fabric;

import org.tinylog.Logger;

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
    final String whoToYellAt;
    int s = -2;

    public AccessWidenerNamespaceChanger(AccessWidenerVisitor parent, MappingTree mappings, int target, String whoToYellAt) {
        this.p = parent;
        this.m = mappings;
        this.t = target;
        this.whoToYellAt = whoToYellAt;
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
        ClassMapping clazz = clazz(name);
        if (clazz != null) {
            p.visitClass(clazz.getName(t), access, transitive);
        } else {
            p.visitClass(name, access, transitive);
        }
        
    }

    @Override
    public void visitMethod(String owner, String name, String descriptor, AccessType access, boolean transitive) {
        ClassMapping clazz = clazz(owner);
        if (clazz != null) {
            MethodMapping method = clazz.getMethod(name, descriptor, s);
            if (method != null) {
                p.visitMethod(clazz.getName(t), method.getName(t), method.getDesc(t), access, transitive);
            } else {
                Logger.warn("Possibly broken aw in {}: No method mapping found for {} in class {}", whoToYellAt, name, owner);
                p.visitMethod(clazz.getName(t), name, m.mapDesc(descriptor, t), access, transitive);
            }
        } else {
            p.visitMethod(owner, name, descriptor, access, transitive);
        }
    }

    @Override
    public void visitField(String owner, String name, String descriptor, AccessType access, boolean transitive) {
        ClassMapping clazz = clazz(owner);
        if (clazz != null) {
            FieldMapping field = clazz.getField(name, descriptor, s);
            if (field != null) {
                p.visitField(clazz.getName(t), field.getName(t), field.getDesc(t), access, transitive);
            } else {
                Logger.warn("Possibly broken aw in {}: No field mapping found for {} in class {}", whoToYellAt, name, owner);
                p.visitField(clazz.getName(t), name, m.mapDesc(descriptor, t), access, transitive);
            }
        } else {
            p.visitField(owner, name, descriptor, access, transitive);
        }
    }

    ClassMapping clazz(String owner) {
        ClassMapping clazz = m.getClass(owner, s);
        if (clazz == null) {
            Logger.warn("Possibly broken aw in {}: No class mapping found for {}", whoToYellAt, owner);
        }
        return clazz;
    }
}
