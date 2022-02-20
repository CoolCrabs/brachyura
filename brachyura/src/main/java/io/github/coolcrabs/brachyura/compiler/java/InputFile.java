package io.github.coolcrabs.brachyura.compiler.java;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.function.Supplier;

import javax.tools.SimpleJavaFileObject;

import io.github.coolcrabs.brachyura.processing.ProcessingId;
import io.github.coolcrabs.brachyura.util.Util;

class InputFile extends SimpleJavaFileObject {
    static final Kind[] KINDS = Kind.values();

    Supplier<InputStream> in;
    String path;

    protected InputFile(Supplier<InputStream> in, ProcessingId id) {
        super(uri(id), getKind(id.path));
        this.in = in;
        this.path = id.path;
    }

    static URI uri(ProcessingId id) {
        try {
            return new URI("crabin", "authority", "/" + id.path, null);
        } catch (URISyntaxException e) {
            throw Util.sneak(e);
        }
    }

    static Kind getKind(String path) {
        for (Kind k : KINDS) {
            if (path.endsWith(k.extension)) return k;
        }
        return Kind.OTHER;
    }

    @Override
    public InputStream openInputStream() throws IOException {
        return in.get();
    }
    
}
