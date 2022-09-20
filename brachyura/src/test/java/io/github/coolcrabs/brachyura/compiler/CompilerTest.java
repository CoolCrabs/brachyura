package io.github.coolcrabs.brachyura.compiler;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import io.github.coolcrabs.brachyura.TestUtil;
import io.github.coolcrabs.brachyura.compiler.java.JavaCompilation;
import io.github.coolcrabs.brachyura.compiler.java.JavaCompilationResult;
import io.github.coolcrabs.brachyura.maven.Maven;
import io.github.coolcrabs.brachyura.maven.MavenId;
import io.github.coolcrabs.brachyura.processing.sources.ProcessingSponge;

class CompilerTest {
    @Test
    void e() {
        Path src = TestUtil.ROOT.resolve("testprogram").resolve("src").resolve("main").resolve("java");
        JavaCompilationResult compilation = new JavaCompilation()
            .addSourceDir(src)
            .compile();
        ProcessingSponge a = new ProcessingSponge();
        compilation.getInputs(a);
        int[] count = new int[1];
        a.getInputs((in, id) -> {
            count[0]++;
            Path sourceFile = compilation.getSourceFile(id);
            System.out.println(sourceFile);
            assertTrue(sourceFile.startsWith(src));
        });
    }

    @Test
    void mem() {
        Path dir = TestUtil.ROOT.resolve("test").resolve("compiler").resolve("java").resolve("memclass");
        JavaCompilationResult compilationA = new JavaCompilation()
            .addSourceFile(dir.resolve("ClassA.java"))
            .addSourceFile(dir.resolve("coolpackage").resolve("CoolPackageClassA.java"))
            .compile();
        ProcessingSponge a = new ProcessingSponge();
        compilationA.getInputs(a);
        JavaCompilationResult compilationB = new JavaCompilation()
            .addSourceFile(dir.resolve("ClassB.java"))
            .addClasspath(a)
            .compile();
        ProcessingSponge b = new ProcessingSponge();
        compilationB.getInputs(b);
        int[] count = new int[1];
        b.getInputs((in, id) -> {
            count[0]++;
            Path sourceFile = compilationB.getSourceFile(id);
            System.out.println(sourceFile);
        });
    }

    @Test
    void immutables() {
        Path dir = TestUtil.ROOT.resolve("test").resolve("compiler").resolve("java").resolve("immutables");
        JavaCompilationResult compilationA = new JavaCompilation()
            .addSourceDir(dir)
            .addClasspath(Maven.getMavenJarDep(Maven.MAVEN_CENTRAL, new MavenId("org.immutables:value:2.8.2")).jar)
            .compile();
        ProcessingSponge a = new ProcessingSponge();
        compilationA.getInputs(a);
        int[] count = new int[1];
        a.getInputs((in, id) -> {
            count[0]++;
            Path sourceFile = compilationA.getSourceFile(id);
            System.out.println(id.path);
            System.out.println(sourceFile);
        });
    }
}
