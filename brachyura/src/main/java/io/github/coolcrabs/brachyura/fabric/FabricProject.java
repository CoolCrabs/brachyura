package io.github.coolcrabs.brachyura.fabric;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import io.github.coolcrabs.brachyura.decompiler.BrachyuraDecompiler;
import io.github.coolcrabs.brachyura.decompiler.cfr.CfrDecompiler;
import io.github.coolcrabs.brachyura.dependency.Dependency;
import io.github.coolcrabs.brachyura.dependency.JavaJarDependency;
import io.github.coolcrabs.brachyura.mappings.MappingHasher;
import io.github.coolcrabs.brachyura.mappings.Namespaces;
import io.github.coolcrabs.brachyura.mappings.tinyremapper.Jsr2JetbrainsMappingProvider;
import io.github.coolcrabs.brachyura.mappings.tinyremapper.MappingTreeMappingProvider;
import io.github.coolcrabs.brachyura.mappings.tinyremapper.PathFileConsumer;
import io.github.coolcrabs.brachyura.mappings.tinyremapper.TinyRemapperHelper;
import io.github.coolcrabs.brachyura.mappings.tinyremapper.TinyRemapperHelper.JarType;
import io.github.coolcrabs.brachyura.maven.Maven;
import io.github.coolcrabs.brachyura.minecraft.Minecraft;
import io.github.coolcrabs.brachyura.minecraft.VersionMeta;
import io.github.coolcrabs.brachyura.util.AtomicFile;
import io.github.coolcrabs.brachyura.util.FileSystemUtil;
import io.github.coolcrabs.brachyura.util.PathUtil;
import io.github.coolcrabs.brachyura.util.Util;
import io.github.coolcrabs.fabricmerge.JarMerger;
import net.fabricmc.mappingio.tree.MappingTree;
import net.fabricmc.tinyremapper.TinyRemapper;

public abstract class FabricProject {
    abstract String getMcVersion();
    abstract MappingTree getMappings();

    public final VersionMeta versionMeta = Minecraft.getVersion(getMcVersion());
    public final Path vanillaClientJar = Minecraft.getDownload(getMcVersion(), versionMeta, "client");
    public final Path vanillaServerJar = Minecraft.getDownload(getMcVersion(), versionMeta, "server");

    private RemappedJar intermediaryJar;
    private RemappedJar namedJar;

    public Intermediary getIntermediary() {
        return Intermediary.ofMaven(FabricMaven.URL, FabricMaven.intermediary(getMcVersion()));
    }

    public Path getMergedJar() {
        Path result = fabricCache().resolve("merged").resolve(getMcVersion() + "-merged.jar");
        if (!Files.isRegularFile(result)) {
            try (AtomicFile atomicFile = new AtomicFile(result)) {
                try {
                    try (JarMerger jarMerger = new JarMerger(vanillaClientJar, vanillaServerJar, atomicFile.tempPath)) {
                        jarMerger.enableSyntheticParamsOffset();
                        jarMerger.merge();
                    }
                } catch (IOException e) {
                    throw Util.sneak(e);
                }
                atomicFile.commit();
            }
        }
        return result;
    }

    public RemappedJar getIntermediaryJar() {
        if (intermediaryJar == null) {
            Path mergedJar = getMergedJar();
            Intermediary intermediary = getIntermediary();
            String intermediaryHash = MappingHasher.hashSha256(intermediary.tree);
            Path result = fabricCache().resolve("intermediary").resolve(getMcVersion() + "-intermediary-" + intermediaryHash + ".jar");
            if (Files.isRegularFile(result)) {
                intermediaryJar = new RemappedJar(result, intermediaryHash);
                return intermediaryJar;
            } else {
                try (AtomicFile atomicFile = new AtomicFile(result)) {
                    remapJar(intermediary.tree, Namespaces.OBF, Namespaces.INTERMEDIARY, mergedJar, atomicFile.tempPath, mcRemapClasspath());
                    atomicFile.commit();
                }
            }
            intermediaryJar = new RemappedJar(result, intermediaryHash);
        }
        return intermediaryJar;
    }

    public RemappedJar getNamedJar() {
        if (namedJar == null) {
            Path intermediaryJar2 = getIntermediaryJar().jar;
            MappingTree mappings = getMappings();
            Intermediary intermediary = getIntermediary();
            String mappingHash = MappingHasher.hashSha256(intermediary.tree, mappings);
            Path result = fabricCache().resolve("named").resolve(getMcVersion() + "-named-" + mappingHash + ".jar");
            if (Files.isRegularFile(result)) {
                namedJar = new RemappedJar(result, mappingHash);
                return namedJar;
            } else {
                try (AtomicFile atomicFile = new AtomicFile(result)) {
                    remapJar(mappings, Namespaces.INTERMEDIARY, Namespaces.NAMED, intermediaryJar2, atomicFile.tempPath, mcRemapClasspath());
                    atomicFile.commit();
                }
            }
            namedJar = new RemappedJar(result, mappingHash);
        }
        return namedJar;
    }

    public Path getDecompiledJar() {
        RemappedJar named = getNamedJar();
        MappingTree mappings = getMappings();
        BrachyuraDecompiler decompiler = decompiler();
        Path result = fabricCache().resolve("decompiled").resolve(getMcVersion() + "-named-" + named.mappingHash + "-decomp-" + decompiler.getName() + "-" + decompiler.getVersion() + ".jar");
        if (Files.isRegularFile(result)) {
            return result;
        } else {
            try (AtomicFile atomicFile = new AtomicFile(result)) {
                decompiler.decompile(named.jar, decompClasspath(), atomicFile.tempPath, null, mappings, mappings.getNamespaceId(Namespaces.NAMED));
                atomicFile.commit();
            }
        }
        return result;
    }

    public void remapJar(MappingTree mappings, String src, String dst, Path inputJar, Path outputJar, List<Path> classpath) {
        TinyRemapper remapper = TinyRemapper.newRemapper()
            .withMappings(new MappingTreeMappingProvider(mappings, src, dst))
            .withMappings(Jsr2JetbrainsMappingProvider.INSTANCE)
            .renameInvalidLocals(true)
            .rebuildSourceFilenames(true)
            .build();
        try {
            Files.deleteIfExists(outputJar);
        } catch (IOException e) {
            throw Util.sneak(e);
        }
        try (FileSystem outputFileSystem = FileSystemUtil.newJarFileSystem(outputJar)) {
            Path outputRoot = outputFileSystem.getPath("/");
            for (Path path : classpath) {
                TinyRemapperHelper.read(remapper, path, JarType.CLASSPATH);
            }
            try (FileSystem inputFileSystem = FileSystemUtil.newJarFileSystem(inputJar)) {
                TinyRemapperHelper.read(remapper, inputFileSystem, JarType.INPUT);
                TinyRemapperHelper.copyNonClassfiles(inputFileSystem, outputFileSystem);
            }
            remapper.apply(new PathFileConsumer(outputRoot));
        } catch (IOException e) {
            throw Util.sneak(e);
        } finally {
            remapper.finish();
        }
    }

    public List<Path> decompClasspath() {
        List<Path> result = new ArrayList<>(mcRemapClasspath());
        result.add(Maven.getMavenJarDep(FabricMaven.URL, FabricMaven.loader("0.9.3+build.207")).jar); // Just for the annotations added by fabric-merge
        return result;
    }

    public List<Path> mcRemapClasspath() {
        List<Dependency> dependencies = Minecraft.getDependencies(versionMeta);
        List<Path> result = new ArrayList<>();
        for (Dependency dependency : dependencies) {
            if (dependency instanceof JavaJarDependency) {
                result.add(((JavaJarDependency)dependency).jar);
            }
        }
        return result;
    }

    public BrachyuraDecompiler decompiler() {
        return CfrDecompiler.INSTANCE;
    }

    public Path fabricCache() {
        return PathUtil.cachePath().resolve("fabric");
    }

    public class RemappedJar {
        public final Path jar;
        public final String mappingHash;

        public RemappedJar(Path jar, String mappingHash) {
            this.jar = jar;
            this.mappingHash = mappingHash;
        }
    }
}
