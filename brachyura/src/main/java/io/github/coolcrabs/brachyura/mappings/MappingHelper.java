package io.github.coolcrabs.brachyura.mappings;

import java.util.Iterator;
import net.fabricmc.mappingio.tree.MappingTree;

public class MappingHelper {
    private MappingHelper() { }
    
    public static void dropNullInNamespace(MappingTree mappings, String ns) {
        dropNullInNamespace(mappings, mappings.getNamespaceId(ns));
    }
    
    public static void dropNullInNamespace(MappingTree mappings, int ns) {
        Iterator<? extends MappingTree.ClassMapping> clsIt = mappings.getClasses().iterator();
        while (clsIt.hasNext()) {
            MappingTree.ClassMapping cls = clsIt.next();
            if (cls.getName(ns) == null) {
                clsIt.remove();
            } else {
                Iterator<? extends MappingTree.MethodMapping> methodIt = cls.getMethods().iterator();
                while (methodIt.hasNext()) {
                    MappingTree.MethodMapping method = methodIt.next();
                    if (method.getName(ns) == null) methodIt.remove();
                }
                Iterator<? extends MappingTree.FieldMapping> fieldIt = cls.getFields().iterator();
                while (fieldIt.hasNext()) {
                    MappingTree.FieldMapping field = fieldIt.next();
                    if (field.getName(ns) == null) fieldIt.remove();
                }
            }
        }
    }
}
