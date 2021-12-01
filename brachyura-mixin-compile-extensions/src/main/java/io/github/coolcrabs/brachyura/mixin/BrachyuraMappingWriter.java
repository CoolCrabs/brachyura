package io.github.coolcrabs.brachyura.mixin;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.processing.Filer;
import javax.tools.StandardLocation;

import org.spongepowered.asm.obfuscation.mapping.common.MappingField;
import org.spongepowered.asm.obfuscation.mapping.common.MappingMethod;
import org.spongepowered.tools.obfuscation.ObfuscationType;
import org.spongepowered.tools.obfuscation.mapping.IMappingConsumer.MappingSet;
import org.spongepowered.tools.obfuscation.mapping.IMappingWriter;

class BrachyuraMappingWriter implements IMappingWriter {
    final String inNamespace;
    final String outNamespace;
    final Filer filer;

    public BrachyuraMappingWriter(String inNamespace, String outNamespace, Filer filer) {
        this.inNamespace = inNamespace;
        this.outNamespace = outNamespace;
        this.filer = filer;
    }

    @Override
    public void write(String output, ObfuscationType type, MappingSet<MappingField> fields, MappingSet<MappingMethod> methods) {
        class MethodsAndFields {
            ArrayList<MappingSet.Pair<MappingField>> fields = new ArrayList<>();
            ArrayList<MappingSet.Pair<MappingMethod>> methods = new ArrayList<>();
        }
        if (!fields.isEmpty()|| !methods.isEmpty()) {
            HashMap<String, MethodsAndFields> classMap = new HashMap<>();
            for (MappingSet.Pair<MappingField> p : fields) {
                classMap.computeIfAbsent(p.from.getOwner(), k -> new MethodsAndFields()).fields.add(p);
            }
            for (MappingSet.Pair<MappingMethod> p : methods) {
                classMap.computeIfAbsent(p.from.getOwner(), k -> new MethodsAndFields()).methods.add(p);
            }
            try {
                Writer writer;
                if (output.matches("^.*[\\\\/:].*$")) {
                    File outFile = new File(output);
                    outFile.getParentFile().mkdirs();
                    writer = Files.newBufferedWriter(outFile.toPath());
                } else {
                    writer = new OutputStreamWriter(filer.createResource(StandardLocation.CLASS_OUTPUT, "", output).openOutputStream(), StandardCharsets.UTF_8);
                }
                try {
                    writer.write("tiny\t2\t0\t"); writer.write(inNamespace); writer.write('\t'); writer.write(outNamespace); writer.write('\n');
                    for (Map.Entry<String, MethodsAndFields> e : classMap.entrySet()) {
                        writer.write("c\t"); writer.write(e.getKey()); writer.write("\t\n"); // Empty dst since we don't remap classes
                        for (MappingSet.Pair<MappingField> field : e.getValue().fields) {
                            writer.write("\tf\t"); writer.write(field.from.getDesc()); writer.write('\t'); writer.write(field.from.getSimpleName()); writer.write('\t'); writer.write(field.to.getSimpleName()); writer.write('\n');
                        }
                        for (MappingSet.Pair<MappingMethod> method : e.getValue().methods) {
                            writer.write("\tm\t"); writer.write(method.from.getDesc()); writer.write('\t'); writer.write(method.from.getSimpleName()); writer.write('\t'); writer.write(method.to.getSimpleName()); writer.write('\n');
                        }
                    }
                } finally {
                    writer.close();
                }
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }
    
}
