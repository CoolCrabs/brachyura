package io.github.coolcrabs.cfr.impl;

import java.util.Map;

import org.benf.cfr.reader.bytecode.analysis.types.JavaTypeInstance;
import org.benf.cfr.reader.bytecode.analysis.types.MethodPrototype;
import org.benf.cfr.reader.util.collections.MapFactory;

public class TypeUtil {
    private TypeUtil() { }

    private static final Map<String, String> PRIMATIVE_SIGNATURES = MapFactory.newMap();

    static {
        PRIMATIVE_SIGNATURES.put("long", "J");
        PRIMATIVE_SIGNATURES.put("int", "I");
        PRIMATIVE_SIGNATURES.put("short", "S");
        PRIMATIVE_SIGNATURES.put("byte", "B");
        PRIMATIVE_SIGNATURES.put("boolean", "Z");
        PRIMATIVE_SIGNATURES.put("float", "F");
        PRIMATIVE_SIGNATURES.put("double", "D");
        PRIMATIVE_SIGNATURES.put("void", "V");
    }

    public static String getSignature(MethodPrototype methodPrototype) {
        StringBuilder result = new StringBuilder();
        result.append('(');
        for (JavaTypeInstance javaTypeInstance : methodPrototype.getArgs()) {
            result.append(descriptor(javaTypeInstance));
        }
        result.append(')');
        result.append(descriptor(methodPrototype.getReturnType()));
        return result.toString();
    }

    public static String descriptor(JavaTypeInstance javaTypeInstance) {
        if (javaTypeInstance == null) return "V"; //void
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < javaTypeInstance.getNumArrayDimensions(); i++) {
            result.append('[');
        }
        if (javaTypeInstance.isObject()) {
            result.append('L');
            result.append(toInternal(javaTypeInstance));
            result.append(';');
        } else {
            result.append(PRIMATIVE_SIGNATURES.get(javaTypeInstance.getRawName()));
        }
        return result.toString();
    }

    public static String toInternal(JavaTypeInstance javaTypeInstance) {
        return toSlashed(javaTypeInstance.getDeGenerifiedType().getRawName());
    }

    public static String toSlashed(String clazz) {
        return clazz.replace('.', '/');
    }
}
