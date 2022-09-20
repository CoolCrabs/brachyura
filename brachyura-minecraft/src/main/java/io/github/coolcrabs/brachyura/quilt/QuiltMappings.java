package io.github.coolcrabs.brachyura.quilt;

import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.util.Collections;

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

public class QuiltMappings {
    public final MappingTree mappings;

    private QuiltMappings(MappingTree mappings) {
        this.mappings = mappings;
    }

    public static QuiltMappings ofMergedV2(Path jar) {
        try {
            try (FileSystem fileSystem = FileSystemUtil.newJarFileSystem(jar)) {
                Path p = fileSystem.getPath("mappings/mappings.tiny");
                MemoryMappingTree tree = new MemoryMappingTree(true);
                MappingReader.read(p, MappingFormat.TINY_2, tree);
                return new QuiltMappings(tree);
            }
        } catch (Exception e) {
            throw Util.sneak(e);
        }
    }

    public static QuiltMappings ofMaven(String mavenUrl, MavenId mavenId) {
        return ofMergedV2(Maven.getMavenFileDep(mavenUrl, mavenId, "-mergedv2.jar").file);
    }

    public MappingTree toIntermediary(MappingTree intermediary) {
        try {
            MemoryMappingTree r = new MemoryMappingTree(false);
            intermediary.accept(new MappingNsRenamer(r, Collections.singletonMap(Namespaces.OBF, "official")));
            mappings.accept(r);
            int o = r.getNamespaceId("official");
            int n = r.getNamespaceId(Namespaces.NAMED);
            // Intermediary maps some stuff hashed doesn't
            // Merged qm doesn't have "holes"
            for (MappingTree.ClassMapping cls : r.getClasses()) {
                if (cls.getName(n) == null) {
                    cls.setDstName(cls.getName(o), n);
                }
                for (MappingTree.MethodMapping m : cls.getMethods()) {
                    if (m.getName(n) == null) {
                        m.setDstName(m.getName(o), n);
                    }
                }
                for (MappingTree.FieldMapping m : cls.getFields()) {
                    if (m.getName(n) == null) {
                        m.setDstName(m.getName(o), n);
                    }
                }
            }
            return r;
        } catch (Exception e) {
            throw Util.sneak(e);
        }
    }

}
