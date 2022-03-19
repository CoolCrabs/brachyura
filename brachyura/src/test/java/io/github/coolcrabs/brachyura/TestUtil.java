package io.github.coolcrabs.brachyura;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.Locale;

import io.github.coolcrabs.brachyura.util.MessageDigestUtil;
import io.github.coolcrabs.brachyura.util.PathUtil;

public class TestUtil {
    public static void assertSha256(Path file, String expected) {
        assertDoesNotThrow(() -> {
            MessageDigest md = MessageDigestUtil.messageDigest(MessageDigestUtil.SHA256);
            try (DigestInputStream i = new DigestInputStream(PathUtil.inputStream(file), md)) {
                byte[] tmp = new byte[1024];
                while (i.read(tmp) != -1);
            }
            assertEquals(expected.toLowerCase(Locale.ENGLISH), MessageDigestUtil.toLowerCaseHexHash(md.digest()));
        });
    }
}
