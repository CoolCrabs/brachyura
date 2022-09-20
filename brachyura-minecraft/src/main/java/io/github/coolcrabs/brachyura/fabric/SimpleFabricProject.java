package io.github.coolcrabs.brachyura.fabric;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.jetbrains.annotations.Nullable;

import io.github.coolcrabs.accesswidener.AccessWidener;
import io.github.coolcrabs.accesswidener.AccessWidenerReader;
import io.github.coolcrabs.brachyura.decompiler.BrachyuraDecompiler;
import io.github.coolcrabs.brachyura.decompiler.cfr.CfrDecompiler;
import io.github.coolcrabs.brachyura.dependency.JavaJarDependency;
import io.github.coolcrabs.brachyura.exception.UnknownJsonException;
import io.github.coolcrabs.brachyura.fabric.FabricContext.ModDependencyCollector;
import io.github.coolcrabs.brachyura.ide.IdeModule;
import io.github.coolcrabs.brachyura.mappings.Namespaces;
import io.github.coolcrabs.brachyura.maven.MavenId;
import io.github.coolcrabs.brachyura.minecraft.VersionMeta;
import io.github.coolcrabs.brachyura.processing.ProcessorChain;
import io.github.coolcrabs.brachyura.processing.sinks.AtomicZipProcessingSink;
import io.github.coolcrabs.brachyura.processing.sources.DirectoryProcessingSource;
import io.github.coolcrabs.brachyura.project.Task;
import io.github.coolcrabs.brachyura.project.java.BaseJavaProject;
import io.github.coolcrabs.brachyura.project.java.SimpleJavaProject;
import io.github.coolcrabs.brachyura.util.Lazy;
import io.github.coolcrabs.brachyura.util.PathUtil;
import io.github.coolcrabs.brachyura.util.Util;
import net.fabricmc.mappingio.tree.MappingTree;

public abstract class SimpleFabricProject extends BaseJavaProject {
    public final Lazy<FabricContext> context = new Lazy<>(this::createContext);
    protected FabricContext createContext() {
        return new SimpleFabricContext();
    }

    public final Lazy<FabricModule> module = new Lazy<>(this::createModule);
    protected FabricModule createModule() {
        return new SimpleFabricModule(context.get());
    }

    public abstract VersionMeta createMcVersion();
    public abstract MappingTree createMappings();
    public abstract FabricLoader getLoader();
    public abstract void getModDependencies(ModDependencyCollector d);

    public int getJavaVersion() {
        return 8;
    }

    public BrachyuraDecompiler decompiler() {
        return new CfrDecompiler();
    }

    public Path[] getSrcDirs() {
        return new Path[]{getProjectDir().resolve("src").resolve("main").resolve("java")};
    }

    public Path[] getResourceDirs() {
        return new Path[]{getProjectDir().resolve("src").resolve("main").resolve("resources")};
    }

    protected ArrayList<JavaJarDependency> jijList = new ArrayList<>();

    public @Nullable AccessWidener createAw() {
        String aw = fmjParseThingy.get()[2];
        if (aw == null) return null;
        for (Path r : getResourceDirs()) {
            Path awp = r.resolve(aw);
            if (Files.exists(awp)) {
                AccessWidener result = new AccessWidener(Namespaces.NAMED);
                try {
                    try (BufferedReader read = Files.newBufferedReader(awp)) {
                        new AccessWidenerReader(result).read(read);
                    }
                } catch (IOException e) {
                    throw Util.sneak(e);
                }
                return result;
            }
        }
        throw new UnknownJsonException("Unable to find aw named:" + aw);
    }

    public String getMavenGroup() {
        return null;
    }
    
    public MavenId getId() {
        return getMavenGroup() == null ? null : new MavenId(getMavenGroup(), getModId(), getVersion());
    }

    public String getModId() {
        return fmjParseThingy.get()[0];
    }

    public String getVersion() {
        return fmjParseThingy.get()[1];
    }

    public JavaJarDependency jij(JavaJarDependency mod) {
        jijList.add(mod);
        return mod;
    }

    public MappingTree createMojmap() {
        return context.get().createMojmap();
    }

    private Lazy<String[]> fmjParseThingy = new Lazy<>(() -> {
        try {
            Gson gson = new GsonBuilder().setPrettyPrinting().setLenient().create();
            JsonObject fabricModJson;
            Path fmj = null;
            for (Path resDir : getResourceDirs()) {
                Path p = resDir.resolve("fabric.mod.json");
                if (Files.exists(p)) {
                    fmj = p;
                    break;
                }
            }
            try (BufferedReader reader = PathUtil.newBufferedReader(fmj)) {
                fabricModJson = gson.fromJson(reader, JsonObject.class);
            }
            JsonElement aw = fabricModJson.get("accessWidener");
            return new String[] {fabricModJson.get("id").getAsString(), fabricModJson.get("version").getAsString(), aw == null ? null : aw.getAsString()};
        } catch (Exception e) {
            throw Util.sneak(e);
        }
    });

    public class SimpleFabricContext extends FabricContext {
        @Override
        public VersionMeta createMcVersion() {
            return SimpleFabricProject.this.createMcVersion();
        }

        @Override
        public MappingTree createMappings() {
            return SimpleFabricProject.this.createMappings();
        }

        @Override
        public FabricLoader getLoader() {
            return SimpleFabricProject.this.getLoader();
        }

        @Override
        public void getModDependencies(ModDependencyCollector d) {
            SimpleFabricProject.this.getModDependencies(d);
        }

        @Override
        protected @Nullable AccessWidener createAw() {
            return SimpleFabricProject.this.createAw();
        }

        @Override
        public @Nullable BrachyuraDecompiler decompiler() {
            return SimpleFabricProject.this.decompiler();
        }

        @Override
        public Path getContextRoot() {
            return getProjectDir();
        }
    }

    public class SimpleFabricModule extends FabricModule {
        public SimpleFabricModule(FabricContext context) {
            super(context);
        }

        @Override
        public int getJavaVersion() {
            return SimpleFabricProject.this.getJavaVersion();
        }

        @Override
        public Path[] getSrcDirs() {
            return SimpleFabricProject.this.getSrcDirs();
        }

        @Override
        public Path[] getResourceDirs() {
            return SimpleFabricProject.this.getResourceDirs();
        }

        @Override
        public String getModuleName() {
            return getModId();
        }

        @Override
        public Path getModuleRoot() {
            return getProjectDir();
        }
    }

    @Override
    public void getTasks(Consumer<Task> p) {
        super.getTasks(p);
        p.accept(Task.of("build", this::build));
        getPublishTasks(p);
    }
    
    public void getPublishTasks(Consumer<Task> p) {
        SimpleJavaProject.createPublishTasks(p, this::build);
    }

    @Override
    public IdeModule[] getIdeModules() {
        return new IdeModule[]{module.get().ideModule.get()};
    }

    public ProcessorChain resourcesProcessingChain() {
        return context.get().resourcesProcessingChain(jijList);
    }

    public JavaJarDependency build() {
        try {
            try (AtomicZipProcessingSink out = new AtomicZipProcessingSink(getBuildJarPath())) {
                context.get().modDependencies.get(); // Ugly hack
                resourcesProcessingChain().apply(out, Arrays.stream(getResourceDirs()).map(DirectoryProcessingSource::new).collect(Collectors.toList()));
                context.get().getRemappedClasses(module.get()).values().forEach(s -> s.getInputs(out));
                out.commit();
            }
            return new JavaJarDependency(getBuildJarPath(), null, getId());
        } catch (Exception e) {
            throw Util.sneak(e);
        }
    }

    public Path getBuildJarPath() {
        return getBuildLibsDir().resolve(getModId() + "-" + getVersion() + ".jar");
    }
}
