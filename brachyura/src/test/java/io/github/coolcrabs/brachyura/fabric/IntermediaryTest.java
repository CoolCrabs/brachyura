package io.github.coolcrabs.brachyura.fabric;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import io.github.coolcrabs.brachyura.mappings.Namespaces;

class IntermediaryTest {
    @Test
    void intermediary1_16_5() {
        Intermediary standardIntermediary = Intermediary.ofMaven(FabricMaven.URL, FabricMaven.intermediary("1.16.5"));
        assertNotNull(standardIntermediary.tree);
        assertEquals(Namespaces.OBF, standardIntermediary.tree.getSrcNamespace());
        assertEquals(Namespaces.INTERMEDIARY, standardIntermediary.tree.getDstNamespaces().get(0));
        assertEquals(Namespaces.INTERMEDIARY, standardIntermediary.tree.getDstNamespaces().get(0));
        assertEquals("net/minecraft/class_2248", standardIntermediary.tree.getClass("buo").getDstName(0));
    }
}
