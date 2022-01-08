package org.jetbrains.java.decompiler.main;

import java.io.File;
import java.util.Map;

import org.jetbrains.java.decompiler.main.extern.IBytecodeProvider;
import org.jetbrains.java.decompiler.main.extern.IFernflowerLogger;
import org.jetbrains.java.decompiler.main.extern.IResultSaver;

public class Fernflower {
    public Fernflower(IBytecodeProvider provider, IResultSaver saver, Map<String, Object> customProperties, IFernflowerLogger logger) {
    }

    public void addSource(File source) {
    }

    public void addLibrary(File library) {
    }

    public void decompileContext() {
    }
}
