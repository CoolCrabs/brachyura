package io.github.coolcrabs.brachyura.fabric;

import java.io.FileNotFoundException;
import java.nio.file.FileSystem;
import java.nio.file.Path;

import io.github.coolcrabs.brachyura.dependency.FileDependency;
import io.github.coolcrabs.brachyura.exception.UnreachableException;
import io.github.coolcrabs.brachyura.maven.Maven;
import io.github.coolcrabs.brachyura.maven.MavenId;
import io.github.coolcrabs.brachyura.util.FileSystemUtil;
import io.github.coolcrabs.brachyura.util.Util;
import net.fabricmc.mappingio.MappingReader;
import net.fabricmc.mappingio.format.MappingFormat;
import net.fabricmc.mappingio.tree.MappingTree;
import net.fabricmc.mappingio.tree.MemoryMappingTree;

public class Yarn {
    // Either obf-named or intermediary-named
    public final MappingTree tree;

    private Yarn(MappingTree tree) {
        this.tree = tree;
    }

    public static Yarn ofV2(Path file) {
        try {
            MemoryMappingTree tree = new MemoryMappingTree();
            try (FileSystem fileSystem = FileSystemUtil.newFileSystem(file)) {
                MappingReader.read(fileSystem.getPath("mappings/mappings.tiny"), MappingFormat.TINY_2, tree);
            }
            return new Yarn(tree);
        } catch (Exception e) {
            throw Util.sneak(e);
        }
    }

    public static Yarn ofObfEnigmaZip(Path file) {
        throw new UnsupportedOperationException(); //todo
    }

    @SuppressWarnings("all")
    public static Yarn ofMaven(String maven, MavenId id) {
        FileDependency v2 = Maven.getMavenFileDep(maven, id, "-v2.jar", false);
        FileDependency enigma = Maven.getMavenFileDep(maven, id, "-enigma.zip", false);
        if (v2 == null && enigma == null) {
            try {
                v2 = Maven.getMavenFileDep(maven, id, "-v2.jar");
            } catch (Exception e) {
                if (e instanceof FileNotFoundException) { // checked exceptions are ir
                    enigma = Maven.getMavenFileDep(maven, id, "-enigma.zip");
                } else {
                    throw e;
                }
            }
        }
        if (v2 != null) {
            return ofV2(v2.file);
        }
        if (enigma != null) {
            return ofObfEnigmaZip(enigma.file);
        }
        throw new UnreachableException();
    }
}
