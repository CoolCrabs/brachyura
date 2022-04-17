package io.github.coolcrabs.brachyura.quilt;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import io.github.coolcrabs.brachyura.fabric.FabricModule;
import io.github.coolcrabs.brachyura.util.OsUtil;
import io.github.coolcrabs.brachyura.util.OsUtil.Os;
import io.github.coolcrabs.brachyura.util.Util;

public abstract class QuiltModule extends FabricModule {

    protected QuiltModule(QuiltContext context) {
        super(context);
    }
    
    public List<String> ideVmArgs(boolean client) {
        try {
            ArrayList<String> r = new ArrayList<>();
            r.add("-Dloader.development=true");
            r.add("-Dloader.remapClasspathFile=" + context.runtimeRemapClasspath.get());
            r.add("-Dlog4j.configurationFile=" + writeLog4jXml());
            r.add("-Dlog4j2.formatMsgNoLookups=true");
            r.add("-Dloader.log.disableAnsi=false");
            if (client) {
                String natives = context.extractedNatives.get().stream().map(Path::toString).collect(Collectors.joining(File.pathSeparator));
                r.add("-Djava.library.path=" + natives);
                r.add("-Dtorg.lwjgl.librarypath=" + natives);
                if (OsUtil.OS == Os.OSX) {
                    r.add("-XstartOnFirstThread");
                }
            }
            return r;
        } catch (Exception e) {
            throw Util.sneak(e);
        }
    }
}
