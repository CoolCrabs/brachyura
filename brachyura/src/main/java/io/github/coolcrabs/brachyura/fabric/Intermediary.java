package io.github.coolcrabs.brachyura.fabric;

import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;

import io.github.coolcrabs.brachyura.mappings.Namespaces;
import io.github.coolcrabs.brachyura.maven.Maven;
import io.github.coolcrabs.brachyura.maven.MavenId;
import io.github.coolcrabs.brachyura.util.FileSystemUtil;
import io.github.coolcrabs.brachyura.util.Util;
import net.fabricmc.mappingio.MappingReader;
import net.fabricmc.mappingio.adapter.MappingNsRenamer;
import net.fabricmc.mappingio.format.MappingFormat;
import net.fabricmc.mappingio.tree.MappingTree;
import net.fabricmc.mappingio.tree.MemoryMappingTree;

public class Intermediary {
    public final MappingTree tree;

    private static final Map<String, String> intermediaryNamespaces = Collections.singletonMap("official", Namespaces.OBF);

    private Intermediary(MappingTree tree) {
        this.tree = tree;
    }

    public static Intermediary ofV1(Path file) {
        try {
            MemoryMappingTree tree = new MemoryMappingTree();
            MappingReader.read(file, MappingFormat.TINY, new MappingNsRenamer(tree, intermediaryNamespaces));
            return new Intermediary(tree);
        } catch (Exception e) {
            throw Util.sneak(e);
        }
    }

    public static Intermediary ofV1Jar(Path file) {
        try {
            try (FileSystem fileSystem = FileSystemUtil.newJarFileSystem(file)) {
                return ofV1(fileSystem.getPath("mappings/mappings.tiny"));
            }
        } catch (Exception e) {
            throw Util.sneak(e);
        }
    }

    public static Intermediary ofMaven(String repo, MavenId id) {
        return ofV1Jar(Maven.getMavenFileDep(repo, id, ".jar").file);
    }
}
