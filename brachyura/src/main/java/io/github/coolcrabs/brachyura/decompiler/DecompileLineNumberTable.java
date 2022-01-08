package io.github.coolcrabs.brachyura.decompiler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DecompileLineNumberTable {
    // If true replaces existing tables
    // If false remaps existing table
    public final Map<String, ClassLineMap> classes = new ConcurrentHashMap<>(); // In internal names

    public static class ClassLineMap {
        final boolean isStupid;
        public final Map<MethodId, MethodLineMap> methods;
        final Map<Integer, Integer> stupid;

        public ClassLineMap(Map<MethodId, MethodLineMap> methods) {
            this.isStupid = false;
            this.methods = methods;
            this.stupid = null;
        }

        public ClassLineMap(int[] stupid) {
            this.isStupid = true;
            this.methods = null;
            this.stupid = new HashMap<>();
            for (int i = 0; i < stupid.length; i += 2) {
                this.stupid.put(stupid[i], stupid[i + 1]);
            }
        }
    }
    
    public static class MethodLineMap {
        final boolean isReplace;
        final List<LineNumberTableEntry> replace;
        final Map<Integer, Integer> remap;
        
        public MethodLineMap(List<LineNumberTableEntry> replace) {
            this.isReplace = true;
            this.replace = replace;
            this.remap = null;
        }
        
        public MethodLineMap(Map<Integer, Integer> remap) {
            this.isReplace = false;
            this.remap = remap;
            this.replace = null;
        }
    }

    public static class MethodId {
        public final String name;
        public final String desc;

        public MethodId(String name, String desc) {
            this.name = name;
            this.desc = desc;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            MethodId methodId = (MethodId) o;
            return name.equals(methodId.name) && desc.equals(methodId.desc);
        }

        @Override
        public int hashCode() {
            int result = name.hashCode();
            result = 31 * result + desc.hashCode();
            return result;
        }
    }
}
