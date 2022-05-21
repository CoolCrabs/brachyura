package io.github.coolcrabs.brachyura.mappings.tinyremapper;

import net.fabricmc.tinyremapper.TinyRemapper;

public class TrWrapper implements AutoCloseable {
    public final TinyRemapper tr;
    
    public TrWrapper(TinyRemapper.Builder b) {
        tr = b.build();
    }

    @Override
    public void close() {
        tr.finish();
    }
}
