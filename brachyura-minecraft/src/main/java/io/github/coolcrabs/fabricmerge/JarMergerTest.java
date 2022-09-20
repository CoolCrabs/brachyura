package io.github.coolcrabs.fabricmerge;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import io.github.coolcrabs.brachyura.minecraft.Minecraft;
import io.github.coolcrabs.brachyura.minecraft.VersionMeta;

class JarMergerTest {
    @Test
    void merge1_16_5() throws IOException {
        Path merged = Files.createTempFile("merged-", ".jar");
        VersionMeta vm = Minecraft.getVersion("1.16.5");
        Path client = Minecraft.getDownload(vm, "client");
        Path server = Minecraft.getDownload(vm, "server");
        System.out.println("merging");
        long start = System.currentTimeMillis();
        try (JarMerger merger = new JarMerger(client, server, merged)) {
            merger.enableSyntheticParamsOffset();
            merger.merge();
        }
        long time = System.currentTimeMillis() - start;
        System.out.println("merged in " + time + " ms");
        System.out.println(merged); // For manual inspection
    }    
}
