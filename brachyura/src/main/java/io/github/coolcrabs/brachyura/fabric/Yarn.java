package io.github.coolcrabs.brachyura.fabric;

import java.io.FileNotFoundException;
import java.nio.file.FileSystem;
import java.nio.file.Path;

import io.github.coolcrabs.brachyura.dependency.FileDependency;
import io.github.coolcrabs.brachyura.exception.UnreachableException;
import io.github.coolcrabs.brachyura.mappings.Namespaces;
import io.github.coolcrabs.brachyura.maven.Maven;
import io.github.coolcrabs.brachyura.maven.MavenId;
import io.github.coolcrabs.brachyura.util.FileSystemUtil;
import io.github.coolcrabs.brachyura.util.Util;
import net.fabricmc.mappingio.MappingReader;
import net.fabricmc.mappingio.format.EnigmaReader;
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
            MappingReader.read(file, MappingFormat.TINY_2, tree);
            return new Yarn(tree);
        } catch (Exception e) {
            throw Util.sneak(e);
        }
    }

    public static Yarn ofObfEnigma(Path dir) {
        try {
            MemoryMappingTree tree = new MemoryMappingTree();
            EnigmaReader.read(dir, Namespaces.OBF, Namespaces.NAMED, tree);
            return new Yarn(tree);
        } catch (Exception e) {
            throw Util.sneak(e);
        }
    }

    public static Yarn ofV2Jar(Path file) {
        try {
            try (FileSystem fileSystem = FileSystemUtil.newJarFileSystem(file)) {
                return ofV2(fileSystem.getPath("mappings/mappings.tiny"));
            }
        } catch (Exception e) {
            throw Util.sneak(e);
        }
    }

    public static Yarn ofObfEnigmaZip(Path file) {
        try {
            try (FileSystem fileSystem = FileSystemUtil.newJarFileSystem(file)) {
                return ofObfEnigma(fileSystem.getPath("/"));
            }
        } catch (Exception e) {
            throw Util.sneak(e);
        }
    }

    @SuppressWarnings("all")
    public static Yarn ofMaven(String repo, MavenId id) {
        FileDependency v2 = Maven.getMavenFileDep(repo, id, "-mergedv2.jar", false);
        FileDependency enigma = Maven.getMavenFileDep(repo, id, "-enigma.zip", false);
        if (v2 == null && enigma == null) {
            try {
                v2 = Maven.getMavenFileDep(repo, id, "-mergedv2.jar");
                Util.<FileNotFoundException>unsneak();
            } catch (FileNotFoundException e) {
                enigma = Maven.getMavenFileDep(repo, id, "-enigma.zip");
            }
        }
        if (v2 != null) {
            return ofV2Jar(v2.file);
        }
        if (enigma != null) {
            return ofObfEnigmaZip(enigma.file);
        }
        throw new UnreachableException();
    }
}
