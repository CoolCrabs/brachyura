package io.github.coolcrabs.brachyura.util;

import java.io.ByteArrayOutputStream;

/**
 * Allows to avoid copying
 */
public class ByteArrayOutputStreamEx extends ByteArrayOutputStream {
    public byte[] buf() {
        return this.buf;
    }
}
