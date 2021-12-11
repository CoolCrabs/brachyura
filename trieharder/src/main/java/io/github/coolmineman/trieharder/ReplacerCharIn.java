package io.github.coolmineman.trieharder;

import java.io.IOException;

/**
 * Reader but simpler
 */
public interface ReplacerCharIn {
    int read() throws IOException; // -1 eof, -2 break search
}
