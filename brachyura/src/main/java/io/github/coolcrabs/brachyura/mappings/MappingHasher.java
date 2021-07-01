package io.github.coolcrabs.brachyura.mappings;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;

import io.github.coolcrabs.brachyura.util.MessageDigestUtil;
import io.github.coolcrabs.brachyura.util.Util;
import net.fabricmc.mappingio.MappedElementKind;
import net.fabricmc.mappingio.MappingVisitor;
import net.fabricmc.mappingio.tree.MappingTree;

public class MappingHasher implements MappingVisitor {
    private final MessageDigest messageDigest;

    public MappingHasher(MessageDigest messageDigest) {
        this.messageDigest = messageDigest;
    }

    public static String hashSha256(MappingTree... trees) {
        MessageDigest digest = MessageDigestUtil.messageDigest(MessageDigestUtil.SHA256);
        MappingHasher mappingHasher = new MappingHasher(digest);
        try {
            for (MappingTree tree : trees) {
                tree.accept(mappingHasher);
            }
        } catch (IOException e) {
            throw Util.sneak(e);
        }
        return MessageDigestUtil.toHexHash(digest.digest());
    }

    private void update(String string) {
        if (string != null) {
            messageDigest.update(string.getBytes(StandardCharsets.UTF_8));
        }
    }

    private void update(List<String> strings) {
        for (String string : strings) {
            update(string);
        }
    }

    private void update(int i) {
        messageDigest.update(
            new byte[] {
                (byte)(i >>> 24),
                (byte)(i >>> 16),
                (byte)(i >>> 8),
                (byte)i
            }
        );
    }

    private void update(MappedElementKind kind) {
        update(kind.ordinal());
    }

    @Override
    public void visitNamespaces(String srcNamespace, List<String> dstNamespaces) throws IOException {
        update(srcNamespace);
        update(dstNamespaces);
    }

    @Override
    public boolean visitClass(String srcName) throws IOException {
        update(srcName);
        return true;
    }

    @Override
    public boolean visitField(String srcName, String srcDesc) throws IOException {
        update(srcName);
        update(srcDesc);
        return true;
    }

    @Override
    public boolean visitMethod(String srcName, String srcDesc) throws IOException {
        update(srcName);
        update(srcDesc);
        return true;
    }

    @Override
    public boolean visitMethodArg(int argPosition, int lvIndex, String srcName) throws IOException {
        update(argPosition);
        update(lvIndex);
        update(srcName);
        return true;
    }

    @Override
    public boolean visitMethodVar(int lvtRowIndex, int lvIndex, int startOpIdx, String srcName) throws IOException {
        update(lvtRowIndex);
        update(lvIndex);
        update(startOpIdx);
        update(srcName);
        return true;
    }

    @Override
    public void visitDstName(MappedElementKind targetKind, int namespace, String name) throws IOException {
        update(targetKind);
        update(namespace);
        update(name);
    }

    @Override
    public void visitComment(MappedElementKind targetKind, String comment) throws IOException {
        update(targetKind);
        update(comment);
    }
}
