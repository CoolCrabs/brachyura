package io.github.coolcrabs.brachyura.decompiler.cfr;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.benf.cfr.reader.api.CfrDriver;
import org.benf.cfr.reader.util.CfrVersionInfo;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;

import io.github.coolcrabs.brachyura.decompiler.BrachyuraDecompiler;
import io.github.coolcrabs.brachyura.util.Util;
import net.fabricmc.mappingio.tree.MappingTree;

public class CfrDecompiler implements BrachyuraDecompiler {
    private final int threadCount;

    public CfrDecompiler(int threadCount) {
        this.threadCount = threadCount;
    }

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
                CfrDriver cfrDriver2 = cfrDriver.build();
                // Split decompilation into multiple threads
                ExecutorService executor = Executors.newFixedThreadPool(threadCount);
                for (String className : classes) {
                    executor.execute(() -> {
                        try {
                            cfrDriver2.analyse(Collections.singletonList(className));
                        } catch (Exception e) {
                            Logger.error("Exception Decompiling" + className);
                            Logger.error(e);
                        }
                    });
                }
                executor.shutdown();
                executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
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
    public int getThreadCount() {
        return threadCount;
    }

    @Override
    public LineNumberMappingsSupport lineNumberMappingsSupport() {
        return LineNumberMappingsSupport.REPLACE;
    }
}
