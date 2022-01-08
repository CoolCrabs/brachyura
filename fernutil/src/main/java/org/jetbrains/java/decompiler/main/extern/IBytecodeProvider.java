package org.jetbrains.java.decompiler.main.extern;

import java.io.IOException;

public interface IBytecodeProvider {
    /**
     * If internalPath is null read from a class file
     * If internalPath is a value it is in a jar
     */
    byte[] getBytecode(String externalPath, String internalPath) throws IOException;
}
