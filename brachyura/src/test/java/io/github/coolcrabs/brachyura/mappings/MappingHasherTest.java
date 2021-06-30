package io.github.coolcrabs.brachyura.mappings;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.security.MessageDigest;

import org.junit.jupiter.api.Test;
import org.tinylog.Logger;

import io.github.coolcrabs.brachyura.fabric.FabricMaven;
import io.github.coolcrabs.brachyura.fabric.Intermediary;
import io.github.coolcrabs.brachyura.fabric.Yarn;
import io.github.coolcrabs.brachyura.util.MessageDigestUtil;

class MappingHasherTest {
    @Test
    void hashYarn() throws IOException {
        Yarn yarn = Yarn.ofMaven(FabricMaven.URL, FabricMaven.yarn("1.17+build.13"));
        MessageDigest digest = MessageDigestUtil.messageDigest(MessageDigestUtil.SHA256);
        long start = System.currentTimeMillis();
        MappingHasher mappingHasher = new MappingHasher(digest);
        yarn.tree.accept(mappingHasher);
        String hash = MessageDigestUtil.toHexHash(digest.digest());
        long time = System.currentTimeMillis() - start;
        Logger.info("Hashed yarn in " + time + "ms " + hash);
        assertEquals("5560AFB7DF948C88A79495E6C3098FD6920116F3EDA59CBC662153DBC639F1DB", hash);
    }

    @Test
    void hashYarn2() throws IOException {
        Yarn yarn = Yarn.ofMaven(FabricMaven.URL, FabricMaven.yarn("1.17+build.13"));
        long start = System.currentTimeMillis();
        String hash = MappingHasher.hashSha256(yarn.tree);
        long time = System.currentTimeMillis() - start;
        Logger.info("Hashed yarn (test 2) in " + time + "ms " + hash);
        assertEquals("5560AFB7DF948C88A79495E6C3098FD6920116F3EDA59CBC662153DBC639F1DB", hash);
    }

    @Test
    void hashIntermediary() throws IOException {
        Intermediary intermediary = Intermediary.ofMaven(FabricMaven.URL, FabricMaven.intermediary("1.17"));
        MessageDigest digest = MessageDigestUtil.messageDigest(MessageDigestUtil.SHA256);
        long start = System.currentTimeMillis();
        MappingHasher mappingHasher = new MappingHasher(digest);
        intermediary.tree.accept(mappingHasher);
        String hash = MessageDigestUtil.toHexHash(digest.digest());
        long time = System.currentTimeMillis() - start;
        Logger.info("Hashed intermediary in " + time + "ms " + hash);
        assertEquals("9B1063C9296B2588237B4DA89A171B6E781674BF93D080A16DC509044C44274D", hash);
    }
}
