package io.github.coolcrabs.brachyura.memurl;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.function.Supplier;

class MemoryUrlConnection extends URLConnection {
    Supplier<InputStream> in = null;

    protected MemoryUrlConnection(URL url) {
        super(url);
    }

    @Override
    public void connect() throws IOException {
        if (!connected) {
            this.in = MemoryUrlProvider.instances.get(url.getHost()).func.apply(url.getPath().charAt(0) == '/' ? url.getPath().substring(1) : url.getPath());
            this.connected = true;
        }
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return in.get();
    }

}
