package io.github.coolcrabs.brachyura.recombobulator.attribute;

import java.nio.ByteBuffer;

import io.github.coolcrabs.brachyura.recombobulator.ByteBufferUtil;
import io.github.coolcrabs.brachyura.recombobulator.ClassDecodeException;
import io.github.coolcrabs.brachyura.recombobulator.RecombobulatorOutput;
import io.github.coolcrabs.brachyura.recombobulator.RecombobulatorVisitor;

public abstract class VerificationType {
    public static final VerificationTypeNonreference TOP_TYPE = new VerificationTypeNonreference((byte)0);
    public static final VerificationTypeNonreference INTEGER_TYPE = new VerificationTypeNonreference((byte)1);
    public static final VerificationTypeNonreference FLOAT_TYPE = new VerificationTypeNonreference((byte)2);
    public static final VerificationTypeNonreference DOUBLE_TYPE = new VerificationTypeNonreference((byte)3);
    public static final VerificationTypeNonreference LONG_TYPE = new VerificationTypeNonreference((byte)4);
    public static final VerificationTypeNonreference NULL_TYPE = new VerificationTypeNonreference((byte)5);
    public static final VerificationTypeNonreference UNINITIALIZED_THIS_TYPE = new VerificationTypeNonreference((byte)6);

    public final byte tag;

    VerificationType(byte tag) {
        this.tag = tag;
    }

    public static VerificationType read(ByteBuffer b, int pos) {
        byte tag = b.get(pos);
        switch (tag) {
            case 0:
                return TOP_TYPE;
            case 1:
                return INTEGER_TYPE;
            case 2:
                return FLOAT_TYPE;
            case 3:
                return DOUBLE_TYPE;
            case 4:
                return LONG_TYPE;
            case 5:
                return NULL_TYPE;
            case 6:
                return UNINITIALIZED_THIS_TYPE;
            case 7:
                int cpool_index = ByteBufferUtil.u2(b, pos + 1);
                return new VerificationTypeObject(cpool_index);
            case 8:
                int offset = ByteBufferUtil.u2(b, pos + 1);
                return new VerificationTypeUninitialized(offset);
            default:
                throw new ClassDecodeException("Unknown verification type: " + Integer.toHexString(tag));
        }
    }

    public int byteSize() {
        return 1;
    }

    public void write(RecombobulatorOutput o) {
        o.writeByte((byte)tag);
    }

    public abstract void accept(RecombobulatorVisitor v);

    @Override
    public int hashCode() {
        int result = 17;
        result = 37*result + tag;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof VerificationType) {
            VerificationType o = (VerificationType) obj;
            return tag == o.tag;
        }
        return false;
    }
}
