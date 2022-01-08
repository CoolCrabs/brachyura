package net.fabricmc.fernflower.api;

import org.jetbrains.java.decompiler.main.extern.IResultSaver;

public interface IFabricResultSaver extends IResultSaver {
    void saveClassEntry(String path, String archiveName, String qualifiedName, String entryName, String content, int[] mapping);
}
