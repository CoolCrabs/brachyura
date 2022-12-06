package io.github.coolcrabs.brachyura.recombobulator.attribute;

import io.github.coolcrabs.brachyura.recombobulator.RecombobulatorVisitor;

public class VerificationTypeNonreference extends VerificationType {
    VerificationTypeNonreference(byte tag) {
        super(tag);
    }

    @Override
    public void accept(RecombobulatorVisitor v) {
        v.visitVerificationTypeNonreference(this);
    }
}
