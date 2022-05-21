package io.github.coolcrabs.brachyura.fabric;

import java.security.MessageDigest;
import java.util.function.Consumer;

import io.github.coolcrabs.accesswidener.AccessWidenerVisitor;
import io.github.coolcrabs.accesswidener.AccessWidenerReader.AccessType;
import io.github.coolcrabs.brachyura.util.MessageDigestUtil;

public class AccessWidenerHasher implements AccessWidenerVisitor {
    private final MessageDigest md;

    public AccessWidenerHasher(MessageDigest md) {
        this.md = md;
    }

    public static String hash256(Consumer<AccessWidenerVisitor> aw) {
        MessageDigest md = MessageDigestUtil.messageDigest(MessageDigestUtil.SHA256);
        hash(md, aw);
        return MessageDigestUtil.toHexHash(md.digest());
    }

    public static void hash(MessageDigest md, Consumer<AccessWidenerVisitor> aw) {
        AccessWidenerHasher hasher = new AccessWidenerHasher(md);
        aw.accept(hasher);
    }

    @Override
    public void visitHeader(String namespace) {
        MessageDigestUtil.update(md, namespace);
    }

    @Override
    public void visitClass(String name, AccessType access, boolean transitive) {
        MessageDigestUtil.update(md, name);
        MessageDigestUtil.update(md, access.toString());
    }

    @Override
    public void visitField(String owner, String name, String descriptor, AccessType access, boolean transitive) {
        MessageDigestUtil.update(md, owner);
        MessageDigestUtil.update(md, name);
        MessageDigestUtil.update(md, descriptor);
        MessageDigestUtil.update(md, access.toString());
    }

    @Override
    public void visitMethod(String owner, String name, String descriptor, AccessType access, boolean transitive) {
        MessageDigestUtil.update(md, owner);
        MessageDigestUtil.update(md, name);
        MessageDigestUtil.update(md, descriptor);
        MessageDigestUtil.update(md, access.toString());
    }
}
