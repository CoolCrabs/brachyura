package io.github.coolcrabs.brachyura.decompiler;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import io.github.coolcrabs.brachyura.util.PathUtil;
import io.github.coolcrabs.brachyura.util.Util;

public class DecompileLineNumberTable {
    public final Map<String, Map<MethodId, List<LineNumberTableEntry>>> classes = new ConcurrentHashMap<>(); // In internal names

    public void write(Path path) {
        try {
            try (DataOutputStream out = new DataOutputStream(new GZIPOutputStream(PathUtil.outputStream(path)))) {
                out.writeInt(classes.size());
                for (Map.Entry<String, Map<MethodId, List<LineNumberTableEntry>>> entry : classes.entrySet()) {
                    out.writeUTF(entry.getKey());
                    out.writeInt(entry.getValue().size());
                    for (Map.Entry<MethodId, List<LineNumberTableEntry>> methodEntry : entry.getValue().entrySet()) {
                        out.writeUTF(methodEntry.getKey().name);
                        out.writeUTF(methodEntry.getKey().desc);
                        out.writeInt(methodEntry.getValue().size());
                        for (LineNumberTableEntry entry2 : methodEntry.getValue()) {
                            out.writeInt(entry2.startPc);
                            out.writeInt(entry2.lineNumber);
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw Util.sneak(e);
        }
    }

    public DecompileLineNumberTable read(Path path) {
        try {
            try (DataInputStream in = new DataInputStream(new GZIPInputStream(PathUtil.inputStream(path)))) {
                int classesSize = in.readInt();
                for (int i = 0; i < classesSize; i++) {
                    String className = in.readUTF();
                    int methodsSize = in.readInt();
                    Map<MethodId, List<LineNumberTableEntry>> methodMap = new HashMap<>(methodsSize);
                    for (int j = 0; j < methodsSize; j++) {
                        String methodName = in.readUTF();
                        String methodDesc = in.readUTF();
                        MethodId methodId = new MethodId(methodName, methodDesc);
                        int methodSize = in.readInt();
                        List<LineNumberTableEntry> methodEntries = new ArrayList<>(methodSize);
                        for (int k = 0; k < methodSize; k++) {
                            int startPc = in.readInt();
                            int lineNumber = in.readInt();
                            methodEntries.add(new LineNumberTableEntry(startPc, lineNumber));
                        }
                        methodMap.put(methodId, methodEntries);
                    }
                    classes.put(className, methodMap);
                }
            }
        } catch (Exception e) {
            throw Util.sneak(e);
        }
        return this;
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
