package io.github.coolcrabs.brachyura.compiler;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import io.github.coolcrabs.brachyura.compiler.java.JavaCompilation;
import io.github.coolcrabs.brachyura.compiler.java.JavaCompilationResult;
import io.github.coolcrabs.brachyura.processing.sources.ProcessingSponge;
import io.github.coolcrabs.brachyura.util.PathUtil;

class CompilerTest {
    @Test
    void e() {
        Path src = PathUtil.CWD.getParent().resolve("testprogram").resolve("src").resolve("main").resolve("java");
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
}
