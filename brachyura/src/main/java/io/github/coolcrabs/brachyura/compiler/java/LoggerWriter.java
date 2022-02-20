package io.github.coolcrabs.brachyura.compiler.java;

import java.io.Writer;

import org.tinylog.Logger;

class LoggerWriter extends Writer {
    StringBuilder data = new StringBuilder();

    @Override
    public void write(int c0) {
        synchronized (lock) {
            char c = (char) c0;
            if (c == '\n') {
                Logger.info(data.toString());
                data.setLength(0);
            } else {
                data.append(c);
            }
        }
    }

    @Override
    public void write(char[] cbuf, int off, int len) {
        synchronized (lock) {
            for (int i = off; i - off < len; i++) {
                char c = cbuf[i];
                if (c == '\n') {
                    Logger.info(data.toString());
                    data.setLength(0);
                } else {
                    data.append(c);
                }
            }
        }
    }

    @Override
    public void flush() {
        // stub
    }

    @Override
    public void close() {
        if (data.length() > 0) {
            Logger.info(data.toString());
        }
    }
    
}
