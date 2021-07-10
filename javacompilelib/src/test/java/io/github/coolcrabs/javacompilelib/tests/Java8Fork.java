package io.github.coolcrabs.javacompilelib.tests;

import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Test;

import io.github.coolcrabs.javacompilelib.ForkingJavaCompilation;
import io.github.coolcrabs.javacompilelib.JavaCompilationUnit;

public class Java8Fork {
    @Test
    public void java8NoFork() {
        File javaFile = new File("tests/java8/Java8BruhMoment.java");
        File outputDir = new File("tests/build/java8");
        JavaCompilationUnit javaCompilationUnit = new JavaCompilationUnit.Builder()
            .sourceFiles(new File[] {javaFile})
            .outputDir(outputDir)
            .options(new String[] {"-target", "1.8", "-source", "1.8"})
            .build();
        ForkingJavaCompilation forkingJavaCompilation = new ForkingJavaCompilation("java");
        assertTrue(forkingJavaCompilation.compile(javaCompilationUnit));
    }
}
