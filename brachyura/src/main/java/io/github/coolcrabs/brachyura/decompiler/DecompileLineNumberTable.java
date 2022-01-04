package io.github.coolcrabs.brachyura.decompiler;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DecompileLineNumberTable {
    // If true replaces existing tables
    // If false remaps existing table
    public final Map<String, Map<MethodId, MethodLineMap>> classes = new ConcurrentHashMap<>(); // In internal names
    
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
