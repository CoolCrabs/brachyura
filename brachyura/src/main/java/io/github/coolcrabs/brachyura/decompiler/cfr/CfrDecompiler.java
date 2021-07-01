package io.github.coolcrabs.brachyura.decompiler.cfr;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.benf.cfr.reader.api.CfrDriver;
import org.benf.cfr.reader.util.CfrVersionInfo;
import org.jetbrains.annotations.Nullable;

import io.github.coolcrabs.brachyura.decompiler.BrachyuraDecompiler;
import io.github.coolcrabs.brachyura.util.Util;
import net.fabricmc.mappingio.tree.MappingTree;

public enum CfrDecompiler implements BrachyuraDecompiler {
    INSTANCE;

    public void decompile(Path jar, List<Path> classpath, @Nullable Path outputJar, @Nullable Path outputLineNumberMappings, @Nullable MappingTree tree, int namespace) {
        try {
            ArrayList<String> classes = new ArrayList<>();
            try (
                BrachyuraCfrClassFileSource cfrClassFileSource = new BrachyuraCfrClassFileSource(jar, classpath, classes);
                BrachyuraCfrOutputSinkFactory cfrOutputSinkFactory = new BrachyuraCfrOutputSinkFactory(outputJar, outputLineNumberMappings);
            ) {
                CfrDriver.Builder cfrDriver = new CfrDriver.Builder();
                if (outputLineNumberMappings != null) {
                    cfrDriver.withOptions(Collections.singletonMap("trackbytecodeloc", "true"));
                }
                cfrDriver.withOverrideClassFileSource(cfrClassFileSource);
                cfrDriver.withOutputSink(cfrOutputSinkFactory);
                if (tree != null) {
                    cfrDriver.withJavadocProvider(new MappingTreeJavadocProvider(tree, namespace));
                }
                cfrDriver.build().analyse(classes);
            }
        } catch (Exception e) {
            throw Util.sneak(e);
        }
    }

    @Override
    public String getName() {
        return "BrachyuraCFR";
    }

    @Override
    public String getVersion() {
        return CfrVersionInfo.VERSION;
    }

    @Override
    public LineNumberMappingsSupport lineNumberMappingsSupport() {
        return LineNumberMappingsSupport.REPLACE;
    }
}
