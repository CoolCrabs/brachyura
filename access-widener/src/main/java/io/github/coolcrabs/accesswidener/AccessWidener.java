package io.github.coolcrabs.accesswidener;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Map.Entry;

import io.github.coolcrabs.accesswidener.AccessWidenerReader.AccessType;

public class AccessWidener implements AccessWidenerVisitor {
    String namespace;
    public final Map<String, ClassAwData> clsMap = new HashMap<>();

    public static class ClassAwData {
        public final Set<AccessType> access = EnumSet.noneOf(AccessType.class);
        public final Map<Member, Set<AccessType>> fields = new HashMap<>();
        public final Map<Member, Set<AccessType>> methods = new HashMap<>();
    }

    public static class Member {
        public final String name;
        public final String desc;

        public Member(String name, String desc) {
            this.name = name;
            this.desc = desc;
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, desc);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Member) {
                Member o2 = (Member) obj;
                return o2.name.equals(name) && o2.desc.equals(desc);
            }
            return false;
        }
    }

    public AccessWidener(String namespace) {
        this.namespace = namespace;
    }

    public AccessWidener() {
        this.namespace = null;
    }

    public String getNamespace() {
        return namespace;
    }

    public void accept(AccessWidenerVisitor visitor) {
        visitor.visitHeader(namespace);
        for (Entry<String, ClassAwData> ce : clsMap.entrySet()) {
            for (AccessType cat : ce.getValue().access) {
                visitor.visitClass(ce.getKey(), cat, false);
            }
            for (Entry<Member, Set<AccessType>> me : ce.getValue().fields.entrySet()) {
                for (AccessType mat : me.getValue()) {
                    visitor.visitField(ce.getKey(), me.getKey().name, me.getKey().desc, mat, false);
                }
            }
            for (Entry<Member, Set<AccessType>> me : ce.getValue().methods.entrySet()) {
                for (AccessType mat : me.getValue()) {
                    visitor.visitMethod(ce.getKey(), me.getKey().name, me.getKey().desc, mat, false);
                }
            }
        }
    }

    @Override
    public void visitHeader(String namespace) {
        if (this.namespace != null && !this.namespace.equals(namespace)) throw new IllegalArgumentException("Expected namespace " + this.namespace + " got " + namespace);
        this.namespace = namespace;
    }

    @Override
    public void visitClass(String name, AccessType access, boolean transitive) {
        clsMap.computeIfAbsent(name, k -> new ClassAwData()).access.add(access);
    }

    @Override
    public void visitField(String owner, String name, String descriptor, AccessType access, boolean transitive) {
        if (access == AccessType.EXTENDABLE) throw new IllegalArgumentException("Fields can not be extended");
        clsMap.computeIfAbsent(owner, k -> new ClassAwData()).fields.computeIfAbsent(new Member(name, descriptor), k -> EnumSet.noneOf(AccessType.class)).add(access);
    }

    @Override
    public void visitMethod(String owner, String name, String descriptor, AccessType access, boolean transitive) {
        if (access == AccessType.MUTABLE) throw new IllegalArgumentException("Methods can not be mutable");
        clsMap.computeIfAbsent(owner, k -> new ClassAwData()).methods.computeIfAbsent(new Member(name, descriptor), k -> EnumSet.noneOf(AccessType.class)).add(access);
    }
}
