package io.github.coolcrabs.brachyura.minecraft;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class LauncherMetaDownloaderTest {
    @Test
    void downloadLauncherMeta() {
        assertDoesNotThrow(() -> {
            LauncherMeta meta = LauncherMetaDownloader.getLauncherMeta();
            assertNotNull(meta.latest.release);
        });
    }
}
