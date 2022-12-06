package io.github.coolcrabs.brachyura.quilt;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipFile;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import io.github.coolcrabs.brachyura.dependency.JavaJarDependency;
import io.github.coolcrabs.brachyura.exception.UnknownJsonException;
import io.github.coolcrabs.brachyura.fabric.AccessWidenerRemapper;
import io.github.coolcrabs.brachyura.fabric.AccessWidenerRemapper.AccessWidenerCollector;
import io.github.coolcrabs.brachyura.fabric.FabricContext;
import io.github.coolcrabs.brachyura.mappings.Namespaces;
import io.github.coolcrabs.brachyura.mappings.tinyremapper.MetaInfFixer;
import io.github.coolcrabs.brachyura.mappings.tinyremapper.RemapperProcessor;
import io.github.coolcrabs.brachyura.maven.MavenId;
import io.github.coolcrabs.brachyura.processing.ProcessingEntry;
import io.github.coolcrabs.brachyura.processing.ProcessingId;
import io.github.coolcrabs.brachyura.processing.ProcessingSink;
import io.github.coolcrabs.brachyura.processing.ProcessingSource;
import io.github.coolcrabs.brachyura.processing.Processor;
import io.github.coolcrabs.brachyura.processing.ProcessorChain;
import io.github.coolcrabs.brachyura.processing.sinks.AtomicZipProcessingSink;
import io.github.coolcrabs.brachyura.processing.sources.ZipProcessingSource;
import io.github.coolcrabs.brachyura.util.GsonUtil;
import io.github.coolcrabs.brachyura.util.PathUtil;
import io.github.coolcrabs.brachyura.util.Util;
import net.fabricmc.mappingio.tree.MappingTree;

public abstract class QuiltContext extends FabricContext {
    @Override
    public ProcessorChain resourcesProcessingChain(List<JavaJarDependency> jij) {
        Path fmjgen = getLocalBrachyuraPath().resolve("fmjgen");
        if (Files.exists(fmjgen)) PathUtil.deleteDirectory(fmjgen);
        List<Path> jij2 = new ArrayList<>();
        for (JavaJarDependency mod : jij) {
                try {
                    try (ZipFile f = new ZipFile(mod.jar.toFile())) {
                        if (f.getEntry("fabric.mod.json") == null && f.getEntry("quilt.mod.json") == null) {
                            Path p = fmjgen.resolve(mod.jar.getFileName());
                            try (
                                ZipProcessingSource s = new ZipProcessingSource(mod.jar);
                                AtomicZipProcessingSink sink = new AtomicZipProcessingSink(p)
                            ) {
                                new ProcessorChain(new FmjGenerator(Collections.singletonMap(s, mod.mavenId))).apply(sink, s);
                                sink.commit();
                            }
                            jij2.add(p);
                        } else {
                            jij2.add(mod.jar);
                        }
                    }
                } catch (Exception e) {
                    throw Util.sneak(e);
                }
        }
        return new ProcessorChain(QmjRefmapApplier.INSTANCE, new QmjJijApplier(jij2), new AccessWidenerRemapper(mappings.get(), mappings.get().getNamespaceId(Namespaces.INTERMEDIARY), QuiltAwCollector.INSTANCE));
    }

    @Override
    public Path remappedModsRootPath() {
        return getLocalBrachyuraPath().resolve("quiltdeps");
    }

    @Override
    public byte remappedModsLogicVersion() {
        return 2;
    }

    @Override
    public ProcessorChain modRemapChainOverrideOnlyIfYouOverrideRemappedModsRootPathAndLogicVersion(MappingTree tree, String src, String dst, List<Path> cp, Map<ProcessingSource, MavenId> c) {
        RemapperProcessor rp = new RemapperProcessor(cp, tree, tree.getNamespaceId(src), tree.getNamespaceId(dst));
        return new ProcessorChain(
            rp,
            new MetaInfFixer(rp),
            JijRemover.INSTANCE,
            new AccessWidenerRemapper(mappings.get(), mappings.get().getNamespaceId(Namespaces.NAMED), QuiltAwCollector.INSTANCE),
            new FmjGenerator(c)
        );
    }

    public enum JijRemover implements Processor {
        INSTANCE;

        @Override
        public void process(Collection<ProcessingEntry> inputs, ProcessingSink sink) throws IOException {
            for (ProcessingEntry e : inputs) {
                boolean fmj = e.id.path.equals("fabric.mod.json");
                boolean qmj = e.id.path.equals("quilt.mod.json");
                if (fmj || qmj) {
                    Gson gson = new GsonBuilder().setPrettyPrinting().setLenient().create();
                    JsonObject modJson;
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(e.in.get(), StandardCharsets.UTF_8))) {
                        modJson = gson.fromJson(reader, JsonObject.class);
                    }
                    if (fmj) {
                        modJson.remove("jars");
                    }
                    if (qmj) {
                        modJson.getAsJsonObject("quilt_loader").remove("jars");
                    }
                    sink.sink(() -> GsonUtil.toIs(modJson, gson), e.id);
                } else {
                    sink.sink(e.in, e.id);
                }
            }
        }
    }

    public enum QuiltAwCollector implements AccessWidenerCollector {
        INSTANCE;

        @Override
        public List<ProcessingId> collect(Collection<ProcessingEntry> inputs) throws IOException {
            ArrayList<ProcessingId> result = new ArrayList<>();
            // Prefer a mod's qmj otherwise use fmj
            HashMap<ProcessingSource, ProcessingEntry> mjs = new HashMap<>();
            for (ProcessingEntry e : inputs) {
                if (e.id.path.equals("quilt.mod.json") || (e.id.path.equals("fabric.mod.json") && mjs.get(e.id.source) == null)) {
                    mjs.put(e.id.source, e);
                }
            }
            for (ProcessingEntry e : mjs.values()) {
                boolean fmj = e.id.path.equals("fabric.mod.json");
                boolean qmj = e.id.path.equals("quilt.mod.json");
                if (fmj || qmj) {
                    JsonObject modJson;
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(e.in.get(), StandardCharsets.UTF_8))) {
                        modJson = new Gson().fromJson(reader, JsonObject.class);
                    }
                    JsonElement aw0 = null;
                    if (fmj) aw0 = modJson.get("accessWidener");
                    if (qmj) aw0 = modJson.get("access_widener");
                    if (aw0 != null) {
                        result.add(new ProcessingId(aw0.getAsString(), e.id.source));
                    }
                }
            }
            return result;
        }
    }

    public enum QmjRefmapApplier implements Processor {
        INSTANCE;

        @Override
        public void process(Collection<ProcessingEntry> inputs, ProcessingSink sink) throws IOException {
            HashMap<String, ProcessingEntry> entries = new HashMap<>();
            for (ProcessingEntry e : inputs) {
                entries.put(e.id.path, e);
            }
            ProcessingEntry qmj = entries.get("quilt.mod.json");
            if (qmj != null) {
                Gson gson = new GsonBuilder().setPrettyPrinting().setLenient().create();
                List<String> mixinjs = new ArrayList<>();
                JsonObject quiltModJson;
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(qmj.in.get(), StandardCharsets.UTF_8))) {
                    quiltModJson = gson.fromJson(reader, JsonObject.class);
                }
                JsonElement m = quiltModJson.get("mixin"); // Not "mixins"
                if (m instanceof JsonArray) {
                    JsonArray mixins = m.getAsJsonArray();
                    for (JsonElement a : mixins) {
                        if (a.isJsonPrimitive()) {
                            mixinjs.add(a.getAsString());
                        } else {
                            throw new UnknownJsonException(a.toString());
                        }
                    }
                } else if (m instanceof JsonPrimitive) {
                    mixinjs.add(m.getAsString());
                } else if (m != null) {
                    throw new UnknownJsonException(m.toString());
                }
                for (String mixin : mixinjs) {
                    ProcessingEntry entry = entries.get(mixin);
                    entries.remove(mixin);
                    JsonObject mixinjson;
                    try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(entry.in.get(), StandardCharsets.UTF_8))) {
                        mixinjson = gson.fromJson(bufferedReader, JsonObject.class);
                    }
                    if (mixinjson.get("refmap") == null) {
                        mixinjson.addProperty("refmap", quiltModJson.getAsJsonObject("quilt_loader").get("id").getAsString() + "-refmap.json");
                    }
                    sink.sink(() -> GsonUtil.toIs(mixinjson, gson), entry.id);
                }
            }
            entries.forEach((k, v) -> sink.sink(v.in, v.id));
        }
    }

    public static class QmjJijApplier implements Processor {
        final List<Path> jij;

        public QmjJijApplier(List<Path> jij) {
            this.jij = jij;
        }

        @Override
        public void process(Collection<ProcessingEntry> inputs, ProcessingSink sink) throws IOException {
            for (ProcessingEntry e : inputs) {
                if (!jij.isEmpty() && "quilt.mod.json".equals(e.id.path)) {
                    Gson gson = new GsonBuilder().setPrettyPrinting().setLenient().create();
                    JsonObject quiltModJson;
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(e.in.get(), StandardCharsets.UTF_8))) {
                        quiltModJson = gson.fromJson(reader, JsonObject.class);
                    }
                    JsonArray jars = new JsonArray();
                    quiltModJson.getAsJsonObject("quilt_loader").add("jars", jars);
                    List<String> used = new ArrayList<>();
                    for (Path jar : jij) {
                        String path = "META-INF/jars/" + jar.getFileName();
                        int a = 0;
                        while (used.contains(path)) {
                            path = "META-INF/jars/" + a + jar.getFileName();
                            a++;
                        }
                        jars.add(path);
                        used.add(path);
                        sink.sink(() -> PathUtil.inputStream(jar), new ProcessingId(path, e.id.source));
                    }
                    sink.sink(() -> GsonUtil.toIs(quiltModJson, gson), e.id);
                } else {
                    sink.sink(e.in, e.id);
                }
            }
        }
    }
}
