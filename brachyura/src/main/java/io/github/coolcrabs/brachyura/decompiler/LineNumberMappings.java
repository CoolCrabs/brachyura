package io.github.coolcrabs.brachyura.decompiler;

import java.io.IOException;
import java.io.Writer;

public interface LineNumberMappings {
    void write(Writer writer) throws IOException;
}
