package io.github.coolcrabs.fernutil;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

import org.jetbrains.java.decompiler.main.Fernflower;
import org.jetbrains.java.decompiler.main.extern.IFernflowerPreferences;

import io.github.coolcrabs.fernutil.FernUtil.JavadocProvider;
import io.github.coolcrabs.fernutil.FernUtil.LineNumbers;
import net.fabricmc.fernflower.api.IFabricJavadocProvider;

class TJump {
    TJump() { }

    public static class PackageHack {
        PackageHack() { }

        public static void decompile(Path inJar, Path outSources, List<Path> cp, Consumer<LineNumbers> lines, JavadocProvider provider) throws IOException {
            boolean fabric;
            try {
                Class.forName("net.fabricmc.fernflower.api.IFabricResultSaver", false, TJump.class.getClassLoader());
                fabric = true;    
            } catch (Exception e) {
                fabric = false;
            }
            ArrayList<Path> cp0 = new ArrayList<>(cp.size() + 1);
            cp0.add(inJar);
            cp0.addAll(cp);
            try (
                TBytecodeProvider bytecodeProvider = new TBytecodeProvider(cp0);
                TFFResultSaver resultSaver = fabric ? new TFFResultSaverFabric(outSources, lines) : new TFFResultSaver(outSources, lines);
            ) {
                HashMap<String, Object> options = new HashMap<>();
                options.put(IFernflowerPreferences.DECOMPILE_GENERIC_SIGNATURES, "1");
                options.put(IFernflowerPreferences.BYTECODE_SOURCE_MAPPING, "1");
                options.put(IFernflowerPreferences.REMOVE_SYNTHETIC, "1");
                options.put(IFernflowerPreferences.INDENT_STRING, "    ");
                options.put(IFernflowerPreferences.NEW_LINE_SEPARATOR, "\n");
                // Threads configured by default in all ff forks
                if (fabric) options.put(IFabricJavadocProvider.PROPERTY_NAME, new TJavadocProviderFabric(provider));
                Fernflower ff = new Fernflower(bytecodeProvider, resultSaver, options, new TFFLogger());
                ff.addSource(inJar.toFile());
                for (Path p : cp) {
                    ff.addLibrary(p.toFile());
                }
                ff.decompileContext();
            }
        }
    }
}
