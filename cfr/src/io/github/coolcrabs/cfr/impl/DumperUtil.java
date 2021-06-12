package io.github.coolcrabs.cfr.impl;

import org.benf.cfr.reader.util.output.Dumper;

public class DumperUtil {
    private DumperUtil() { }

    public static void writeJavaDoc(Dumper dumper, String javadoc) {
        if (javadoc == null || javadoc.isEmpty()) return;
        dumper.print("/**").newln();
        for (String line : javadoc.split("\n")) {
            dumper.print(" * ").print(line).newln();
        }
        dumper.print(" */").newln();
    }
}
