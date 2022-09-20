package io.github.coolcrabs.brachyura.decompiler.cfr;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.benf.cfr.reader.api.CfrDriver;
import org.benf.cfr.reader.util.CfrVersionInfo;
import org.tinylog.Logger;

import io.github.coolcrabs.brachyura.decompiler.BrachyuraDecompiler;
import io.github.coolcrabs.brachyura.decompiler.DecompileLineNumberTable;
import io.github.coolcrabs.brachyura.decompiler.LineNumberTableReplacer;
import io.github.coolcrabs.brachyura.util.Util;
import net.fabricmc.mappingio.tree.MappingTree;
import net.fabricmc.mappingio.tree.MemoryMappingTree;

public class CfrDecompiler extends BrachyuraDecompiler {
    private static final Map<String, String> CFR_OPTIONS = new HashMap<>();
    private static final String VERSION;

    static {
        CFR_OPTIONS.put("trackbytecodeloc", "true");
        try {
            VERSION = (String) CfrVersionInfo.class.getField("VERSION").get(null); // Java moment
        } catch (Exception t) {
            throw Util.sneak(t);
        }
    }

    private final int threadCount;
    private final boolean replaceLineNumbers;
    
    public CfrDecompiler() {
        this(false);
    }
    
    public CfrDecompiler(int threadCount) {
        this(threadCount, false);
    }
    
    public CfrDecompiler(boolean replaceLineNumbers) {
        this(Runtime.getRuntime().availableProcessors(), replaceLineNumbers);
    }

    /**
     * Creates a new CFR instance
     * @param threadCount should be the same as system CPU thread count for fastest decomp
     * @param replaceLineNumbers should be false for MC because of Mixin bugs :(
     */
    public CfrDecompiler(int threadCount, boolean replaceLineNumbers) {
        this.threadCount = threadCount;
        this.replaceLineNumbers = replaceLineNumbers;
    }

    @Override
    protected void decompileAndLinemap(Path jar, List<Path> classpath, Path resultDir, MappingTree tree, int namespace) {
        DecompileResult r = getDecompileResult(jar, resultDir);
        try {
            ArrayList<String> classes = new ArrayList<>();
            DecompileLineNumberTable lineNumbers = new DecompileLineNumberTable();
            try (
                BrachyuraCfrClassFileSource cfrClassFileSource = new BrachyuraCfrClassFileSource(jar, classpath, classes);
                BrachyuraCfrOutputSinkFactory cfrOutputSinkFactory = new BrachyuraCfrOutputSinkFactory(r.sourcesJar, lineNumbers, replaceLineNumbers);
            ) {
                CfrDriver.Builder cfrDriver = new CfrDriver.Builder();
                cfrDriver.withOptions(CFR_OPTIONS);
                cfrDriver.withClassFileSource(cfrClassFileSource);
                cfrDriver.withOutputSink(cfrOutputSinkFactory);
                if (tree != null) {
                    if (namespace >= 0 && tree instanceof MemoryMappingTree) {
                        ((MemoryMappingTree)tree).setIndexByDstNames(true);
                    }
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
                            Logger.error("Exception Decompiling " + className);
                            Logger.error(e);
                        }
                    });
                }
                executor.shutdown();
                executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
                Logger.info("(CFR) Linemapping {}", jar.getFileName());
                LineNumberTableReplacer.replaceLineNumbers(jar, r.jar, lineNumbers);
            }
        } catch (Exception e) {
            throw Util.sneak(e);
        }
    }

    @Override
    public String getName() {
        return "BrachyuraCFR" + (replaceLineNumbers ? "-replace" : "-remap");
    }

    @Override
    public String getVersion() {
        return VERSION;
    }

    @Override
    public int getThreadCount() {
        return threadCount;
    }
}
