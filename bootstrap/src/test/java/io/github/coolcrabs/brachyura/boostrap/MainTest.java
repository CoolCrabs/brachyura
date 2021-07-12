package io.github.coolcrabs.brachyura.boostrap;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import io.github.coolcrabs.brachyura.bootstrap.Main;

class MainTest {
    @Disabled
    @Test
    void b() {
        assertDoesNotThrow(() -> {
            Main.main(new String[] {});
        });
    }
}
