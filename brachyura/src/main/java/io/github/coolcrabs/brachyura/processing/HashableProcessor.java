package io.github.coolcrabs.brachyura.processing;

import java.security.MessageDigest;

public interface HashableProcessor extends Processor {
    void hash(MessageDigest md);
}
