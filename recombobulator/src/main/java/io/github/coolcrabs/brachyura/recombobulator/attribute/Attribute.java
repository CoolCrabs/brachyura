package io.github.coolcrabs.brachyura.recombobulator.attribute;

import java.nio.ByteBuffer;
import java.util.List;

import io.github.coolcrabs.brachyura.recombobulator.ConstantPool;
import io.github.coolcrabs.brachyura.recombobulator.ConstantUtf8;
import io.github.coolcrabs.brachyura.recombobulator.Mutf8Slice;
import io.github.coolcrabs.brachyura.recombobulator.RecombobulatorOptions;
import io.github.coolcrabs.brachyura.recombobulator.RecombobulatorOutput;
import io.github.coolcrabs.brachyura.recombobulator.RecombobulatorVisitor;

import static io.github.coolcrabs.brachyura.recombobulator.ByteBufferUtil.*;

public abstract class Attribute {
    public int attribute_name_index;

    Attribute(int attribute_name_index) {
        this.attribute_name_index = attribute_name_index;
    }

    /**
     * Size excluding name index and length
     */
    public abstract int byteSize();

    public abstract void write(RecombobulatorOutput o);

    public abstract void accept(RecombobulatorVisitor v);

    @Override
    public abstract int hashCode();

    @Override
    public abstract boolean equals(Object obj);
}
