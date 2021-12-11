package io.github.coolmineman.trieharder;

import java.io.IOException;
import java.io.Reader;

public class ReaderCharIn implements ReplacerCharIn {
    Reader reader;

    public ReaderCharIn(Reader reader) {
        this.reader = reader;
    }

    @Override
    public int read() {
        try {
            return reader.read();
        } catch (IOException e) {
            throw Util.sneak(e);
        }
    }
}
