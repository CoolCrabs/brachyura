package io.github.coolcrabs.brachyura.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

/**
 * Allows to avoid copying
 */
public class ByteArrayOutputStreamEx extends ByteArrayOutputStream {
    public byte[] buf() {
        return this.buf;
    }

    public InputStream toIs() {
        return new ByteArrayInputStream(buf, 0, size());
    }
}
