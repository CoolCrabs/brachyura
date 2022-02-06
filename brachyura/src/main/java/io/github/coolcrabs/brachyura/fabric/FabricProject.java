package io.github.coolcrabs.brachyura.fabric;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.tinylog.Logger;

import io.github.coolcrabs.brachyura.compiler.java.JavaCompilation;
import io.github.coolcrabs.brachyura.compiler.java.JavaCompilationResult;
import io.github.coolcrabs.brachyura.decompiler.BrachyuraDecompiler;
import io.github.coolcrabs.brachyura.decompiler.cfr.CfrDecompiler;
import io.github.coolcrabs.brachyura.dependency.Dependency;
import io.github.coolcrabs.brachyura.dependency.JavaJarDependency;
import io.github.coolcrabs.brachyura.dependency.NativesJarDependency;
import io.github.coolcrabs.brachyura.exception.UnknownJsonException;
import io.github.coolcrabs.brachyura.ide.IdeProject;
import io.github.coolcrabs.brachyura.ide.IdeProject.IdeProjectBuilder;
import io.github.coolcrabs.brachyura.ide.IdeProject.RunConfig.RunConfigBuilder;
import io.github.coolcrabs.brachyura.mappings.MappingHasher;
import io.github.coolcrabs.brachyura.mappings.MappingHelper;
import io.github.coolcrabs.brachyura.mappings.Namespaces;
import io.github.coolcrabs.brachyura.mappings.tinyremapper.Jsr2JetbrainsMappingProvider;
import io.github.coolcrabs.brachyura.mappings.tinyremapper.MappingTreeMappingProvider;
import io.github.coolcrabs.brachyura.mappings.tinyremapper.RemapperProcessor;
import io.github.coolcrabs.brachyura.mappings.tinyremapper.TinyRemapperHelper;
import io.github.coolcrabs.brachyura.maven.Maven;
import io.github.coolcrabs.brachyura.maven.MavenId;
import io.github.coolcrabs.brachyura.minecraft.Minecraft;
import io.github.coolcrabs.brachyura.minecraft.VersionMeta;
import io.github.coolcrabs.brachyura.mixin.BrachyuraMixinCompileExtensions;
import io.github.coolcrabs.brachyura.processing.ProcessingEntry;
import io.github.coolcrabs.brachyura.processing.ProcessingId;
import io.github.coolcrabs.brachyura.processing.ProcessingSink;
import io.github.coolcrabs.brachyura.processing.ProcessingSource;
import io.github.coolcrabs.brachyura.processing.Processor;
import io.github.coolcrabs.brachyura.processing.ProcessorChain;
import io.github.coolcrabs.brachyura.processing.sinks.AtomicZipProcessingSink;
import io.github.coolcrabs.brachyura.processing.sinks.ZipProcessingSink;
import io.github.coolcrabs.brachyura.processing.sources.DirectoryProcessingSource;
import io.github.coolcrabs.brachyura.processing.sources.ProcessingSponge;
import io.github.coolcrabs.brachyura.processing.sources.ZipProcessingSource;
import io.github.coolcrabs.brachyura.project.Task;
import io.github.coolcrabs.brachyura.project.java.BaseJavaProject;
import io.github.coolcrabs.brachyura.project.java.SimpleJavaProject;
import io.github.coolcrabs.brachyura.util.AtomicDirectory;
import io.github.coolcrabs.brachyura.util.AtomicFile;
import io.github.coolcrabs.brachyura.util.CloseableArrayList;
import io.github.coolcrabs.brachyura.util.GsonUtil;
import io.github.coolcrabs.brachyura.util.JvmUtil;
import io.github.coolcrabs.brachyura.util.Lazy;
import io.github.coolcrabs.brachyura.util.MessageDigestUtil;
import io.github.coolcrabs.brachyura.util.OsUtil;
import io.github.coolcrabs.brachyura.util.PathUtil;
import io.github.coolcrabs.brachyura.util.StreamUtil;
import io.github.coolcrabs.brachyura.util.UnzipUtil;
import io.github.coolcrabs.brachyura.util.Util;
import io.github.coolcrabs.brachyura.util.OsUtil.Os;
import io.github.coolcrabs.fabricmerge.JarMerger;
import io.github.coolmineman.trieharder.FindReplaceSourceRemapper;
import net.fabricmc.accesswidener.AccessWidener;
import net.fabricmc.accesswidener.AccessWidenerClassVisitor;
import net.fabricmc.accesswidener.AccessWidenerReader;
import net.fabricmc.accesswidener.AccessWidenerVisitor;
import net.fabricmc.accesswidener.AccessWidenerWriter;
import net.fabricmc.mappingio.MappingReader;
import net.fabricmc.mappingio.adapter.MappingSourceNsSwitch;
import net.fabricmc.mappingio.format.MappingFormat;
import net.fabricmc.mappingio.format.Tiny2Writer;
import net.fabricmc.mappingio.tree.MappingTree;
import net.fabricmc.mappingio.tree.MemoryMappingTree;
import net.fabricmc.tinyremapper.TinyRemapper;

public abstract class FabricProject extends BaseJavaProject {
    public abstract String getMcVersion();
    public final Lazy<MappingTree> mappings = new Lazy<>(this::createMappings);
    public abstract MappingTree createMappings();
    public abstract FabricLoader getLoader();
    public String getMavenGroup() {
        return null;
    }
    public @Nullable Consumer<AccessWidenerVisitor> getAw() {
        return null;
    }
    public MavenId getId() {
        return getMavenGroup() == null ? null : new MavenId(getMavenGroup(), getModId(), getVersion());
    }

    public final Lazy<List<ModDependency>> modDependencies = new Lazy<>(() -> {
        ModDependencyCollector d = new ModDependencyCollector();
        getModDependencies(d);
        return d.dependencies;
    });
    public abstract void getModDependencies(ModDependencyCollector d);

    public String getModId() {
        return fmjParseThingy.get()[0];
    }

    public String getVersion() {
        return fmjParseThingy.get()[1];
    }
    
    public MappingTree createMojmap() {
        return createMojmap(intermediary.get(), getMcVersion());
    }

    public static MappingTree createMojmap(MappingTree intermediary, String mcVersion) {
        try {
            MemoryMappingTree r = new MemoryMappingTree(true);
            intermediary.accept(r);
            Minecraft.getMojmap(mcVersion, Minecraft.getVersion(mcVersion)).accept(r);
            MappingHelper.dropNullInNamespace(r, Namespaces.INTERMEDIARY);
            return r;
        } catch (IOException e) {
            throw Util.sneak(e);
        }
    }

    private Lazy<String[]> fmjParseThingy = new Lazy<>(() -> {
        try {
            Gson gson = new GsonBuilder().setPrettyPrinting().setLenient().create();
            JsonObject fabricModJson;
            try (BufferedReader reader = PathUtil.newBufferedReader(getResourcesDir().resolve("fabric.mod.json"))) {
                fabricModJson = gson.fromJson(reader, JsonObject.class);
            }
            return new String[] {fabricModJson.get("id").getAsString(), fabricModJson.get("version").getAsString()};
        } catch (Exception e) {
            throw Util.sneak(e);
        }
    });

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
        RUNTIME,
        JIJ
    }

    public final VersionMeta versionMeta = Minecraft.getVersion(getMcVersion());

    @Override
    public void getTasks(Consumer<Task> p) {
        super.getTasks(p);
        p.accept(Task.of("build", this::build));
    }
    
    public void getPublishTasks(Consumer<Task> p) {
        SimpleJavaProject.createPublishTasks(p, this::build);
    }

    @Override
    public IdeProject getIdeProject() {
        Path cwd = PathUtil.resolveAndCreateDir(getProjectDir(), "run");
        Lazy<List<Path>> classpath = new Lazy<>(() -> {
            Path mappingsClasspath = writeMappings4FabricStuff().getParent().getParent();
            ArrayList<Path> r = new ArrayList<>(runtimeDependencies.get().size() + 1);
            for (JavaJarDependency dependency : runtimeDependencies.get()) {
                r.add(dependency.jar);
            }
            r.add(mappingsClasspath);
            return r;
        });
        Lazy<Path> launchConfig = new Lazy<>(this::writeLaunchCfg);
        return new IdeProjectBuilder()
            .name(getModId())
            .javaVersion(getJavaVersion())
            .dependencies(ideDependencies)
            .sourcePath(getSrcDir())
            .resourcePaths(getResourcesDir())
            .runConfigs(
                new RunConfigBuilder()
                    .name("Minecraft Client")
                    .cwd(cwd)
                    .mainClass("net.fabricmc.devlaunchinjector.Main")
                    .classpath(classpath)
                    .resourcePaths(getResourcesDir())
                    .vmArgs(
                        () -> {
                            ArrayList<String> clientArgs = new ArrayList<>(Arrays.asList(
                                "-Dfabric.dli.config=" + launchConfig.get().toString(),
                                "-Dfabric.dli.env=client",
                                "-Dfabric.dli.main=net.fabricmc.loader.launch.knot.KnotClient"
                            ));
                            if (OsUtil.OS == Os.OSX) {
                                clientArgs.add("-XstartOnFirstThread");
                            }
                            return clientArgs;
                        }
                    )
                .build(),
                new RunConfigBuilder()
                    .name("Minecraft Server")
                    .cwd(cwd)
                    .mainClass("net.fabricmc.devlaunchinjector.Main")
                    .classpath(classpath)
                    .resourcePaths(getResourcesDir())
                    .vmArgs(
                        () -> Arrays.asList(
                            "-Dfabric.dli.config=" + launchConfig.get().toString(),
                            "-Dfabric.dli.env=server",
                            "-Dfabric.dli.main=net.fabricmc.loader.launch.knot.KnotServer"
                        )
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
                writer.write("\tfabric.remapClasspathFile="); writer.write(writeRuntimeRemapClasspath().toString()); writer.write('\n');
                //TOOD: fabric.remapClasspathFile
                writer.write("\tlog4j.configurationFile="); writer.write(writeLog4jXml().toAbsolutePath().toString()); writer.write('\n');
                writer.write("\tlog4j2.formatMsgNoLookups=true\n"); // Prob overkill but won't hurt
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

    public Path writeRuntimeRemapClasspath() throws IOException {
        List<Path> result = new ArrayList<>();
        for (Dependency dependency : dependencies.get()) {
            if (dependency instanceof JavaJarDependency) {
                result.add(((JavaJarDependency) dependency).jar);
            }
        }
        for (ModDependency dep : modDependencies.get()) {
            result.add(dep.jarDependency.jar);
        }
        result.add(intermediaryjar.get().jar);
        Path target = PathUtil.resolveAndCreateDir(getLocalBrachyuraPath(), "remapclasspath").resolve("remapClasspath.txt");
        Files.deleteIfExists(target);
        Files.copy(new ByteArrayInputStream(result.stream().map(Path::toString).collect(Collectors.joining(File.pathSeparator)).getBytes(StandardCharsets.UTF_8)), target);
        return target;
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

    public JavaJarDependency build() {
        try {
            String mixinOut = "mixinmapout.tiny";
            JavaCompilationResult compilation = new JavaCompilation()
                .addOption(JvmUtil.compileArgs(JvmUtil.CURRENT_JAVA_VERSION, getJavaVersion()))
                .addOption(
                    "-AbrachyuraInMap=" + writeMappings4FabricStuff().toAbsolutePath().toString(),
                    "-AbrachyuraOutMap=" + mixinOut, // Remaps shadows etc
                    "-AbrachyuraInNamespace=" + Namespaces.NAMED,
                    "-AbrachyuraOutNamespace=" + Namespaces.INTERMEDIARY,
                    "-AoutRefMapFile=" + getModId() + "-refmap.json", // Remaps annotations
                    "-AdefaultObfuscationEnv=brachyura"
                )
                .addClasspath(getCompileDependencies())
                .addSourceDir(getSrcDir())
                .compile();
            ProcessingSponge compilationOutput = new ProcessingSponge();
            compilation.getInputs(compilationOutput);
            MemoryMappingTree compmappings = new MemoryMappingTree(true);
            mappings.get().accept(new MappingSourceNsSwitch(compmappings, Namespaces.NAMED));
            ProcessingEntry mixinMappings = compilationOutput.popEntry(mixinOut);
            if (mixinMappings != null) {
                try (Reader reader = new InputStreamReader(mixinMappings.in.get())) {
                    // For easier debugging a seperate tree is made here
                    MemoryMappingTree mixinMappingsTree = new MemoryMappingTree();
                    MappingReader.read(reader, MappingFormat.TINY_2, mixinMappingsTree);
                    mixinMappingsTree.accept(compmappings);
                }
            }
            ProcessingSponge trout = new ProcessingSponge();
            new ProcessorChain(
                new RemapperProcessor(TinyRemapper.newRemapper().withMappings(new MappingTreeMappingProvider(compmappings, Namespaces.NAMED, Namespaces.INTERMEDIARY)), getCompileDependencies())
            ).apply(trout, compilationOutput);
            try (AtomicZipProcessingSink out = new AtomicZipProcessingSink(getBuildJarPath())) {
                resourcesProcessingChain().apply(out, new DirectoryProcessingSource(getResourcesDir()));
                trout.getInputs(out);
                out.commit();
            }
            return new JavaJarDependency(getBuildJarPath(), null, getId());
        } catch (Exception e) {
            throw Util.sneak(e);
        }
    }

    public enum FMJRefmapApplier implements Processor {
        INSTANCE;

        @Override
        public void process(Collection<ProcessingEntry> inputs, ProcessingSink sink) throws IOException {
            HashMap<String, ProcessingEntry> entries = new HashMap<>();
            for (ProcessingEntry e : inputs) {
                entries.put(e.id.path, e);
            }
            ProcessingEntry fmj = entries.get("fabric.mod.json");
            if (fmj != null) {
                Gson gson = new GsonBuilder().setPrettyPrinting().setLenient().create();
                List<String> mixinjs = new ArrayList<>();
                JsonObject fabricModJson;
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(fmj.in.get(), StandardCharsets.UTF_8))) {
                    fabricModJson = gson.fromJson(reader, JsonObject.class);
                }
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
                for (String mixin : mixinjs) {
                    ProcessingEntry entry = entries.get(mixin);
                    entries.remove(mixin);
                    JsonObject mixinjson;
                    try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(entry.in.get(), StandardCharsets.UTF_8))) {
                        mixinjson = gson.fromJson(bufferedReader, JsonObject.class);
                    }
                    if (mixinjson.get("refmap") == null) {
                        mixinjson.addProperty("refmap", fabricModJson.get("id").getAsString() + "-refmap.json");
                    }
                    sink.sink(() -> GsonUtil.toIs(mixinjson, gson), entry.id);
                }
            }
            entries.forEach((k, v) -> sink.sink(v.in, v.id));
        }
    }

    public static class FmjJijApplier implements Processor {
        final List<Path> jij;

        public FmjJijApplier(List<Path> jij) {
            this.jij = jij;
        }

        @Override
        public void process(Collection<ProcessingEntry> inputs, ProcessingSink sink) throws IOException {
            for (ProcessingEntry e : inputs) {
                if (!jij.isEmpty() && "fabric.mod.json".equals(e.id.path)) {
                    Gson gson = new GsonBuilder().setPrettyPrinting().setLenient().create();
                    JsonObject fabricModJson;
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(e.in.get(), StandardCharsets.UTF_8))) {
                        fabricModJson = gson.fromJson(reader, JsonObject.class);
                    }
                    JsonArray jars = new JsonArray();
                    fabricModJson.add("jars", jars);
                    List<String> used = new ArrayList<>();
                    for (Path jar : jij) {
                        String path = "META-INF/jars/" + jar.getFileName();
                        int a = 0;
                        while (used.contains(path)) {
                            path = "META-INF/jars/" + a + jar.getFileName();
                            a++;
                        }
                        JsonObject o = new JsonObject();
                        o.addProperty("file", path);
                        jars.add(o);
                        used.add(path);
                        sink.sink(() -> PathUtil.inputStream(jar), new ProcessingId(path, e.id.source));
                    }
                    sink.sink(() -> GsonUtil.toIs(fabricModJson, gson), e.id);
                } else {
                    sink.sink(e.in, e.id);
                }
            }
        }
    }

    public static class AccessWidenerRemapper implements Processor {
        final MappingTree mappings;
        final int namespace;

        public AccessWidenerRemapper(MappingTree mappings, int namespace) {
            this.mappings = mappings;
            this.namespace = namespace;
        }

        @Override
        public void process(Collection<ProcessingEntry> inputs, ProcessingSink sink) throws IOException {
            HashMap<ProcessingId, ProcessingEntry> entries = new HashMap<>();
            ArrayList<ProcessingId> aws = new ArrayList<>();
            for (ProcessingEntry e : inputs) {
                entries.put(e.id, e);
                if (e.id.path.equals("fabric.mod.json")) {
                    JsonObject fabricModJson;
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(e.in.get(), StandardCharsets.UTF_8))) {
                        fabricModJson = new Gson().fromJson(reader, JsonObject.class);
                    }
                    JsonElement aw0 = fabricModJson.get("accessWidener");
                    if (aw0 != null) {
                        aws.add(new ProcessingId(aw0.getAsString(), e.id.source));
                    }
                }
            }
            for (ProcessingId awid : aws) {
                ProcessingEntry aw = entries.remove(awid);
                byte[] awb;
                try (InputStream is = aw.in.get()) {
                    awb = StreamUtil.readFullyAsBytes(is);
                }
                AccessWidenerWriter w = new AccessWidenerWriter(AccessWidenerReader.readVersion(awb));
                AccessWidenerNamespaceChanger nc = new AccessWidenerNamespaceChanger(w, mappings, namespace, aw.id.path);
                new AccessWidenerReader(nc).read(awb);
                sink.sink(() -> new ByteArrayInputStream(w.write()), aw.id);
            }
            for (ProcessingEntry e : entries.values()) {
                sink.sink(e.in, e.id);
            }
        }
    }

    // https://github.com/FabricMC/fabric-loom/blob/dev/0.11/src/main/java/net/fabricmc/loom/build/nesting/IncludedJarFactory.java
    public static class FmjGenerator implements Processor {
        final Map<ProcessingSource, MavenId> map;

        public FmjGenerator(Map<ProcessingSource, MavenId> map) {
            this.map = map;
        }

        @Override
        public void process(Collection<ProcessingEntry> inputs, ProcessingSink sink) throws IOException {
            HashSet<ProcessingSource> fmj = new HashSet<>();
            for (ProcessingEntry e : inputs) {
                if ("fabric.mod.json".equals(e.id.path)) {
                    fmj.add(e.id.source);
                }
                sink.sink(e.in, e.id);
            }
            for (Map.Entry<ProcessingSource, MavenId> e : map.entrySet()) {
                if (!fmj.contains(e.getKey())) {
                    Logger.info("Generating fmj for {}", e.getValue());
                    Gson gson = new GsonBuilder().setPrettyPrinting().create();
                    String modId = (e.getValue().groupId + "_" + e.getValue().artifactId).replace('.', '_').toLowerCase(Locale.ENGLISH);
                    if (modId.length() > 64) {
                        MessageDigest md = MessageDigestUtil.messageDigest(MessageDigestUtil.SHA256);
                        MessageDigestUtil.update(md, modId);
                        modId = modId.substring(0, 50) + MessageDigestUtil.toHexHash(md.digest()).substring(0, 14);
                    }
                    final JsonObject jsonObject = new JsonObject();
                    jsonObject.addProperty("schemaVersion", 1);
                    jsonObject.addProperty("id", modId);
                    jsonObject.addProperty("version", e.getValue().version);
                    jsonObject.addProperty("name", e.getValue().artifactId);
                    JsonObject custom = new JsonObject();
                    custom.addProperty("fabric-loom:generated", true);
                    jsonObject.add("custom", custom);
                    sink.sink(() -> GsonUtil.toIs(jsonObject, gson), new ProcessingId("fabric.mod.json", e.getKey()));
                }
            }
        }
    }
    
    public enum JijRemover implements Processor {
        INSTANCE;

        @Override
        public void process(Collection<ProcessingEntry> inputs, ProcessingSink sink) throws IOException {
            for (ProcessingEntry e : inputs) {
                if ("fabric.mod.json".equals(e.id.path)) {
                    Gson gson = new GsonBuilder().setPrettyPrinting().setLenient().create();
                    JsonObject fabricModJson;
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(e.in.get(), StandardCharsets.UTF_8))) {
                        fabricModJson = gson.fromJson(reader, JsonObject.class);
                    }
                    fabricModJson.remove("jars");
                    sink.sink(() -> GsonUtil.toIs(fabricModJson, gson), e.id);
                } else {
                    sink.sink(e.in, e.id);
                }
            }
        }
    }

    @Override
    public ProcessorChain resourcesProcessingChain() {
        Path fmjgen = getLocalBrachyuraPath().resolve("fmjgen");
        if (Files.exists(fmjgen)) PathUtil.deleteDirectory(fmjgen);
        List<Path> jij = new ArrayList<>();
        for (ModDependency modDependency : modDependencies.get()) {
            if (modDependency.flags.contains(ModDependencyFlag.JIJ)) {
                try {
                    try (ZipFile f = new ZipFile(modDependency.jarDependency.jar.toFile())) {
                        if (f.getEntry("fabric.mod.json") == null) {
                            Path p = fmjgen.resolve(modDependency.jarDependency.jar.getFileName());
                            try (
                                ZipProcessingSource s = new ZipProcessingSource(modDependency.jarDependency.jar);
                                AtomicZipProcessingSink sink = new AtomicZipProcessingSink(p)
                            ) {
                                new ProcessorChain(new FmjGenerator(Collections.singletonMap(s, modDependency.jarDependency.mavenId))).apply(sink, s);
                                sink.commit();
                            }
                            jij.add(p);
                        } else {
                            jij.add(modDependency.jarDependency.jar);
                        }
                    }
                } catch (Exception e) {
                    throw Util.sneak(e);
                }
            }
        }
        return new ProcessorChain(FMJRefmapApplier.INSTANCE, new FmjJijApplier(jij), new AccessWidenerRemapper(mappings.get(), mappings.get().getNamespaceId(Namespaces.INTERMEDIARY)));
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
        result.add(BrachyuraMixinCompileExtensions.getJar());
        for (ModDependency dep : remappedModDependencies.get()) {
            if (dep.flags.contains(ModDependencyFlag.COMPILE)) result.add(dep.jarDependency.jar);
        }
        return result;
    }

    public final Lazy<List<JavaJarDependency>> ideDependencies = new Lazy<>(this::createIdeDependencies);
    public List<JavaJarDependency> createIdeDependencies() {
        List<JavaJarDependency> result = new ArrayList<>();
        for (Dependency dependency : dependencies.get()) {
            if (dependency instanceof JavaJarDependency) {
                result.add((JavaJarDependency) dependency);
            }
        }
        result.add(Maven.getMavenJarDep(FabricMaven.URL, FabricMaven.devLaunchInjector("0.2.1+build.8"))); // vscode moment
        result.add(decompiledJar.get());
        for (ModDependency d : remappedModDependencies.get()) {
            if (d.flags.contains(ModDependencyFlag.COMPILE)) result.add(d.jarDependency);
        }
        return result;
    }

    public final Lazy<List<JavaJarDependency>> runtimeDependencies = new Lazy<>(this::createRuntimeDependencies);
    public List<JavaJarDependency> createRuntimeDependencies() {
        List<JavaJarDependency> result = new ArrayList<>();
        for (Dependency dependency : dependencies.get()) {
            if (dependency instanceof JavaJarDependency) {
                result.add((JavaJarDependency) dependency);
            }
        }
        result.add(Maven.getMavenJarDep(FabricMaven.URL, FabricMaven.devLaunchInjector("0.2.1+build.8")));
        result.add(Maven.getMavenJarDep(Maven.MAVEN_CENTRAL, new MavenId("net.minecrell", "terminalconsoleappender", "1.2.0")));
        result.add(decompiledJar.get());
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
        class RemapInfo {
            ModDependency source;
            ModDependency target;
        }
        try {
            List<ModDependency> unmapped = modDependencies.get();
            if (unmapped == null || unmapped.isEmpty()) return Collections.emptyList();
            List<RemapInfo> remapinfo = new ArrayList<>(unmapped.size());
            List<ModDependency> remapped = new ArrayList<>(unmapped.size());
            MessageDigest dephasher = MessageDigestUtil.messageDigest(MessageDigestUtil.SHA256);
            dephasher.update((byte) 7); // Bump this if the logic changes
            for (ModDependency dep : unmapped) {
                hashDep(dephasher, dep);
            }
            for (JavaJarDependency dep : mcClasspath.get()) {
                hashDep(dephasher, dep);
            }
            dephasher.update(namedJar.get().mappingHash.getBytes(StandardCharsets.UTF_8));
            dephasher.update(intermediaryjar.get().mappingHash.getBytes(StandardCharsets.UTF_8));
            MessageDigestUtil.update(dephasher, TinyRemapperHelper.VERSION);
            String dephash = MessageDigestUtil.toHexHash(dephasher.digest());
            Path depdir = getLocalBrachyuraPath().resolve("deps");
            Path resultdir = depdir.resolve(dephash);
            for (ModDependency u : unmapped) {
                RemapInfo ri = new RemapInfo();
                remapinfo.add(ri);
                ri.source = u;
                ri.target = new ModDependency(
                    new JavaJarDependency(
                        resultdir.resolve(
                            u.jarDependency.jar.getFileName().toString()
                        ),
                        u.jarDependency.sourcesJar == null ? null : resultdir.resolve(u.jarDependency.jar.getFileName().toString().replace(".jar", "-sources.jar")),
                        u.jarDependency.mavenId
                    ),
                    u.flags
                );
                remapped.add(ri.target);
            }
            if (!Files.isDirectory(resultdir)) {
                if (Files.isDirectory(depdir)) {
                    PathUtil.deleteDirectoryChildren(depdir);
                }
                try (AtomicDirectory a = new AtomicDirectory(resultdir)) {
                    TinyRemapper.Builder tr = TinyRemapper.newRemapper()
                        .withMappings(new MappingTreeMappingProvider(mappings.get(), Namespaces.INTERMEDIARY, Namespaces.NAMED))
                        .renameInvalidLocals(false);
                    ArrayList<Path> cp = new ArrayList<>();
                    cp.add(intermediaryjar.get().jar);
                    for (JavaJarDependency dep : mcClasspath.get()) {
                        cp.add(dep.jar);
                    }
                    HashMap<ProcessingSource, ZipProcessingSink> b = new HashMap<>();
                    HashMap<ProcessingSource, MavenId> c = new HashMap<>();
                    try (CloseableArrayList toClose = new CloseableArrayList()) {
                        for (RemapInfo ri : remapinfo) {
                            ZipProcessingSource s = new ZipProcessingSource(ri.source.jarDependency.jar);
                            toClose.add(s);
                            ZipProcessingSink si = new ZipProcessingSink(a.tempPath.resolve(ri.target.jarDependency.jar.getFileName()));
                            toClose.add(si);
                            b.put(s, si);
                            c.put(s, ri.source.jarDependency.mavenId);
                        }
                        Logger.info("Remapping {} mods", b.size());
                        new ProcessorChain(
                            new RemapperProcessor(tr, cp),
                            JijRemover.INSTANCE,
                            new AccessWidenerRemapper(mappings.get(), mappings.get().getNamespaceId(Namespaces.NAMED)),
                            new FmjGenerator(c)
                        ).apply(
                            (in, id) -> b.get(id.source).sink(in, id),
                            b.keySet()
                        );
                    }
                    FindReplaceSourceRemapper sourceRemapper = new FindReplaceSourceRemapper(mappings.get(), mappings.get().getNamespaceId(Namespaces.INTERMEDIARY), mappings.get().getNamespaceId(Namespaces.NAMED));
                    for (ModDependency u : unmapped) { 
                        if (u.jarDependency.sourcesJar != null) {
                            Path target = a.tempPath.resolve(u.jarDependency.jar.getFileName().toString().replace(".jar", "-sources.jar"));
                            sourceRemapper.remapSourcesJar(u.jarDependency.sourcesJar, target);
                        }
                    }
                    a.commit();
                }
            }
            return remapped;
        } catch (Exception e) {
            throw Util.sneak(e);
        }
    }

    public void hashDep(MessageDigest md, ModDependency dep) {
        hashDep(md, dep.jarDependency);
        for (ModDependencyFlag flag : dep.flags) {
            MessageDigestUtil.update(md, flag.toString());
        }
    }

    public void hashDep(MessageDigest md, JavaJarDependency dep) {
        if (dep.mavenId == null) {
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
        } else {
            // Hash the id if it exists
            MessageDigestUtil.update(md, dep.mavenId.artifactId);
            MessageDigestUtil.update(md, dep.mavenId.groupId);
            MessageDigestUtil.update(md, dep.mavenId.version);
            MessageDigestUtil.update(md, (byte)(dep.sourcesJar == null ? 0 : 1));
        }
    }

    public final Lazy<MappingTree> intermediary = new Lazy<>(this::createIntermediary);
    public MappingTree createIntermediary() {
        return Intermediary.ofMaven(FabricMaven.URL, FabricMaven.intermediary(getMcVersion())).tree;
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
            String intermediaryHash = MappingHasher.hashSha256(intermediary.get());
            Path result = fabricCache().resolve("intermediary").resolve(getMcVersion() + TinyRemapperHelper.getFileVersionTag() + "intermediary-" + intermediaryHash + ".jar");
            if (!Files.isRegularFile(result)) {
                try (AtomicFile atomicFile = new AtomicFile(result)) {
                    remapJar(intermediary.get(), null, Namespaces.OBF, Namespaces.INTERMEDIARY, mergedJar, atomicFile.tempPath, mcClasspathPaths.get());
                    atomicFile.commit();
                }
            }
            return new RemappedJar(result, intermediaryHash);
    }

    public final Lazy<RemappedJar> namedJar = new Lazy<>(this::createNamedJar);
    public RemappedJar createNamedJar() {
        Path intermediaryJar2 = intermediaryjar.get().jar;
        MessageDigest md = MessageDigestUtil.messageDigest(MessageDigestUtil.SHA256);
        MappingHasher.hash(md, intermediary.get(), mappings.get());
        if (getAw() != null) AccessWidenerHasher.hash(md, getAw());
        String mappingHash = MessageDigestUtil.toHexHash(md.digest());
        Path result = fabricCache().resolve("named").resolve(getMcVersion() + TinyRemapperHelper.getFileVersionTag() + "named-" + mappingHash + ".jar");
        if (!Files.isRegularFile(result)) {
            try (AtomicFile atomicFile = new AtomicFile(result)) {
                remapJar(mappings.get(), getAw(), Namespaces.INTERMEDIARY, Namespaces.NAMED, intermediaryJar2, atomicFile.tempPath, mcClasspathPaths.get());
                atomicFile.commit();
            }
        }
        return new RemappedJar(result, mappingHash);
    }

    public final Lazy<JavaJarDependency> decompiledJar = new Lazy<>(this::createDecompiledJar);
    public JavaJarDependency createDecompiledJar() {
        RemappedJar named = namedJar.get();
        BrachyuraDecompiler decompiler = decompiler();
        if (decompiler == null) return new JavaJarDependency(named.jar, null, null);
        // Different Java Versions have different classes
        // This will lead to missing classes if ran on an older jdk and MC uses newer jdk
        // Adding the JVM version to the directory avoids this issue if you rerun with a newer jdk
        Path resultDir = fabricCache().resolve("decompiled").resolve(decompiler.getName() + "-" + decompiler.getVersion()).resolve(getMcVersion() + TinyRemapperHelper.getFileVersionTag() + "named-" + named.mappingHash + "-J" + JvmUtil.CURRENT_JAVA_VERSION);
        return decompiler.getDecompiled(named.jar, decompClasspath(), resultDir, mappings.get(), Namespaces.NAMED).toJavaJarDep(null);
    }

    public void remapJar(MappingTree mappings, @Nullable Consumer<AccessWidenerVisitor> aw, String src, String dst, Path inputJar, Path outputJar, List<Path> classpath) {
        TinyRemapper.Builder remapperBuilder = TinyRemapper.newRemapper()
            .withMappings(new MappingTreeMappingProvider(mappings, src, dst))
            .withMappings(Jsr2JetbrainsMappingProvider.INSTANCE)
            .renameInvalidLocals(true)
            .rebuildSourceFilenames(true);
        if (aw != null) {
            AccessWidener accessWidener = new AccessWidener();
            aw.accept(accessWidener);
            remapperBuilder.extraPostApplyVisitor((cls, next) -> AccessWidenerClassVisitor.createClassVisitor(Opcodes.ASM9, next, accessWidener));
        }
        try {
            Files.deleteIfExists(outputJar);
        } catch (IOException e) {
            throw Util.sneak(e);
        }
        try (
            ZipProcessingSource source = new ZipProcessingSource(inputJar);
            ZipProcessingSink sink = new ZipProcessingSink(outputJar)
        ) {
            new ProcessorChain(new RemapperProcessor(remapperBuilder, classpath)).apply(sink, source);
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
        return new CfrDecompiler();
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
