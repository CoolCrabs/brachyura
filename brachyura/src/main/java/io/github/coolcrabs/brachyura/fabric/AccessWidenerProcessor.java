package io.github.coolcrabs.brachyura.fabric;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.Collection;
import java.util.HashMap;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.tinylog.Logger;

import io.github.coolcrabs.accesswidener.AccessWidener;
import io.github.coolcrabs.accesswidener.AccessWidenerClassVisitor;
import io.github.coolcrabs.brachyura.processing.HashableProcessor;
import io.github.coolcrabs.brachyura.processing.ProcessingEntry;
import io.github.coolcrabs.brachyura.processing.ProcessingSink;

public class AccessWidenerProcessor implements HashableProcessor {
    final AccessWidener aw;

    public AccessWidenerProcessor(AccessWidener aw) {
        this.aw = aw;
    }

    @Override
    public void process(Collection<ProcessingEntry> inputs, ProcessingSink sink) throws IOException {
        HashMap<String, ProcessingEntry> entries = new HashMap<>();
        HashMap<String, ProcessingEntry> processedEntries = new HashMap<>();
        for (ProcessingEntry e : inputs) {
            if (entries.put(e.id.path, e) != null) {
                throw new IllegalArgumentException("Duplicate entry " + e.id.path);
            }
        }
        for (String cls : aw.clsMap.keySet()) {
            process(cls, entries, processedEntries, true);
            while (cls.contains("$")) {
                cls = cls.substring(0, cls.lastIndexOf("$"));
                process(cls, entries, processedEntries, false);
            }
        }
        for (ProcessingEntry e : entries.values()) {
            sink.sink(e.in, e.id);
        }
        for (ProcessingEntry e : processedEntries.values()) {
            sink.sink(e.in, e.id);
        }
    }

    void process(String cls, HashMap<String, ProcessingEntry> entries, HashMap<String, ProcessingEntry> processedEntries, boolean softhard) throws IOException {
        String file = cls + ".class";
        ProcessingEntry e = entries.remove(file);
        if (e == null) {
            if (softhard && !processedEntries.containsKey(file)) Logger.warn("Unable to access class to widen {}", cls);
            return;
        }
        ClassWriter w = new ClassWriter(0);
        try (InputStream is = e.in.get()) {
            new ClassReader(is).accept(new AccessWidenerClassVisitor(Opcodes.ASM9, w, aw), 0);
        }
        byte[] bytes = w.toByteArray();
        processedEntries.put(file, new ProcessingEntry(() -> new ByteArrayInputStream(bytes), e.id));
    }

    @Override
    public void hash(MessageDigest md) {
        md.update((byte) 1); // version
        AccessWidenerHasher.hash(md, aw::accept);
    }
    
}
