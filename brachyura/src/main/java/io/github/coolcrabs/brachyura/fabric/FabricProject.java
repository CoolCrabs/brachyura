package io.github.coolcrabs.brachyura.fabric;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;

import org.tinylog.Logger;

import io.github.coolcrabs.brachyura.compiler.java.JavaCompilers;
import io.github.coolcrabs.brachyura.decompiler.BrachyuraDecompiler;
import io.github.coolcrabs.brachyura.decompiler.cfr.CfrDecompiler;
import io.github.coolcrabs.brachyura.dependency.Dependency;
import io.github.coolcrabs.brachyura.dependency.JavaJarDependency;
import io.github.coolcrabs.brachyura.ide.Vscode;
import io.github.coolcrabs.brachyura.mappings.MappingHasher;
import io.github.coolcrabs.brachyura.mappings.Namespaces;
import io.github.coolcrabs.brachyura.mappings.tinyremapper.Jsr2JetbrainsMappingProvider;
import io.github.coolcrabs.brachyura.mappings.tinyremapper.MappingTreeMappingProvider;
import io.github.coolcrabs.brachyura.mappings.tinyremapper.PathFileConsumer;
import io.github.coolcrabs.brachyura.mappings.tinyremapper.TinyRemapperHelper;
import io.github.coolcrabs.brachyura.mappings.tinyremapper.TinyRemapperHelper.JarType;
import io.github.coolcrabs.brachyura.maven.Maven;
import io.github.coolcrabs.brachyura.maven.MavenId;
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
    abstract JavaJarDependency getLoader();
    abstract Path getProjectDir();
    abstract String getModId();
    abstract String getVersion();

    public final VersionMeta versionMeta = Minecraft.getVersion(getMcVersion());
    public final Path vanillaClientJar = Minecraft.getDownload(getMcVersion(), versionMeta, "client");
    public final Path vanillaServerJar = Minecraft.getDownload(getMcVersion(), versionMeta, "server");

    private RemappedJar intermediaryJar;
    private RemappedJar namedJar;

    public void vscode() {
        Vscode.updateSettingsJson(getProjectDir().resolve(".vscode").resolve("settings.json"), getIdeDependencies());
    }

    public boolean compile() {
        try {
            Path buildClassesDir = getBuildClassesDir();
            Path buildResourcesDir = getBuildResourcesDir();
            PathUtil.deleteDirectoryChildren(buildResourcesDir);
            if (!JavaCompilers.compile(getSrcDir(), buildClassesDir, getCompileDependencies())) {
                return false;
            }
        
            Path resourcesDir = getResourcesDir();
            Files.walkFileTree(resourcesDir, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    processResource(resourcesDir.relativize(file), file, buildResourcesDir);
                    return FileVisitResult.CONTINUE;
                }
            });

            Path target = getBuildJarPath();
            Files.deleteIfExists(target);
            try (AtomicFile atomicFile = new AtomicFile(target)) {
                Files.deleteIfExists(atomicFile.tempPath);
                TinyRemapper remapper = TinyRemapper.newRemapper().withMappings(new MappingTreeMappingProvider(getMappings(), Namespaces.NAMED, Namespaces.INTERMEDIARY)).build();
                for (Path path : getCompileDependencies()) {
                    TinyRemapperHelper.readJar(remapper, path, JarType.CLASSPATH);
                }
                TinyRemapperHelper.readDir(remapper, buildClassesDir, JarType.INPUT);
                try (FileSystem outputFileSystem = FileSystemUtil.newJarFileSystem(atomicFile.tempPath)) {
                    remapper.apply(new PathFileConsumer(outputFileSystem.getPath("/")));
                    TinyRemapperHelper.copyNonClassfilesFromDir(buildResourcesDir, outputFileSystem);
                }
                atomicFile.commit();
            }

            return true;
        } catch (Exception e) {
            throw Util.sneak(e);
        }
    }

    public void processResource(Path relativePath, Path absolutePath, Path targetDir) throws IOException {
        if (relativePath.toString().equals("fabric.mod.json")) {
            Gson gson = new Gson();
            JsonObject fabricModJson = gson.fromJson(PathUtil.newBufferedReader(absolutePath), JsonObject.class);
            fabricModJson.addProperty("version", getVersion());
            try (JsonWriter jsonWriter = new JsonWriter(PathUtil.newBufferedWriter(targetDir.resolve(relativePath)))) {
                gson.toJson(fabricModJson, jsonWriter);
            }
        } else {
            Path target = targetDir.resolve(relativePath);
            Files.createDirectories(target.getParent());
            Files.copy(absolutePath, targetDir.resolve(relativePath));
        }
    }

    public List<Path> getCompileDependencies() {
        List<Path> result = new ArrayList<>();
        for (Dependency dependency : mcDependencies()) {
            if (dependency instanceof JavaJarDependency) {
                result.add(((JavaJarDependency) dependency).jar);
            }
        }
        result.add(getLoader().jar);
        result.add(getNamedJar().jar);
        return result;
    }

    public List<JavaJarDependency> getIdeDependencies() {
        List<JavaJarDependency> result = new ArrayList<>();
        for (Dependency dependency : mcDependencies()) {
            if (dependency instanceof JavaJarDependency) {
                result.add((JavaJarDependency) dependency);
            }
        }
        result.add(getLoader());
        result.add(new JavaJarDependency(getNamedJar().jar, getDecompiledJar(), null)); // TODO: line number mappings
        return result;
    }

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
                    remapJar(intermediary.tree, Namespaces.OBF, Namespaces.INTERMEDIARY, mergedJar, atomicFile.tempPath, mcClasspath());
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
                    remapJar(mappings, Namespaces.INTERMEDIARY, Namespaces.NAMED, intermediaryJar2, atomicFile.tempPath, mcClasspath());
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
        Path result = fabricCache().resolve("decompiled").resolve(getMcVersion() + "-named-" + named.mappingHash + "-decomp-" + decompiler.getName() + "-" + decompiler.getVersion() + "-sources.jar");
        Path result2 = fabricCache().resolve("decompiled").resolve(getMcVersion() + "-named-" + named.mappingHash + "-decomp-" + decompiler.getName() + "-" + decompiler.getVersion() + ".linemappings");
        if (Files.isRegularFile(result) && Files.isRegularFile(result2)) {
            return result;
        } else {
            try (
                AtomicFile atomicFile = new AtomicFile(result);
                AtomicFile atomicFile2 = new AtomicFile(result2);
            ) {
                Logger.info("Decompiling " + named.jar.getFileName() + " using " + decompiler.getName() + " " + decompiler.getVersion() + " with " + decompiler.getThreadCount() + " threads"); 
                long start = System.currentTimeMillis();
                decompiler.decompile(named.jar, decompClasspath(), atomicFile.tempPath, atomicFile2.tempPath, mappings, mappings.getNamespaceId(Namespaces.NAMED));
                long end = System.currentTimeMillis();
                Logger.info("Decompiled " + named.jar.getFileName() + " in " + (end - start) + "ms");
                atomicFile.commit();
                atomicFile2.commit();
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
                TinyRemapperHelper.readJar(remapper, path, JarType.CLASSPATH);
            }
            try (FileSystem inputFileSystem = FileSystemUtil.newJarFileSystem(inputJar)) {
                TinyRemapperHelper.readFileSystem(remapper, inputFileSystem, JarType.INPUT);
                TinyRemapperHelper.copyNonClassfilesFromFileSystem(inputFileSystem, outputFileSystem);
            }
            remapper.apply(new PathFileConsumer(outputRoot));
        } catch (IOException e) {
            throw Util.sneak(e);
        } finally {
            remapper.finish();
        }
    }

    public List<Path> decompClasspath() {
        List<Path> result = new ArrayList<>(mcClasspath());
        result.add(Maven.getMavenJarDep(FabricMaven.URL, FabricMaven.loader("0.9.3+build.207")).jar); // Just for the annotations added by fabric-merge
        return result;
    }

    public List<Path> mcClasspath() {
        List<Dependency> dependencies = mcDependencies();
        List<Path> result = new ArrayList<>();
        for (Dependency dependency : dependencies) {
            if (dependency instanceof JavaJarDependency) {
                result.add(((JavaJarDependency)dependency).jar);
            }
        }
        return result;
    }

    public List<Dependency> mcDependencies() {
        ArrayList<Dependency> result = new ArrayList<>(Minecraft.getDependencies(versionMeta));
        result.add(Maven.getMavenJarDep(Maven.MAVEN_CENTRAL, new MavenId("org.jetbrains", "annotations", "19.0.0")));
        return result;
    }

    public BrachyuraDecompiler decompiler() {
        return new CfrDecompiler(Runtime.getRuntime().availableProcessors());
    }

    public Path getBuildClassesDir() {
        return PathUtil.resolveAndCreateDir(getBuildDir(), "classes");
    }

    public Path getBuildResourcesDir() {
        return PathUtil.resolveAndCreateDir(getBuildDir(), "resources");
    }

    public Path getBuildLibsDir() {
        return PathUtil.resolveAndCreateDir(getBuildDir(), "libs");
    }

    public Path getBuildJarPath() {
        return getBuildLibsDir().resolve(getModId() + "-" + getVersion() + ".jar");
    }

    public Path getBuildDir() {
        return PathUtil.resolveAndCreateDir(getProjectDir(), "build");
    }

    public Path getSrcDir() {
        return getProjectDir().resolve("src").resolve("main").resolve("java");
    }

    public Path getResourcesDir() {
        return getProjectDir().resolve("src").resolve("main").resolve("resources");
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
