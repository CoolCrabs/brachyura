package io.github.coolcrabs.brachyura.decompiler;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;

import org.junit.jupiter.api.Test;

import io.github.coolcrabs.brachyura.decompiler.DecompileLineNumberTable.MethodId;
import io.github.coolcrabs.brachyura.decompiler.cfr.CfrDecompiler;
import io.github.coolcrabs.brachyura.util.FileSystemUtil;

// If you put a line number on a dup following a new call mixin causes a verify error
class CfrMixinBugDontHappenAhh {
    @Test
    void ahh() throws Exception {
        System.out.println(CfrMixinBugDontHappenAhh.class.getProtectionDomain().getCodeSource().getLocation());
        Path file = File.createTempFile("ahhh", ".jar").toPath();
        Path lines = file.getParent().resolve(file.getFileName().toString() + ".linenumber");
        Files.deleteIfExists(file);
        try (FileSystem fileSystem = FileSystemUtil.newJarFileSystem(file)) {
            Files.createDirectories(fileSystem.getPath("io/github/coolcrabs/brachyura/decompiler"));
            Files.copy(
                CfrMixinBugDontHappenAhh.class.getResourceAsStream("/io/github/coolcrabs/brachyura/decompiler/CfrMixinBugDontHappenAhh$B.class"),
                fileSystem.getPath("io/github/coolcrabs/brachyura/decompiler/CfrMixinBugDontHappenAhh$B.class")
            );
        }
        new CfrDecompiler(1).decompile(file, Collections.emptyList(), null, lines, null, 99);
        DecompileLineNumberTable linesTable = new DecompileLineNumberTable();
        linesTable.read(lines);
        assertEquals(0, linesTable.classes.get("io/github/coolcrabs/brachyura/decompiler/CfrMixinBugDontHappenAhh$B").get(new MethodId("bruh", "()V")).get(0).startPc);
    }

    static class B {
        void bruh() {
            B b = new B();
        }
    }
}
