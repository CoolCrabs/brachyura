package io.github.coolcrabs.brachyura.fabric;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;

import org.tinylog.Logger;

import io.github.coolcrabs.brachyura.compiler.java.JavaCompilationUnitBuilder;
import io.github.coolcrabs.brachyura.decompiler.BrachyuraDecompiler;
import io.github.coolcrabs.brachyura.decompiler.cfr.CfrDecompiler;
import io.github.coolcrabs.brachyura.dependency.Dependency;
import io.github.coolcrabs.brachyura.dependency.JavaJarDependency;
import io.github.coolcrabs.brachyura.dependency.NativesJarDependency;
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
import io.github.coolcrabs.brachyura.project.BaseJavaProject;
import io.github.coolcrabs.brachyura.util.AtomicDirectory;
import io.github.coolcrabs.brachyura.util.AtomicFile;
import io.github.coolcrabs.brachyura.util.FileSystemUtil;
import io.github.coolcrabs.brachyura.util.JvmUtil;
import io.github.coolcrabs.brachyura.util.Lazy;
import io.github.coolcrabs.brachyura.util.PathUtil;
import io.github.coolcrabs.brachyura.util.UnzipUtil;
import io.github.coolcrabs.brachyura.util.Util;
import io.github.coolcrabs.fabricmerge.JarMerger;
import io.github.coolcrabs.javacompilelib.JavaCompilationUnit;
import net.fabricmc.mappingio.tree.MappingTree;
import net.fabricmc.tinyremapper.TinyRemapper;

public abstract class FabricProject extends BaseJavaProject {
    abstract String getMcVersion();
    abstract MappingTree getMappings();
    abstract FabricLoader getLoader();
    abstract String getModId();
    abstract String getVersion();

    public final VersionMeta versionMeta = Minecraft.getVersion(getMcVersion());
    public final Path vanillaClientJar = Minecraft.getDownload(getMcVersion(), versionMeta, "client");
    public final Path vanillaServerJar = Minecraft.getDownload(getMcVersion(), versionMeta, "server");

    public void vscode() {
        Path vscode = getProjectDir().resolve(".vscode");
        Vscode.updateSettingsJson(vscode.resolve("settings.json"), getIdeDependencies());
        Vscode.LaunchJson launchJson = new Vscode.LaunchJson();
        Vscode.LaunchJson.Configuration server = new Vscode.LaunchJson.Configuration();
        server.name = "Minecraft Server";
        server.cwd = "${workspaceFolder}/run";
        server.mainClass = "net.fabricmc.devlaunchinjector.Main";
        server.vmArgs = "-Dfabric.dli.config=" + writeLaunchCfg().toAbsolutePath().toString() + " -Dfabric.dli.env=server -Dfabric.dli.main=net.fabricmc.loader.launch.knot.KnotServer";
        Vscode.LaunchJson.Configuration client = new Vscode.LaunchJson.Configuration();
        client.name = "Minecraft Client";
        client.cwd = "${workspaceFolder}/run";
        client.mainClass = "net.fabricmc.devlaunchinjector.Main";
        client.vmArgs = "-Dfabric.dli.config=" + writeLaunchCfg().toAbsolutePath().toString() + " -Dfabric.dli.env=client -Dfabric.dli.main=net.fabricmc.loader.launch.knot.KnotClient";
        launchJson.configurations = new Vscode.LaunchJson.Configuration[] {client, server};
        Vscode.updateLaunchJson(vscode.resolve("launch.json"), launchJson);
        PathUtil.resolveAndCreateDir(getProjectDir(), "run");
    }

    public Path writeLaunchCfg() {
        try {
            Path result = getLocalBrachyuraPath().resolve("launch.cfg");
            Files.deleteIfExists(result);
            try (BufferedWriter writer = Files.newBufferedWriter(result)) {
                writer.write("commonProperties\n");
                writer.write("\tfabric.development=true\n");
                //TOOD: fabric.remapClasspathFile
                writer.write("\tlog4j.configurationFile="); writer.write(writeLog4jXml().toAbsolutePath().toString()); writer.write('\n');
                writer.write("\tfabric.log.disableAnsi=false\n");
                writer.write("clientArgs\n");
                writer.write("\t--assetIndex\n");
                writer.write('\t'); writer.write(Minecraft.downloadAssets(versionMeta)); writer.write('\n');
                writer.write("\t--assetsDir\n");
                writer.write('\t'); writer.write(Minecraft.assets().toAbsolutePath().toString()); writer.write('\n');
                writer.write("clientProperties\n");
                StringBuilder natives = new StringBuilder();
                for (Path path : getExtractedNatives()) {
                    natives.append(path.toAbsolutePath().toString());
                    natives.append(File.pathSeparatorChar);
                }
                natives.setLength(natives.length() - 1);
                String natives2 = natives.toString();
                writer.write("\tjava.library.path="); writer.write(natives2); writer.write('\n');
                writer.write("\torg.lwjgl.librarypath="); writer.write(natives2); writer.write('\n');
            }
            return result;
        } catch (Exception e) {
            throw Util.sneak(e);
        }
    }

    public Path writeLog4jXml() {
        try {
            Path result = getLocalBrachyuraPath().resolve("log4j.xml");
            Files.deleteIfExists(result);
            Files.copy(this.getClass().getResourceAsStream("/log4j2.fabric.xml"), result);
            return result;
        } catch (Exception e) {
            throw Util.sneak(e);
        }
    }

    public List<Path> getExtractedNatives() {
        List<Path> result = new ArrayList<>();
        for (Dependency dependency : mcDependencies.get()) {
            if (dependency instanceof NativesJarDependency) {
                NativesJarDependency nativesJarDependency = (NativesJarDependency) dependency;
                Path target = Minecraft.mcCache().resolve("natives-cache").resolve(Minecraft.mcLibCache().relativize(nativesJarDependency.jar));
                if (!Files.isDirectory(target)) {
                    try (AtomicDirectory atomicDirectory = new AtomicDirectory(target)) {
                        UnzipUtil.unzipToDir(nativesJarDependency.jar, atomicDirectory.tempPath);
                        atomicDirectory.commit();
                    }
                }
                result.add(target);
            }
        }
        return result;
    }

    public boolean build() {
        String[] compileArgs = JvmUtil.compileArgs(JvmUtil.CURRENT_JAVA_VERSION, 8);
        JavaCompilationUnit javaCompilationUnit = new JavaCompilationUnitBuilder()
                .sourceDir(getSrcDir())
                .outputDir(getBuildClassesDir())
                .classpath(getCompileDependencies())
                .options(compileArgs)
                .build();
        if (!compile(javaCompilationUnit)) return false;
        try {
            Path target = getBuildJarPath();
            Files.deleteIfExists(target);
            try (AtomicFile atomicFile = new AtomicFile(target)) {
                Files.deleteIfExists(atomicFile.tempPath);
                TinyRemapper remapper = TinyRemapper.newRemapper().withMappings(new MappingTreeMappingProvider(getMappings(), Namespaces.NAMED, Namespaces.INTERMEDIARY)).build();
                for (Path path : getCompileDependencies()) {
                    TinyRemapperHelper.readJar(remapper, path, JarType.CLASSPATH);
                }
                TinyRemapperHelper.readDir(remapper, getBuildClassesDir(), JarType.INPUT);
                try (FileSystem outputFileSystem = FileSystemUtil.newJarFileSystem(atomicFile.tempPath)) {
                    remapper.apply(new PathFileConsumer(outputFileSystem.getPath("/")));
                    TinyRemapperHelper.copyNonClassfilesFromDir(getBuildResourcesDir(), outputFileSystem);
                }
                atomicFile.commit();
            }
            return true;
        } catch (Exception e) {
            throw Util.sneak(e);
        }
    }

    @Override
    public void processResource(Path relativePath, Path absolutePath, Path targetDir) throws IOException {
        if (relativePath.toString().equals("fabric.mod.json")) {
            Gson gson = new Gson();
            JsonObject fabricModJson = gson.fromJson(PathUtil.newBufferedReader(absolutePath), JsonObject.class);
            fabricModJson.addProperty("version", getVersion());
            try (JsonWriter jsonWriter = new JsonWriter(PathUtil.newBufferedWriter(targetDir.resolve(relativePath)))) {
                gson.toJson(fabricModJson, jsonWriter);
            }
        } else {
            super.processResource(relativePath, absolutePath, targetDir);
        }
    }

    @Override
    public List<Path> getCompileDependencies() {
        List<Path> result = new ArrayList<>();
        for (Dependency dependency : dependencies.get()) {
            if (dependency instanceof JavaJarDependency) {
                result.add(((JavaJarDependency) dependency).jar);
            }
        }
        result.add(namedJar.get().jar);
        return result;
    }

    public List<JavaJarDependency> getIdeDependencies() {
        List<JavaJarDependency> result = new ArrayList<>();
        for (Dependency dependency : dependencies.get()) {
            if (dependency instanceof JavaJarDependency) {
                result.add((JavaJarDependency) dependency);
            }
        }
        result.add(Maven.getMavenJarDep(FabricMaven.URL, FabricMaven.devLaunchInjector("0.2.1+build.8")));
        result.add(Maven.getMavenJarDep(Maven.MAVEN_CENTRAL, new MavenId("net.minecrell", "terminalconsoleappender", "1.2.0")));
        result.add(new JavaJarDependency(namedJar.get().jar, getDecompiledJar(), null)); // TODO: line number mappings
        return result;
    }

    public final Lazy<List<Dependency>> dependencies = new Lazy<>(this::createDependencies);
    public List<Dependency> createDependencies() {
        List<Dependency> result = new ArrayList<>(mcDependencies.get());
        FabricLoader floader = getLoader();
        result.add(floader.jar);
        Collections.addAll(result, floader.commonDeps);
        Collections.addAll(result, floader.serverDeps);
        Collections.addAll(result, floader.clientDeps);
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

    public final Lazy<RemappedJar> intermediaryjar = new Lazy<>(this::createIntermediaryJar);
    public RemappedJar createIntermediaryJar() {
            Path mergedJar = getMergedJar();
            Intermediary intermediary = getIntermediary();
            String intermediaryHash = MappingHasher.hashSha256(intermediary.tree);
            Path result = fabricCache().resolve("intermediary").resolve(getMcVersion() + "-intermediary-" + intermediaryHash + ".jar");
            if (!Files.isRegularFile(result)) {
                try (AtomicFile atomicFile = new AtomicFile(result)) {
                    remapJar(intermediary.tree, Namespaces.OBF, Namespaces.INTERMEDIARY, mergedJar, atomicFile.tempPath, mcClasspath.get());
                    atomicFile.commit();
                }
            }
            return new RemappedJar(result, intermediaryHash);
    }

    public final Lazy<RemappedJar> namedJar = new Lazy<>(this::createNamedJar);
    public RemappedJar createNamedJar() {
        Path intermediaryJar2 = intermediaryjar.get().jar;
        MappingTree mappings = getMappings();
        Intermediary intermediary = getIntermediary();
        String mappingHash = MappingHasher.hashSha256(intermediary.tree, mappings);
        Path result = fabricCache().resolve("named").resolve(getMcVersion() + "-named-" + mappingHash + ".jar");
        if (!Files.isRegularFile(result)) {
            try (AtomicFile atomicFile = new AtomicFile(result)) {
                remapJar(mappings, Namespaces.INTERMEDIARY, Namespaces.NAMED, intermediaryJar2, atomicFile.tempPath, mcClasspath.get());
                atomicFile.commit();
            }
        }
        return new RemappedJar(result, mappingHash);
    }

    public Path getDecompiledJar() {
        RemappedJar named = namedJar.get();
        MappingTree mappings = getMappings();
        BrachyuraDecompiler decompiler = decompiler();
        Path result = fabricCache().resolve("decompiled").resolve(getMcVersion() + "-named-" + named.mappingHash + "-decomp-" + decompiler.getName() + "-" + decompiler.getVersion() + "-sources.jar");
        Path result2 = fabricCache().resolve("decompiled").resolve(getMcVersion() + "-named-" + named.mappingHash + "-decomp-" + decompiler.getName() + "-" + decompiler.getVersion() + ".linemappings");
        if (!(Files.isRegularFile(result) && Files.isRegularFile(result2))) {
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
        List<Path> result = new ArrayList<>(mcClasspath.get());
        result.add(Maven.getMavenJarDep(FabricMaven.URL, FabricMaven.loader("0.9.3+build.207")).jar); // Just for the annotations added by fabric-merge
        return result;
    }

    public final Lazy<List<Path>> mcClasspath = new Lazy<>(this::createMcClasspath);
    public List<Path> createMcClasspath() {
        List<Dependency> deps = mcDependencies.get();
        List<Path> result = new ArrayList<>();
        for (Dependency dependency : deps) {
            if (dependency instanceof JavaJarDependency) {
                result.add(((JavaJarDependency)dependency).jar);
            }
        }
        return result;
    }

    public final Lazy<List<Dependency>> mcDependencies = new Lazy<>(this::createMcDependencies);
    public List<Dependency> createMcDependencies() {
        ArrayList<Dependency> result = new ArrayList<>(Minecraft.getDependencies(versionMeta));
        result.add(Maven.getMavenJarDep(Maven.MAVEN_CENTRAL, new MavenId("org.jetbrains", "annotations", "19.0.0")));
        return result;
    }

    public BrachyuraDecompiler decompiler() {
        return new CfrDecompiler(Runtime.getRuntime().availableProcessors());
    }

    public Path getBuildJarPath() {
        return getBuildLibsDir().resolve(getModId() + "-" + getVersion() + ".jar");
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
