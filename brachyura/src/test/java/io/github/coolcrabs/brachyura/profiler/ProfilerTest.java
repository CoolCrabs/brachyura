package io.github.coolcrabs.brachyura.profiler;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

class ProfilerTest {
    @Test
    void e() throws IOException {
        assertTrue(ProfilePlugin.INSTANCE.init());
        Path a = Files.createTempFile("bruh", ".jfr");
        Long b = ProfilePlugin.INSTANCE.startRecording(a);
        String yeet = "yeet";
        for (int i = 0; i < 10000; i++) {
            yeet += System.currentTimeMillis();
        }
        assertNotNull(yeet);
        ProfilePlugin.INSTANCE.stopRecording(b);
        assertTrue(Files.exists(a));
        System.out.println(a);
    }    
}
