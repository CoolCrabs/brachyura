package io.github.coolcrabs.brachyura.profiler;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

class ProfilerTest {
    @Test
    void e() throws IOException, InterruptedException {
        assertTrue(ProfilePlugin.INSTANCE.init());
        Path a = Files.createTempFile("bruh", ".jfr");
        Long b = ProfilePlugin.INSTANCE.startRecording(a);
        long start = System.currentTimeMillis();
        double bruh = 5;
        while (System.currentTimeMillis() - start < 5000) {
            bruh += Math.random();
        }
        System.out.println(bruh);
        ProfilePlugin.INSTANCE.stopRecording(b);
        assertTrue(Files.exists(a));
        System.out.println(a);
    }    
}
