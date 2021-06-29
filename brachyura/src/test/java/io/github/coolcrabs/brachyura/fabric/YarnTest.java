package io.github.coolcrabs.brachyura.fabric;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import io.github.coolcrabs.brachyura.mappings.Namespaces;
import io.github.coolcrabs.brachyura.maven.MavenId;

class YarnTest {
    @Test
    void modernYarn() {
        Yarn yarn = Yarn.ofMaven(FabricMaven.URL, FabricMaven.yarn("1.17+build.13"));
        assertNotNull(yarn.tree);
        assertEquals("net/minecraft/block/Block", yarn.tree.getClass("net/minecraft/class_2248").getDstName(0));
        assertEquals(Namespaces.INTERMEDIARY, yarn.tree.getSrcNamespace());
        assertEquals(Namespaces.NAMED, yarn.tree.getDstNamespaces().get(0));
    }

    @Test
    void oldYarn() {
        Yarn yarn = Yarn.ofMaven(FabricMaven.URL, new MavenId("net.fabricmc", "pomf", "18w44a.1"));
        assertNotNull(yarn.tree);
        assertEquals("net/minecraft/block/Block", yarn.tree.getClass("bet").getDstName(0));
        assertEquals(Namespaces.OBF, yarn.tree.getSrcNamespace());
        assertEquals(Namespaces.NAMED, yarn.tree.getDstNamespaces().get(0));
    }
}
