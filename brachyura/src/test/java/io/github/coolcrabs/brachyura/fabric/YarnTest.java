package io.github.coolcrabs.brachyura.fabric;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import io.github.coolcrabs.brachyura.maven.MavenId;

class YarnTest {
    @Test
    void modernYarn() {
        Yarn yarn = Yarn.ofMaven("https://maven.fabricmc.net/", new MavenId("net.fabricmc", "yarn", "1.17+build.13"));
        assertNotNull(yarn.tree);
    }
}
