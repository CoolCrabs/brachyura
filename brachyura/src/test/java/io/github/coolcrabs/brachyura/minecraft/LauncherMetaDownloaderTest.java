package io.github.coolcrabs.brachyura.minecraft;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.Test;

class LauncherMetaDownloaderTest {
    @Test
    void downloadLauncherMeta() {
        assertDoesNotThrow(() -> {
            LauncherMeta meta = LauncherMetaDownloader.getLauncherMeta();
            System.out.println(meta.latest.release);
        });
    }
}
