package io.github.coolcrabs.brachyura.recombobulator.attribute;

// GENERATED CLASS :)

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import io.github.coolcrabs.brachyura.recombobulator.ByteBufferUtil;
import io.github.coolcrabs.brachyura.recombobulator.ConstantPool;
import io.github.coolcrabs.brachyura.recombobulator.Mutf8Slice;
import io.github.coolcrabs.brachyura.recombobulator.RecombobulatorOptions;
import io.github.coolcrabs.brachyura.recombobulator.RecombobulatorOutput;
import io.github.coolcrabs.brachyura.recombobulator.RecombobulatorVisitor;
import io.github.coolcrabs.brachyura.recombobulator.U2Slice;

public final class AttributeCode extends Attribute {
    public static final Mutf8Slice NAME = new Mutf8Slice("Code");

    public static final AttributeType TYPE = new AttributeType(45, 3, EnumSet.of(AttributeType.Location.METHOD_INFO)) {
        @Override
        Attribute read(ByteBuffer b, int pos, int attribute_name_index, int attribute_length, RecombobulatorOptions options, ConstantPool pool, int major, int minor) {
            int max_stack = ByteBufferUtil.u2(b, pos);
            pos += 2;
            int max_locals = ByteBufferUtil.u2(b, pos);
            pos += 2;
            int code_length = b.getInt(pos);
            pos += 4;
            ByteBuffer code = ByteBufferUtil.slice(b, pos, pos + code_length);
            pos += code_length;
            int exception_table_length = ByteBufferUtil.u2(b, pos);
            pos += 2;
            List<EntryExceptionTable> exception_table = new ArrayList<>(exception_table_length);
            for (int i = 0; i < exception_table_length; i++) {
                EntryExceptionTable tmp = EntryExceptionTable.read(b, pos, attribute_name_index, attribute_length, options, pool, major, minor);
                pos += tmp.byteSize();
                exception_table.add(tmp);
            }
            Attributes attributes = Attributes.read(b, pos, options, pool, major, minor, AttributeType.Location.CODE);
            pos = attributes.readEnd();
            return new AttributeCode(
                attribute_name_index,
                max_stack,
                max_locals,
                code,
                exception_table,
                attributes
            );
        }
    };

    public int max_stack;
    public int max_locals;
    public ByteBuffer code;
    public List<EntryExceptionTable> exception_table;
    public Attributes attributes;

    public AttributeCode(int attribute_name_index, int max_stack, int max_locals, ByteBuffer code, List<EntryExceptionTable> exception_table, Attributes attributes) {
        super(attribute_name_index);
        this.max_stack = max_stack;
        this.max_locals = max_locals;
        this.code = code;
        this.exception_table = exception_table;
        this.attributes = attributes;
    }

    @Override
    public int byteSize() {
        int size = 0;
        size += 2; // max_stack
        size += 2; // max_locals
        size += 4; // code_length
        size += code.remaining();
        size += 2; // exception_table_length
        for (EntryExceptionTable tmp : exception_table) size += tmp.byteSize();
        size += attributes.byteSize();
        return size;
    }

    @Override
    public void write(RecombobulatorOutput o) {
        o.writeShort((short) max_stack);
        o.writeShort((short) max_locals);
        o.writeInt((int) code.remaining());
        o.writeBytes(code);
        o.writeShort((short) exception_table.size());
        for (EntryExceptionTable tmp : exception_table) tmp.write(o);
        attributes.write(o);
    }

    public void accept(RecombobulatorVisitor v) {
        v.visitAttributeCode(this);
        for (EntryExceptionTable tmp : exception_table) tmp.accept(v);
        for (int i = 0; i < attributes.size(); i++) attributes.get(i).accept(v);
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 37*result + (int) max_stack;
        result = 37*result + (int) max_locals;
        result = 37*result + code.hashCode();
        result = 37*result + exception_table.hashCode();
        result = 37*result + attributes.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj instanceof AttributeCode) {
            AttributeCode o = (AttributeCode) obj;
            if (
                max_stack == o.max_stack &&
                max_locals == o.max_locals &&
                code.equals(o.code) &&
                exception_table.equals(o.exception_table) &&
                attributes.equals(o.attributes) &&
                attribute_name_index == o.attribute_name_index
            ) return true;
        }
        return false;
    }
}
