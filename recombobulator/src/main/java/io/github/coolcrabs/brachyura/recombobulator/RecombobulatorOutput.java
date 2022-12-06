package io.github.coolcrabs.brachyura.recombobulator;

import java.io.DataOutput;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public interface RecombobulatorOutput {
    void writeByte(byte d);
    void writeShort(short d);
    void writeInt(int d);
    void writeLong(long d);
    void writeFloat(float d);
    void writeDouble(double d);
    void writeBytes(ByteBuffer buf);

    public static RecombobulatorOutput of(ByteBuffer b) {
        b.order(ByteOrder.BIG_ENDIAN);
        return new ByteBufferOutput(b);
    }

    public static RecombobulatorOutput of(DataOutput b) {
        return new DataOutputOutput(b);
    }

    static class ByteBufferOutput implements RecombobulatorOutput {
        ByteBuffer b;

        ByteBufferOutput(ByteBuffer b) {
            this.b = b;
        }

        @Override
        public void writeByte(byte d) {
            b.put(d);
        }

        @Override
        public void writeShort(short d) {
            b.putShort(d);
        }

        @Override
        public void writeInt(int d) {
            b.putInt(d);
        }

        @Override
        public void writeLong(long d) {
            b.putLong(d);
        }

        @Override
        public void writeFloat(float d) {
            b.putFloat(d);
        }

        @Override
        public void writeDouble(double d) {
            b.putDouble(d);
        }

        @Override
        public void writeBytes(ByteBuffer buf) {
            if (buf.hasArray()) {
                b.put(buf.array(), buf.position(), buf.limit() - buf.position());
            } else {
                ByteBuffer slice = buf.duplicate();
                b.put(slice);
            }
        }
    }

    static class DataOutputOutput implements RecombobulatorOutput {
        DataOutput o;

        DataOutputOutput(DataOutput o) {
            this.o = o;
        }

        @Override
        public void writeByte(byte d) {
            try {
                o.write(d);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }

        @Override
        public void writeShort(short d) {
            try {
                o.writeShort(d);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }

        @Override
        public void writeInt(int d) {
            try {
                o.writeInt(d);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }

        @Override
        public void writeLong(long d) {
            try {
                o.writeLong(d);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }

        @Override
        public void writeFloat(float d) {
            try {
                o.writeFloat(d);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }

        @Override
        public void writeDouble(double d) {
            try {
                o.writeDouble(d);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }

        @Override
        public void writeBytes(ByteBuffer buf) {
            try {
                if (buf.hasArray()) {
                    o.write(buf.array(), buf.position(), buf.limit());
                } else {
                    int start = buf.position();
                    int end = buf.limit();
                    byte[] tmp = new byte[Math.min(end - start, 256)];
                    int i = start;
                    while (i <= end) {
                        int length = Math.min(end - i, tmp.length);
                        buf.get(tmp, 0, length);
                        o.write(tmp, 0, length);
                        i += length;
                    }
                }
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }
}
