package io.github.coolcrabs.brachyura.quilt;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.jetbrains.annotations.Nullable;

import io.github.coolcrabs.accesswidener.AccessWidener;
import io.github.coolcrabs.accesswidener.AccessWidenerReader;
import io.github.coolcrabs.brachyura.decompiler.BrachyuraDecompiler;
import io.github.coolcrabs.brachyura.exception.UnknownJsonException;
import io.github.coolcrabs.brachyura.fabric.FabricContext;
import io.github.coolcrabs.brachyura.fabric.FabricLoader;
import io.github.coolcrabs.brachyura.fabric.FabricModule;
import io.github.coolcrabs.brachyura.fabric.SimpleFabricProject;
import io.github.coolcrabs.brachyura.mappings.Namespaces;
import io.github.coolcrabs.brachyura.minecraft.VersionMeta;
import io.github.coolcrabs.brachyura.util.Lazy;
import io.github.coolcrabs.brachyura.util.PathUtil;
import io.github.coolcrabs.brachyura.util.Util;
import net.fabricmc.mappingio.tree.MappingTree;

public abstract class SimpleQuiltProject extends SimpleFabricProject {
    @Override
    protected FabricContext createContext() {
        return new SimpleQuiltContext();
    }

    @Override
    protected FabricModule createModule() {
        return new SimpleQuiltModule((QuiltContext) context.get());
    }

    @Override
    public String getModId() {
        return qmjParseThingy.get()[0];
    }

    @Override
    public String getVersion() {
        return qmjParseThingy.get()[1];
    }

    @Override
    public String getMavenGroup() {
        return qmjParseThingy.get()[2];
    }

    @Override
    public @Nullable AccessWidener createAw() {
        String aw = qmjParseThingy.get()[3];
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

    private Lazy<String[]> qmjParseThingy = new Lazy<>(() -> {
        try {
            Gson gson = new GsonBuilder().setPrettyPrinting().setLenient().create();
            JsonObject quiltModJson;
            Path qmj = null;
            for (Path resDir : getResourceDirs()) {
                Path p = resDir.resolve("quilt.mod.json");
                if (Files.exists(p)) {
                    qmj = p;
                    break;
                }
            }
            try (BufferedReader reader = PathUtil.newBufferedReader(qmj)) {
                quiltModJson = gson.fromJson(reader, JsonObject.class);
            }
            JsonObject qloader = quiltModJson.getAsJsonObject("quilt_loader");
            JsonElement aw = quiltModJson.get("access_widener");
            return new String[] {qloader.get("id").getAsString(), qloader.get("version").getAsString(), qloader.get("group").getAsString(), aw == null ? null : aw.getAsString()};
        } catch (Exception e) {
            throw Util.sneak(e);
        }
    });


    public class SimpleQuiltContext extends QuiltContext {
        @Override
        public VersionMeta createMcVersion() {
            return SimpleQuiltProject.this.createMcVersion();
        }

        @Override
        public MappingTree createMappings() {
            return SimpleQuiltProject.this.createMappings();
        }

        @Override
        public FabricLoader getLoader() {
            return SimpleQuiltProject.this.getLoader();
        }

        @Override
        public void getModDependencies(ModDependencyCollector d) {
            SimpleQuiltProject.this.getModDependencies(d);
        }

        @Override
        public @Nullable AccessWidener createAw() {
            return SimpleQuiltProject.this.createAw();
        }

        @Override
        public @Nullable BrachyuraDecompiler decompiler() {
            return SimpleQuiltProject.this.decompiler();
        }

        @Override
        public Path getContextRoot() {
            return getProjectDir();
        }
    }

    public class SimpleQuiltModule extends QuiltModule {
        public SimpleQuiltModule(QuiltContext context) {
            super(context);
        }

        @Override
        public int getJavaVersion() {
            return SimpleQuiltProject.this.getJavaVersion();
        }

        @Override
        public Path[] getSrcDirs() {
            return SimpleQuiltProject.this.getSrcDirs();
        }

        @Override
        public Path[] getResourceDirs() {
            return SimpleQuiltProject.this.getResourceDirs();
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
}
