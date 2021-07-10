package io.github.coolcrabs.javacompilelib.tests;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Test;

import io.github.coolcrabs.javacompilelib.JavaCompilationUnit;
import io.github.coolcrabs.javacompilelib.LocalJavaCompilation;

public class Java6NoFork {
    @Test
    public void java6NoFork() {
        File javaFile = new File("tests/java6/HelloWorld.java");
        File outputDir = new File("tests/build/java6");
        JavaCompilationUnit javaCompilationUnit = new JavaCompilationUnit.Builder()
            .sourceFiles(new File[] {javaFile})
            .outputDir(outputDir)
            .options(new String[] {"-target", "1.6", "-source", "1.6"})
            .build();
        assertNotNull(LocalJavaCompilation.INSTANCE);
        assertTrue(
            LocalJavaCompilation.INSTANCE.compile(
                javaCompilationUnit
            )
        );
    }
}
