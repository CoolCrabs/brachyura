package io.github.coolcrabs.majoidea;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.ast.visitor.ModifierVisitor;
import com.github.javaparser.ast.visitor.Visitable;
import com.github.javaparser.resolution.types.ResolvedReferenceType;

import net.fabricmc.mappingio.tree.MappingTree;
import net.fabricmc.mappingio.tree.MappingTree.ClassMapping;

class RemapModifier extends ModifierVisitor<Void> {
    final MappingTree mappings;
    final int srcNamespace;
    final int dstNamespace;
    final List<Runnable> tasks = new ArrayList<>();

    public RemapModifier(MappingTree mappings, int srcNamespace, int dstNamespace) {
        this.mappings = mappings;
        this.srcNamespace = srcNamespace;
        this.dstNamespace = dstNamespace;
    }

    private ClassMapping getClassMapping(String qualifiedName) {
        String testingname = qualifiedName.replace('.', '/');
        for (;;) {
            ClassMapping mapping = mappings.getClass(testingname, srcNamespace);
            if (mapping != null) return mapping;
            int a = testingname.lastIndexOf('/');
            if (a < 0) return null;
            testingname = testingname.substring(0, a) + "$" + testingname.substring(a + 1);
        }
    }

    private String toQualifiedClass(String internalClass) {
        return internalClass.replace('/', '.').replace('$', '.');
    }

    private ClassOrInterfaceType getClassOrInterfaceType(String[] a) {
        ClassOrInterfaceType result = null;
        for (int i = 0; i < a.length; i++) {
            result = new ClassOrInterfaceType(result, a[i]);
        }
        return result;
    }

    private void remapType(Type type) {
        if (type instanceof ClassOrInterfaceType) {
            remapClass((ClassOrInterfaceType)type);
        }
        //TODO All other types
    }

    private void remapClass(ClassOrInterfaceType n) {
        Optional<NodeList<Type>> otypeArgs = n.getTypeArguments();
        if (otypeArgs.isPresent()) {
            for (Type type2 : otypeArgs.get()) {
                remapType(type2);
            }
        }
        ResolvedReferenceType type;
        try {
            type = n.resolve();
        } catch (Exception e) {
            Majoidea.logger.warning("Unkown class " + n.toString());
            return;
        }
        ClassMapping classMapping = getClassMapping(type.getQualifiedName());
        if (classMapping != null) {
            String srcName = type.getQualifiedName();
            String targetName = toQualifiedClass(classMapping.getName(dstNamespace));
            SimpleName simpleName = new SimpleName(targetName.substring(targetName.lastIndexOf('.') + 1));
            tasks.add(() -> {
                n.setName(simpleName);
            });
            ClassOrInterfaceType replaceScope = null;
            
            Optional<ClassOrInterfaceType> scope = n.getScope();
            if (scope.isPresent()) {
                ClassOrInterfaceType scope2 = scope.get();
                String[] a = scope2.toString().split("\\.");
                String[] b = srcName.split("\\.");
                String[] c = targetName.split("\\.");
                if (a.length == b.length - 1) {
                    replaceScope = getClassOrInterfaceType(Arrays.copyOfRange(c, 0, c.length - 1));
                } else {
                    int d = b.length - a.length - 1;
                    replaceScope = getClassOrInterfaceType(Arrays.copyOfRange(c, c.length - d, c.length - 1));
                }
                ClassOrInterfaceType replaceScope2 = replaceScope; // make javac happy
                tasks.add(() -> {
                    ClassOrInterfaceType replacement = n.clone();
                    n.setName(simpleName);
                    if (replaceScope2 != null) {
                        replacement.setScope(replaceScope2);
                    }
                    n.replace(replacement);
                });
            }
        }
    }

    @Override
    public Visitable visit(final CompilationUnit n, final Void arg) {
        super.visit(n, arg);
        for (Runnable task : tasks) {
            task.run();
        }
        return n;
    }

    @Override
    public Visitable visit(final ClassOrInterfaceType n, final Void arg) {
        System.out.println(n);
        remapClass(n);
        return super.visit(n, arg);
    }
    
}
