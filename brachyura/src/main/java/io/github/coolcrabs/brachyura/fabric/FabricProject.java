package io.github.coolcrabs.brachyura.fabric;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;

import io.github.coolcrabs.brachyura.compiler.java.JavaCompilationUnitBuilder;
import io.github.coolcrabs.brachyura.decompiler.BrachyuraDecompiler;
import io.github.coolcrabs.brachyura.decompiler.DecompileLineNumberTable;
import io.github.coolcrabs.brachyura.decompiler.LineNumberTableReplacer;
import io.github.coolcrabs.brachyura.decompiler.cfr.CfrDecompiler;
import io.github.coolcrabs.brachyura.dependency.Dependency;
import io.github.coolcrabs.brachyura.dependency.JavaJarDependency;
import io.github.coolcrabs.brachyura.dependency.NativesJarDependency;
import io.github.coolcrabs.brachyura.exception.UnknownJsonException;
import io.github.coolcrabs.brachyura.ide.IdeProject;
import io.github.coolcrabs.brachyura.ide.IdeProject.IdeProjectBuilder;
import io.github.coolcrabs.brachyura.ide.IdeProject.RunConfig.RunConfigBuilder;
import io.github.coolcrabs.brachyura.mappings.BudgetSourceRemapper;
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
import io.github.coolcrabs.brachyura.project.Task;
import io.github.coolcrabs.brachyura.project.java.BaseJavaProject;
import io.github.coolcrabs.brachyura.util.ArrayUtil;
import io.github.coolcrabs.brachyura.util.AtomicDirectory;
import io.github.coolcrabs.brachyura.util.AtomicFile;
import io.github.coolcrabs.brachyura.util.FileSystemUtil;
import io.github.coolcrabs.brachyura.util.JvmUtil;
import io.github.coolcrabs.brachyura.util.Lazy;
import io.github.coolcrabs.brachyura.util.MessageDigestUtil;
import io.github.coolcrabs.brachyura.util.PathUtil;
import io.github.coolcrabs.brachyura.util.StreamUtil;
import io.github.coolcrabs.brachyura.util.UnzipUtil;
import io.github.coolcrabs.brachyura.util.Util;
import io.github.coolcrabs.fabricmerge.JarMerger;
import io.github.coolcrabs.javacompilelib.JavaCompilationUnit;
import net.fabricmc.accesswidener.AccessWidenerReader;
import net.fabricmc.accesswidener.AccessWidenerWriter;
import net.fabricmc.mappingio.format.Tiny2Writer;
import net.fabricmc.mappingio.tree.MappingTree;
import net.fabricmc.tinyremapper.InputTag;
import net.fabricmc.tinyremapper.TinyRemapper;

public abstract class FabricProject extends BaseJavaProject {
    public abstract String getMcVersion();
    public final Lazy<MappingTree> mappings = new Lazy<>(this::createMappings);
    public abstract MappingTree createMappings();
    public abstract FabricLoader getLoader();
    public abstract String getModId();
    public abstract String getVersion();
    public abstract void getModDependencies(ModDependencyCollector d);

    public static class ModDependencyCollector {
        public final List<ModDependency> dependencies = new ArrayList<>();

        public void addMaven(String repo, MavenId id, ModDependencyFlag... flags) {
            add(Maven.getMavenJarDep(repo, id), flags);
        }

        public void add(JavaJarDependency jarDependency, ModDependencyFlag... flags) {
            if (flags.length == 0) throw new UnsupportedOperationException("Must have atleast one dependency flag");
            EnumSet<ModDependencyFlag> flags2 = EnumSet.of(flags[0], flags); // Bruh
            dependencies.add(new ModDependency(jarDependency, flags2));
        }
    }

    public static class ModDependency {
        public final JavaJarDependency jarDependency;
        public final Set<ModDependencyFlag> flags;

        public ModDependency(JavaJarDependency jarDependency, Set<ModDependencyFlag> flags) {
            this.jarDependency = jarDependency;
            this.flags = flags;
        }
    }

    public enum ModDependencyFlag {
        COMPILE,
        RUNTIME
    }

    public final VersionMeta versionMeta = Minecraft.getVersion(getMcVersion());

    @Override
    public void getTasks(Consumer<Task> p) {
        super.getTasks(p);
        p.accept(Task.of("build", this::build));
    }

    @Override
    public IdeProject getIdeProject() {
        Path mappingsClasspath = writeMappings4FabricStuff().getParent().getParent();
        Path cwd = getProjectDir().resolve("run");
        PathUtil.createDirectories(cwd);
        List<JavaJarDependency> ideDependencies = getIdeDependencies();
        ArrayList<Path> classpath = new ArrayList<>(ideDependencies.size() + 1);
        for (JavaJarDependency dependency : ideDependencies) {
            classpath.add(dependency.jar);
        }
        classpath.add(mappingsClasspath);
        Path launchConfig = writeLaunchCfg();
        return new IdeProjectBuilder()
            .dependencies(getIdeDependencies())
            .sourcePaths(getSrcDir())
            .resourcePaths(getResourcesDir())
            .runConfigs(
                new RunConfigBuilder()
                    .name("Minecraft Client")
                    .cwd(cwd)
                    .mainClass("net.fabricmc.devlaunchinjector.Main")
                    .classpath(classpath)
                    .vmArgs(
                        "-Dfabric.dli.config=" + launchConfig.toString(),
                        "-Dfabric.dli.env=client",
                        "-Dfabric.dli.main=net.fabricmc.loader.launch.knot.KnotClient"
                    )
                .build(),
                new RunConfigBuilder()
                    .name("Minecraft Server")
                    .cwd(cwd)
                    .mainClass("net.fabricmc.devlaunchinjector.Main")
                    .classpath(classpath)
                    .vmArgs(
                        "-Dfabric.dli.config=" + launchConfig.toString(),
                        "-Dfabric.dli.env=server",
                        "-Dfabric.dli.main=net.fabricmc.loader.launch.knot.KnotServer"
                    )
                .build()
            )
        .build();
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

    public Path writeMappings4FabricStuff() {
        try {
            MappingTree mappingTree = mappings.get();
            String hash = MappingHasher.hashSha256(mappingTree);
            Path result = getLocalBrachyuraPath().resolve("mappings-cache").resolve(hash).resolve("mappings").resolve("mappings.tiny"); // floader hardcoded path as it asumes you are using a yarn jar as mapping root of truth
            if (!Files.isRegularFile(result)) {
                try (AtomicFile atomicFile = new AtomicFile(result)) {
                    try (Tiny2Writer tiny2Writer = new Tiny2Writer(Files.newBufferedWriter(atomicFile.tempPath), false)) {
                        mappingTree.accept(tiny2Writer);
                    }
                    atomicFile.commit();
                }
            }
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
        String[] compileArgs = ArrayUtil.join(String.class, 
            JvmUtil.compileArgs(JvmUtil.CURRENT_JAVA_VERSION, 8),
            "-AinMapFileNamedIntermediary=" + writeMappings4FabricStuff().toAbsolutePath().toString(),
            // "-AoutMapFileNamedIntermediary=" + getLocalBrachyuraPath() + "wat.tiny",
            "-AoutRefMapFile=" + getBuildResourcesDir().resolve(getModId() + "-refmap.json").toAbsolutePath().toString(),
            "-AdefaultObfuscationEnv=named:intermediary"
        );
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
                TinyRemapper remapper = TinyRemapper.newRemapper().withMappings(new MappingTreeMappingProvider(mappings.get(), Namespaces.NAMED, Namespaces.INTERMEDIARY)).build();
                try {
                    for (Path path : getCompileDependencies()) {
                        TinyRemapperHelper.readJar(remapper, path, JarType.CLASSPATH);
                    }
                    TinyRemapperHelper.readDir(remapper, getBuildClassesDir(), JarType.INPUT);
                    try (FileSystem outputFileSystem = FileSystemUtil.newJarFileSystem(atomicFile.tempPath)) {
                        remapper.apply(new PathFileConsumer(outputFileSystem.getPath("/")));
                        TinyRemapperHelper.copyNonClassfilesFromDir(getBuildResourcesDir(), outputFileSystem);
                    }
                } finally {
                    remapper.finish();
                }
                atomicFile.commit();
            }
            return true;
        } catch (Exception e) {
            throw Util.sneak(e);
        }
    }

    @Override
    public boolean processResources(Path source, Path target) throws IOException {
        // If you think this is bad look at what loom does
        Gson gson = new GsonBuilder().setPrettyPrinting().setLenient().create();
        Path fmj = source.resolve("fabric.mod.json");
        List<String> mixinjs = new ArrayList<>();
        List<Path> mixinFiles = new ArrayList<>();
        if (Files.isRegularFile(fmj)) {
            JsonObject fabricModJson;
            try (BufferedReader reader = PathUtil.newBufferedReader(fmj)) {
                fabricModJson = gson.fromJson(reader, JsonObject.class);
            }
            fabricModJson.addProperty("version", getVersion());
            JsonElement m = fabricModJson.get("mixins");
            if (m instanceof JsonArray) {
                JsonArray mixins = m.getAsJsonArray();
                for (JsonElement a : mixins) {
                    if (a.isJsonPrimitive()) {
                        mixinjs.add(a.getAsString());
                    } else if (a.isJsonObject()) {
                        mixinjs.add(a.getAsJsonObject().get("config").getAsString());
                    } else {
                        throw new UnknownJsonException(a.toString());
                    }
                }
            }
            try (BufferedWriter jsonWriter = PathUtil.newBufferedWriter(target.resolve("fabric.mod.json"))) {
                gson.toJson(fabricModJson, jsonWriter);
            }
        }
        for (String mixin : mixinjs) {
            Path mixinsource = source.resolve(mixin);
            mixinFiles.add(mixinsource);
            Path mixintarget = target.resolve(mixin);
            JsonObject mixinjson;
            try (BufferedReader bufferedReader = Files.newBufferedReader(mixinsource)) {
                mixinjson = gson.fromJson(bufferedReader, JsonObject.class);
            }
            if (mixinjson.get("refmap") == null) {
                mixinjson.addProperty("refmap", getModId() + "-refmap.json");
            }
            try (BufferedWriter jsonWriter = PathUtil.newBufferedWriter(mixintarget)) {
                gson.toJson(mixinjson, jsonWriter);
            }
        }
        boolean[] result = new boolean[] {true};
        Files.walkFileTree(source, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                // Skip fmj and mixin files (already copied)
                if (file.equals(fmj) || mixinFiles.contains(file) || processResource(source.relativize(file), file, target)) {
                    return FileVisitResult.CONTINUE;
                } else {
                    result[0] = false;
                    return FileVisitResult.TERMINATE;
                }
            }
        });
        return result[0];
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
        result.add(Maven.getMavenJarDep(FabricMaven.URL, FabricMaven.mixinCompileExtensions("0.4.4")).jar);
        for (ModDependency dep : remappedModDependencies.get()) {
            if (dep.flags.contains(ModDependencyFlag.COMPILE)) result.add(dep.jarDependency.jar);
        }
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
        result.add(getDecompiledJar());
        for (ModDependency d : remappedModDependencies.get()) {
            if (d.flags.contains(ModDependencyFlag.RUNTIME)) result.add(d.jarDependency);
        }
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

    public final Lazy<List<ModDependency>> remappedModDependencies = new Lazy<>(this::createRemappedModDependencies);
    /**
     * 🍝
     */
    public List<ModDependency> createRemappedModDependencies() {
        try {
            ModDependencyCollector d = new ModDependencyCollector();
            getModDependencies(d);
            List<ModDependency> unmapped = d.dependencies;
            if (unmapped == null) return Collections.emptyList();
            List<ModDependency> result = new ArrayList<>(unmapped.size());
            MessageDigest dephasher = MessageDigestUtil.messageDigest(MessageDigestUtil.SHA256);
            dephasher.update((byte) 3); // Bump this if the logic changes
            for (ModDependency dep : unmapped) {
                hashDep(dephasher, dep);
            }
            for (JavaJarDependency dep : mcClasspath.get()) {
                hashDep(dephasher, dep);
            }
            dephasher.update(namedJar.get().mappingHash.getBytes(StandardCharsets.UTF_8));
            dephasher.update(intermediaryjar.get().mappingHash.getBytes(StandardCharsets.UTF_8));
            String dephash = MessageDigestUtil.toHexHash(dephasher.digest());
            Path depdir = getLocalBrachyuraPath().resolve("deps");
            Path resultdir = depdir.resolve(dephash);
            if (!Files.isDirectory(resultdir)) {
                if (Files.isDirectory(depdir)) {
                    PathUtil.deleteDirectoryChildren(depdir);
                }
                try (AtomicDirectory a = new AtomicDirectory(resultdir)) {
                    TinyRemapper tr = TinyRemapper.newRemapper()
                        .withMappings(new MappingTreeMappingProvider(mappings.get(), Namespaces.INTERMEDIARY, Namespaces.NAMED))
                        .renameInvalidLocals(false)
                        .build();
                    TinyRemapperHelper.readJar(tr, intermediaryjar.get().jar, JarType.CLASSPATH);
                    for (JavaJarDependency dep : mcClasspath.get()) {
                        TinyRemapperHelper.readJar(tr, dep.jar, JarType.CLASSPATH);
                    }
                    class RemapInfo {
                        ModDependency dep;
                        FileSystem fs;
                        FileSystem outFs;
                        Path outPath;
                        InputTag tag;
                    }
                    List<RemapInfo> b = new ArrayList<>();
                    try {
                        for (ModDependency u : unmapped) {
                            RemapInfo ri = new RemapInfo();
                            b.add(ri);
                            ri.dep = u;
                            ri.tag = tr.createInputTag();
                            ri.outPath = a.tempPath.resolve(u.jarDependency.jar.getFileName().toString());
                            ri.fs = FileSystemUtil.newJarFileSystem(u.jarDependency.jar);
                            ri.outFs = FileSystemUtil.newJarFileSystem(ri.outPath);
                        }
                        Logger.info("Remapping " + b.size() + " mods");
                        for (RemapInfo ri : b) {
                            TinyRemapperHelper.readFileSystem(tr, ri.fs, JarType.INPUT, ri.tag);
                        }
                        for (RemapInfo ri : b) {
                            tr.apply(new PathFileConsumer(ri.outFs.getPath("/")), ri.tag);
                            // TODO strip jij?
                            // TODO add fmj for non mod for jij
                            Files.walkFileTree(ri.fs.getPath("/"), new SimpleFileVisitor<Path>() {
                                @Override
                                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                                    String fileString = file.toString();
                                    if (!fileString.endsWith(".class")) {
                                        Path target = ri.outFs.getPath("/").resolve(ri.fs.getPath("/").relativize(file).toString());
                                        Files.createDirectories(target.getParent());
                                        if (fileString.endsWith(".accesswidener")) {
                                            try (BufferedReader r = Files.newBufferedReader(file)) {
                                                // TODO this is dumb
                                                r.mark(20);
                                                int v = AccessWidenerReader.readVersion(r);
                                                r.reset();
                                                AccessWidenerWriter w = new AccessWidenerWriter(v);
                                                AccessWidenerNamespaceChanger nc = new AccessWidenerNamespaceChanger(w, mappings.get(), mappings.get().getNamespaceId(Namespaces.NAMED));
                                                new AccessWidenerReader(nc).read(r);
                                                Files.copy(new ByteArrayInputStream(w.write()), target);
                                            }
                                        } else {
                                            Files.copy(file, target);
                                        }
                                    }
                                    return FileVisitResult.CONTINUE;
                                }
                            });
                        }
                    } finally {
                        for (RemapInfo c : b) {
                            if (c.fs != null) c.fs.close();
                            if (c.outFs != null) c.outFs.close();
                        }
                        tr.finish();
                    }
                    BudgetSourceRemapper sourceRemapper = new BudgetSourceRemapper(mappings.get(), mappings.get().getNamespaceId(Namespaces.INTERMEDIARY), mappings.get().getNamespaceId(Namespaces.NAMED));
                    for (ModDependency u : unmapped) { 
                        if (u.jarDependency != null) {
                            Logger.info("Remapping " + u.jarDependency.sourcesJar.getFileName());
                            long start = System.currentTimeMillis();
                            Path target = a.tempPath.resolve(u.jarDependency.jar.getFileName().toString().replace(".jar", "-sources.jar"));
                            sourceRemapper.remapSourcesJar(u.jarDependency.sourcesJar, target);
                            long end = System.currentTimeMillis() - start;
                            Logger.info("Remapped " + u.jarDependency.sourcesJar.getFileName() + " in " + end + "ms");
                        }
                    }
                    a.commit();
                }
            }
            for (ModDependency unmapdep : unmapped) {
                result.add(
                    new ModDependency(
                        new JavaJarDependency(
                            resultdir.resolve(
                                unmapdep.jarDependency.jar.getFileName().toString()
                            ),
                            unmapdep.jarDependency.sourcesJar == null ? null : resultdir.resolve(unmapdep.jarDependency.jar.getFileName().toString().replace(".jar", "-sources.jar")),
                            unmapdep.jarDependency.mavenId
                        ),
                        unmapdep.flags
                    )
                );
            }
            return result;
        } catch (Exception e) {
            throw Util.sneak(e);
        }
    }

    public void hashDep(MessageDigest md, ModDependency dep) {
        hashDep(md, dep.jarDependency);
        for (ModDependencyFlag flag : dep.flags) {
            MessageDigestUtil.update(md, flag.ordinal());
        }
    }

    public void hashDep(MessageDigest md, JavaJarDependency dep) {
        if (dep.mavenId == null) {
            // Hash the id if it exists
            MessageDigestUtil.update(md, dep.mavenId.artifactId);
            MessageDigestUtil.update(md, dep.mavenId.groupId);
            MessageDigestUtil.update(md, dep.mavenId.version);
        } else {
            // Hash all the metadata if no id
            MessageDigestUtil.update(md, dep.jar.toAbsolutePath().toString());
            BasicFileAttributes attr;
            try {
                attr = Files.readAttributes(dep.jar, BasicFileAttributes.class);
                Instant time = attr.lastModifiedTime().toInstant();
                MessageDigestUtil.update(md, time.getEpochSecond());
                MessageDigestUtil.update(md, time.getNano());
                MessageDigestUtil.update(md, attr.size());
            } catch (IOException e) {
                Logger.warn(e);
            }
        }
    }

    public Intermediary getIntermediary() {
        return Intermediary.ofMaven(FabricMaven.URL, FabricMaven.intermediary(getMcVersion()));
    }

    public Path getMergedJar() {
        try {
            Path vanillaClientJar = Minecraft.getDownload(getMcVersion(), versionMeta, "client");
            Path vanillaServerJar = Minecraft.getDownload(getMcVersion(), versionMeta, "server");
            try (ZipFile file = new ZipFile(vanillaServerJar.toFile())) {
                ZipEntry entry = file.getEntry("META-INF/versions.list");
                if (entry != null) {
                    String jar;
                    try (InputStream is = file.getInputStream(entry)) {
                        jar = StreamUtil.readFullyAsString(is).split("\t")[2];
                    }
                    vanillaServerJar = fabricCache().resolve("serverextract").resolve(jar);
                    if (!Files.isRegularFile(vanillaServerJar)) {
                        try (
                            AtomicFile f = new AtomicFile(vanillaServerJar);
                            InputStream is = file.getInputStream(file.getEntry("META-INF/versions/" + jar))
                        ) {
                            Files.copy(is, f.tempPath, StandardCopyOption.REPLACE_EXISTING);
                            f.commit();
                        }
                    }
                }
            }
            Path result = fabricCache().resolve("merged").resolve(getMcVersion() + "-merged.jar");
            if (!Files.isRegularFile(result)) {
                try (AtomicFile atomicFile = new AtomicFile(result)) {
                    try (JarMerger jarMerger = new JarMerger(vanillaClientJar, vanillaServerJar, atomicFile.tempPath)) {
                        jarMerger.enableSyntheticParamsOffset();
                        jarMerger.merge();
                    }
                    atomicFile.commit();
                }
            }
            return result;
        } catch (Exception e) {
            throw Util.sneak(e);
        }
    }

    public final Lazy<RemappedJar> intermediaryjar = new Lazy<>(this::createIntermediaryJar);
    public RemappedJar createIntermediaryJar() {
            Path mergedJar = getMergedJar();
            Intermediary intermediary = getIntermediary();
            String intermediaryHash = MappingHasher.hashSha256(intermediary.tree);
            Path result = fabricCache().resolve("intermediary").resolve(getMcVersion() + "-intermediary-" + intermediaryHash + ".jar");
            if (!Files.isRegularFile(result)) {
                try (AtomicFile atomicFile = new AtomicFile(result)) {
                    remapJar(intermediary.tree, Namespaces.OBF, Namespaces.INTERMEDIARY, mergedJar, atomicFile.tempPath, mcClasspathPaths.get());
                    atomicFile.commit();
                }
            }
            return new RemappedJar(result, intermediaryHash);
    }

    public final Lazy<RemappedJar> namedJar = new Lazy<>(this::createNamedJar);
    public RemappedJar createNamedJar() {
        Path intermediaryJar2 = intermediaryjar.get().jar;
        Intermediary intermediary = getIntermediary();
        String mappingHash = MappingHasher.hashSha256(intermediary.tree, mappings.get());
        Path result = fabricCache().resolve("named").resolve(getMcVersion() + "-named-" + mappingHash + ".jar");
        if (!Files.isRegularFile(result)) {
            try (AtomicFile atomicFile = new AtomicFile(result)) {
                remapJar(mappings.get(), Namespaces.INTERMEDIARY, Namespaces.NAMED, intermediaryJar2, atomicFile.tempPath, mcClasspathPaths.get());
                atomicFile.commit();
            }
        }
        return new RemappedJar(result, mappingHash);
    }

    public JavaJarDependency getDecompiledJar() {
        RemappedJar named = namedJar.get();
        BrachyuraDecompiler decompiler = decompiler();
        if (decompiler == null) return new JavaJarDependency(named.jar, null, null);
        Path result = fabricCache().resolve("decompiled").resolve(getMcVersion() + "-named-" + named.mappingHash + "-decomp-" + decompiler.getName() + "-" + decompiler.getVersion() + "-sources.jar");
        Path result2 = fabricCache().resolve("decompiled").resolve(getMcVersion() + "-named-" + named.mappingHash + "-decomp-" + decompiler.getName() + "-" + decompiler.getVersion() + ".linemappings");
        if (!(Files.isRegularFile(result) && Files.isRegularFile(result2))) {
            try (
                AtomicFile atomicFile = new AtomicFile(result);
                AtomicFile atomicFile2 = new AtomicFile(result2);
            ) {
                Logger.info("Decompiling " + named.jar.getFileName() + " using " + decompiler.getName() + " " + decompiler.getVersion() + " with " + decompiler.getThreadCount() + " threads"); 
                long start = System.currentTimeMillis();
                decompiler.decompile(named.jar, decompClasspath(), atomicFile.tempPath, atomicFile2.tempPath, mappings.get(), mappings.get().getNamespaceId(Namespaces.NAMED));
                long end = System.currentTimeMillis();
                Logger.info("Decompiled " + named.jar.getFileName() + " in " + (end - start) + "ms");
                atomicFile.commit();
                atomicFile2.commit();
            }
        }
        Path result3 = fabricCache().resolve("decompiled").resolve(getMcVersion() + "-named-" + named.mappingHash + "-lineremapped-" + decompiler.getName() + "-" + decompiler.getVersion() + ".jar");
        if (!Files.isRegularFile(result3)) {
            LineNumberTableReplacer.replaceLineNumbers(named.jar, result3, new DecompileLineNumberTable().read(result2));
        }
        return new JavaJarDependency(result3, result, null);
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
        List<Path> result = new ArrayList<>(mcClasspath.get().size() + 1);
        for (JavaJarDependency dep : mcClasspath.get()) {
            result.add(dep.jar);
        }
        result.add(Maven.getMavenJarDep(FabricMaven.URL, FabricMaven.loader("0.9.3+build.207")).jar); // Just for the annotations added by fabric-merge
        return result;
    }
    
    public final Lazy<List<JavaJarDependency>> mcClasspath = new Lazy<>(this::createMcClasspath);
    public final Lazy<List<Path>> mcClasspathPaths = new Lazy<>(() -> {
        ArrayList<Path> result = new ArrayList<>(mcClasspath.get().size());
        for (JavaJarDependency dep : mcClasspath.get()) {
            result.add(dep.jar);
        }
        return result;
    });

    public List<JavaJarDependency> createMcClasspath() {
        List<Dependency> deps = mcDependencies.get();
        List<JavaJarDependency> result = new ArrayList<>();
        for (Dependency dependency : deps) {
            if (dependency instanceof JavaJarDependency) {
                result.add((JavaJarDependency)dependency);
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

    public @Nullable BrachyuraDecompiler decompiler() {
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
