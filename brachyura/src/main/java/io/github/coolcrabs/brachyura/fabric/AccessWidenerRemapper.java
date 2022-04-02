package io.github.coolcrabs.brachyura.fabric;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import io.github.coolcrabs.brachyura.processing.ProcessingEntry;
import io.github.coolcrabs.brachyura.processing.ProcessingId;
import io.github.coolcrabs.brachyura.processing.ProcessingSink;
import io.github.coolcrabs.brachyura.processing.Processor;
import io.github.coolcrabs.brachyura.util.StreamUtil;
import net.fabricmc.accesswidener.AccessWidenerReader;
import net.fabricmc.accesswidener.AccessWidenerWriter;
import net.fabricmc.mappingio.tree.MappingTree;

public class AccessWidenerRemapper implements Processor {
    final MappingTree mappings;
    final int namespace;

    public AccessWidenerRemapper(MappingTree mappings, int namespace) {
        this.mappings = mappings;
        this.namespace = namespace;
    }

    @Override
    public void process(Collection<ProcessingEntry> inputs, ProcessingSink sink) throws IOException {
        HashMap<ProcessingId, ProcessingEntry> entries = new HashMap<>();
        ArrayList<ProcessingId> aws = new ArrayList<>();
        for (ProcessingEntry e : inputs) {
            entries.put(e.id, e);
            if (e.id.path.equals("fabric.mod.json")) {
                JsonObject fabricModJson;
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(e.in.get(), StandardCharsets.UTF_8))) {
                    fabricModJson = new Gson().fromJson(reader, JsonObject.class);
                }
                JsonElement aw0 = fabricModJson.get("accessWidener");
                if (aw0 != null) {
                    aws.add(new ProcessingId(aw0.getAsString(), e.id.source));
                }
            }
        }
        for (ProcessingId awid : aws) {
            ProcessingEntry aw = entries.remove(awid);
            byte[] awb;
            try (InputStream is = aw.in.get()) {
                awb = StreamUtil.readFullyAsBytes(is);
            }
            AccessWidenerWriter w = new AccessWidenerWriter(AccessWidenerReader.readVersion(awb));
            AccessWidenerNamespaceChanger nc = new AccessWidenerNamespaceChanger(w, mappings, namespace, aw.id.path);
            new AccessWidenerReader(nc).read(awb);
            sink.sink(() -> new ByteArrayInputStream(w.write()), aw.id);
        }
        for (ProcessingEntry e : entries.values()) {
            sink.sink(e.in, e.id);
        }
    }
}
