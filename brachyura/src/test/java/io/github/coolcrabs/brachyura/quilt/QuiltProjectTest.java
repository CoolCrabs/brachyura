package io.github.coolcrabs.brachyura.quilt;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;

import io.github.coolcrabs.brachyura.decompiler.BrachyuraDecompiler;
import io.github.coolcrabs.brachyura.fabric.FabricContext.ModDependencyCollector;
import io.github.coolcrabs.brachyura.fabric.FabricLoader;
import io.github.coolcrabs.brachyura.mappings.Namespaces;
import io.github.coolcrabs.brachyura.minecraft.Minecraft;
import io.github.coolcrabs.brachyura.minecraft.VersionMeta;
import io.github.coolcrabs.brachyura.util.JvmUtil;
import io.github.coolcrabs.brachyura.util.PathUtil;
import net.fabricmc.accesswidener.AccessWidenerReader;
import net.fabricmc.accesswidener.AccessWidenerVisitor;
import net.fabricmc.mappingio.tree.MappingTree;

public class QuiltProjectTest {
    SimpleQuiltProject proj = new SimpleQuiltProject() {

        @Override
        public VersionMeta createMcVersion() {
            return Minecraft.getVersion("1.18.2");
        }

        @Override
        public int getJavaVersion() {
            return 17;
        };

        @Override
        public Consumer<AccessWidenerVisitor> getAw() {
            return (v) -> {
                try {
                    new AccessWidenerReader(v).read(Files.newBufferedReader(getResourcesDir().resolve("testaw.accesswidener")), Namespaces.NAMED);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            };
        };

        @Override
        public MappingTree createMappings() {
            return createMojmap();
        }

        @Override
        public FabricLoader getLoader() {
            return new FabricLoader(QuiltMaven.URL, QuiltMaven.loader("0.16.0-beta.5"));
        }

        @Override
        public void getModDependencies(ModDependencyCollector d) {
            
        }

        @Override
        public Path getProjectDir() {
            Path result = PathUtil.CWD.getParent().resolve("test").resolve("quilt").resolve("testmod");
            assertTrue(Files.isDirectory(result)); 
            return result;
        }

        public BrachyuraDecompiler decompiler() {
            return null;
        };
    };

    @Test
    void ide() {
        long a = System.currentTimeMillis();
        proj.getTasks(p -> {
            if (p.name.equals("netbeans")) p.doTask(new String[]{});
            if (p.name.equals("idea")) p.doTask(new String[]{});
            if (p.name.equals("jdt")) p.doTask(new String[]{});
        });
        long b = System.currentTimeMillis();
        System.out.println(b - a);
    }

    @Test
    void compile() {
        if (JvmUtil.CURRENT_JAVA_VERSION >= 17) {
            try {
                proj.build();
            } catch (Exception e) {
                e.printStackTrace();
                throw e;
            }
        }
    }
}
