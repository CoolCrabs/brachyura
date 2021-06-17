package io.github.coolcrabs.fabricmerge;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import org.junit.jupiter.api.Test;

class JarMergerTest {
    @Test
    void merge1_16_5() {
        assertDoesNotThrow(() -> {
            Path client = Files.createTempFile("client-", ".jar");
            Path server = Files.createTempFile("server-", ".jar");
            Path merged = Files.createTempFile("merged-", ".jar");
            System.out.println("downloading 1.16.5 client");
            try (InputStream clientStream = new URL("https://launcher.mojang.com/v1/objects/37fd3c903861eeff3bc24b71eed48f828b5269c8/client.jar").openStream()) {
                Files.copy(clientStream, client, StandardCopyOption.REPLACE_EXISTING);
            }
            System.out.println("downloading 1.16.5 server");
            try (InputStream serverStream = new URL("https://launcher.mojang.com/v1/objects/1b557e7b033b583cd9f66746b7a9ab1ec1673ced/server.jar").openStream()) {
                Files.copy(serverStream, server, StandardCopyOption.REPLACE_EXISTING);
            }
            System.out.println("merging");
            long start = System.currentTimeMillis();
            try (JarMerger merger = new JarMerger(client.toFile(), server.toFile(), merged.toFile())) {
                merger.enableSyntheticParamsOffset();
                merger.merge();
            }
            long time = System.currentTimeMillis() - start;
            System.out.println("merged in " + time + " ms");
            System.out.println(merged); // For manual inspection
        });
    }    
}
