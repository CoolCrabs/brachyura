package io.github.coolcrabs.brachyura.fabric;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.tinylog.Logger;

import io.github.coolcrabs.brachyura.mappings.Namespaces;
import io.github.coolcrabs.brachyura.maven.MavenId;
import net.fabricmc.mappingio.tree.MappingTree.MethodArgMapping;
import net.fabricmc.mappingio.tree.MappingTree.MethodMapping;

class YarnTest {
    @Test
    void modernYarn() {
        Yarn yarn = Yarn.ofMaven(FabricMaven.URL, FabricMaven.yarn("1.16.5+build.10"));
        assertNotNull(yarn.tree);
        assertEquals("net/minecraft/block/Block", yarn.tree.getClass("net/minecraft/class_2248", yarn.tree.getNamespaceId(Namespaces.INTERMEDIARY)).getName(Namespaces.NAMED));
        assertEquals(Namespaces.INTERMEDIARY, yarn.tree.getNamespaceName(yarn.tree.getNamespaceId(Namespaces.INTERMEDIARY)));
        assertEquals(Namespaces.NAMED, yarn.tree.getNamespaceName(yarn.tree.getNamespaceId(Namespaces.NAMED)));
        MethodMapping methodMapping = yarn.tree.getClass("net/minecraft/class_2586", yarn.tree.getNamespaceId(Namespaces.INTERMEDIARY)).getMethod("method_11009", "(Lnet/minecraft/class_1937;Lnet/minecraft/class_2338;)V", yarn.tree.getNamespaceId(Namespaces.INTERMEDIARY));
        for (MethodArgMapping methodArgMapping : methodMapping.getArgs()) {
            Logger.info(methodArgMapping.getLvIndex() + " " + methodArgMapping.getDstName(yarn.tree.getNamespaceId(Namespaces.NAMED)));
        }
        assertEquals("world", methodMapping.getArgs().stream().filter(arg -> arg.getLvIndex() == 1).findFirst().get().getDstName(yarn.tree.getNamespaceId(Namespaces.NAMED)));
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
