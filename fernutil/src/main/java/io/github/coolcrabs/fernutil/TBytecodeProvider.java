package io.github.coolcrabs.fernutil;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;

import org.jetbrains.java.decompiler.main.extern.IBytecodeProvider;

class TBytecodeProvider implements IBytecodeProvider, Closeable {
    HashMap<String, FileSystem> map = new HashMap<>();

    public TBytecodeProvider(List<Path> paths) {
        for (Path p : paths) {
            map.put(p.toFile().toString(), TUtil.newJarFileSystem(p));
        } 
    }

    @Override
    public byte[] getBytecode(String externalPath, String internalPath) throws IOException {
        if (internalPath == null) {
            return Files.readAllBytes(Paths.get(externalPath));
        } else {
            return Files.readAllBytes(map.get(externalPath).getPath(internalPath));
        }
    }

    @Override
    public void close() throws IOException {
        for (FileSystem f : map.values()) {
            f.close();
        }
    }
    
}
