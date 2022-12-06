package io.github.coolcrabs.brachyura.recombobulator;

public class ConstantDouble extends ConstantPoolEntry {
    public final double data;

    public ConstantDouble(double data) {
        this.data = data;
    }

    @Override
    byte tag() {
        return ConstantPool.CONSTANT_Double;
    }

    @Override
    int byteSize() {
        return 8;
    }

    @Override
    void write(RecombobulatorOutput o) {
        o.writeDouble(data);
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 37*result + Double.hashCode(data);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj instanceof ConstantDouble) {
            ConstantDouble o = (ConstantDouble) obj;
            if (data == o.data) return true;
        }
        return false;
    }
}
