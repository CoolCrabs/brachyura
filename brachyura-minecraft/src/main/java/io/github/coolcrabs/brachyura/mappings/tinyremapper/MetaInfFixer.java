package io.github.coolcrabs.brachyura.mappings.tinyremapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Collection;
import java.util.Iterator;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import io.github.coolcrabs.brachyura.processing.ProcessingEntry;
import io.github.coolcrabs.brachyura.processing.ProcessingSink;
import io.github.coolcrabs.brachyura.processing.Processor;
import io.github.coolcrabs.brachyura.util.ByteArrayOutputStreamEx;
import net.fabricmc.tinyremapper.TinyRemapper;

// https://github.com/FabricMC/tiny-remapper/blob/master/src/main/java/net/fabricmc/tinyremapper/MetaInfFixer.java
// Rewritten since tr's is heavily nio tied atm
public class MetaInfFixer implements Processor {
    final TinyRemapper remapper;

    public MetaInfFixer(TrWrapper remapper) {
        this.remapper = remapper.tr;
    }

    @Override
    public void process(Collection<ProcessingEntry> inputs, ProcessingSink sink) throws IOException {
        for (ProcessingEntry e : inputs) {
            if (e.id.path.startsWith("META-INF/")) {
                int si = e.id.path.lastIndexOf('/');
                String fn = si == -1 ? e.id.path : e.id.path.substring(si + 1);
                if (e.id.path.equals("META-INF/MANIFEST.MF")) {
                    Manifest m;
                    try (InputStream i = e.in.get()) {
                        m = new Manifest(i);
                    }
                    fixManifest(m, remapper);
                    ByteArrayOutputStreamEx ex = new ByteArrayOutputStreamEx();
                    m.write(ex);
                    sink.sink(ex::toIs, e.id);
                } else if (e.id.path.startsWith("META-INF/services/")) {
                    ByteArrayOutputStreamEx ex = new ByteArrayOutputStreamEx();
                    try (
                        BufferedReader r = new BufferedReader(new InputStreamReader(e.in.get()));
                        Writer w = new OutputStreamWriter(ex);
                    ) {
                        fixServiceDecl(r, w, remapper);
                    }
                    sink.sink(ex::toIs, e.id);
                } else if (fn.endsWith(".SF") || fn.endsWith(".DSA") || fn.endsWith(".RSA") || fn.startsWith("SIG-")) {
                    // Strip (noop)
                } else {
                    sink.sink(e.in, e.id);
                }
            } else {
                sink.sink(e.in, e.id);
            }
        }
    }

    private static String mapFullyQualifiedClassName(String name, TinyRemapper tr) {
        return tr.getEnvironment().getRemapper().map(name.replace('.', '/')).replace('/', '.');
    }

    private static void fixManifest(Manifest manifest, TinyRemapper remapper) {
        Attributes mainAttrs = manifest.getMainAttributes();
        if (remapper != null) {
            String val = mainAttrs.getValue(Attributes.Name.MAIN_CLASS);
            if (val != null)
                mainAttrs.put(Attributes.Name.MAIN_CLASS, mapFullyQualifiedClassName(val, remapper));
            val = mainAttrs.getValue("Launcher-Agent-Class");
            if (val != null)
                mainAttrs.putValue("Launcher-Agent-Class", mapFullyQualifiedClassName(val, remapper));
        }
        mainAttrs.remove(Attributes.Name.SIGNATURE_VERSION);
        for (Iterator<Attributes> it = manifest.getEntries().values().iterator(); it.hasNext();) {
            Attributes attrs = it.next();
            for (Iterator<Object> it2 = attrs.keySet().iterator(); it2.hasNext();) {
                Attributes.Name attrName = (Attributes.Name) it2.next();
                String name = attrName.toString();
                if (name.endsWith("-Digest") || name.contains("-Digest-") || name.equals("Magic")) {
                    it2.remove();
                }
            }
            if (attrs.isEmpty()) it.remove();
        }
    }

    private static void fixServiceDecl(BufferedReader reader, Writer writer, TinyRemapper remapper) throws IOException {
        String line;
        while ((line = reader.readLine()) != null) {
            int end = line.indexOf('#');
            if (end < 0) end = line.length();
            // trim start+end to skip ' ' and '\t'
            int start = 0;
            char c;
            while (start < end && ((c = line.charAt(start)) == ' ' || c == '\t')) {
                start++;
            }
            while (end > start && ((c = line.charAt(end - 1)) == ' ' || c == '\t')) {
                end--;
            }
            if (start == end) {
                writer.write(line);
            } else {
                writer.write(line, 0, start);
                writer.write(mapFullyQualifiedClassName(line.substring(start, end), remapper));
                writer.write(line, end, line.length() - end);
            }
            writer.write('\n');
        }
    }
}
