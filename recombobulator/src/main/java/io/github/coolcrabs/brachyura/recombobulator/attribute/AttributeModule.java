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

public final class AttributeModule extends Attribute {
    public static final Mutf8Slice NAME = new Mutf8Slice("Module");

    public static final AttributeType TYPE = new AttributeType(53, 0, EnumSet.of(AttributeType.Location.CLASS_FILE)) {
        @Override
        Attribute read(ByteBuffer b, int pos, int attribute_name_index, int attribute_length, RecombobulatorOptions options, ConstantPool pool, int major, int minor) {
            int module_name_index = ByteBufferUtil.u2(b, pos);
            pos += 2;
            int module_flags = ByteBufferUtil.u2(b, pos);
            pos += 2;
            int module_version_index = ByteBufferUtil.u2(b, pos);
            pos += 2;
            int requires_count = ByteBufferUtil.u2(b, pos);
            pos += 2;
            List<EntryRequires> requires = new ArrayList<>(requires_count);
            for (int i = 0; i < requires_count; i++) {
                EntryRequires tmp = EntryRequires.read(b, pos, attribute_name_index, attribute_length, options, pool, major, minor);
                pos += tmp.byteSize();
                requires.add(tmp);
            }
            int exports_count = ByteBufferUtil.u2(b, pos);
            pos += 2;
            List<EntryExports> exports = new ArrayList<>(exports_count);
            for (int i = 0; i < exports_count; i++) {
                EntryExports tmp = EntryExports.read(b, pos, attribute_name_index, attribute_length, options, pool, major, minor);
                pos += tmp.byteSize();
                exports.add(tmp);
            }
            int opens_count = ByteBufferUtil.u2(b, pos);
            pos += 2;
            List<EntryOpens> opens = new ArrayList<>(opens_count);
            for (int i = 0; i < opens_count; i++) {
                EntryOpens tmp = EntryOpens.read(b, pos, attribute_name_index, attribute_length, options, pool, major, minor);
                pos += tmp.byteSize();
                opens.add(tmp);
            }
            int uses_count = ByteBufferUtil.u2(b, pos);
            pos += 2;
            U2Slice uses_index = new U2Slice(ByteBufferUtil.slice(b, pos, pos + (uses_count * 2)));
            pos += uses_count * 2;
            int provides_count = ByteBufferUtil.u2(b, pos);
            pos += 2;
            List<EntryProvides> provides = new ArrayList<>(provides_count);
            for (int i = 0; i < provides_count; i++) {
                EntryProvides tmp = EntryProvides.read(b, pos, attribute_name_index, attribute_length, options, pool, major, minor);
                pos += tmp.byteSize();
                provides.add(tmp);
            }
            return new AttributeModule(
                attribute_name_index,
                module_name_index,
                module_flags,
                module_version_index,
                requires,
                exports,
                opens,
                uses_index,
                provides
            );
        }
    };

    public int module_name_index;
    public int module_flags;
    public int module_version_index;
    public List<EntryRequires> requires;
    public List<EntryExports> exports;
    public List<EntryOpens> opens;
    public U2Slice uses_index;
    public List<EntryProvides> provides;

    public AttributeModule(int attribute_name_index, int module_name_index, int module_flags, int module_version_index, List<EntryRequires> requires, List<EntryExports> exports, List<EntryOpens> opens, U2Slice uses_index, List<EntryProvides> provides) {
        super(attribute_name_index);
        this.module_name_index = module_name_index;
        this.module_flags = module_flags;
        this.module_version_index = module_version_index;
        this.requires = requires;
        this.exports = exports;
        this.opens = opens;
        this.uses_index = uses_index;
        this.provides = provides;
    }

    @Override
    public int byteSize() {
        int size = 0;
        size += 2; // module_name_index
        size += 2; // module_flags
        size += 2; // module_version_index
        size += 2; // requires_count
        for (EntryRequires tmp : requires) size += tmp.byteSize();
        size += 2; // exports_count
        for (EntryExports tmp : exports) size += tmp.byteSize();
        size += 2; // opens_count
        for (EntryOpens tmp : opens) size += tmp.byteSize();
        size += 2; // uses_count
        size += uses_index.byteSize();
        size += 2; // provides_count
        for (EntryProvides tmp : provides) size += tmp.byteSize();
        return size;
    }

    @Override
    public void write(RecombobulatorOutput o) {
        o.writeShort((short) module_name_index);
        o.writeShort((short) module_flags);
        o.writeShort((short) module_version_index);
        o.writeShort((short) requires.size());
        for (EntryRequires tmp : requires) tmp.write(o);
        o.writeShort((short) exports.size());
        for (EntryExports tmp : exports) tmp.write(o);
        o.writeShort((short) opens.size());
        for (EntryOpens tmp : opens) tmp.write(o);
        o.writeShort((short) uses_index.size());
        uses_index.write(o);
        o.writeShort((short) provides.size());
        for (EntryProvides tmp : provides) tmp.write(o);
    }

    public void accept(RecombobulatorVisitor v) {
        v.visitAttributeModule(this);
        for (EntryRequires tmp : requires) tmp.accept(v);
        for (EntryExports tmp : exports) tmp.accept(v);
        for (EntryOpens tmp : opens) tmp.accept(v);
        for (EntryProvides tmp : provides) tmp.accept(v);
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 37*result + (int) module_name_index;
        result = 37*result + (int) module_flags;
        result = 37*result + (int) module_version_index;
        result = 37*result + requires.hashCode();
        result = 37*result + exports.hashCode();
        result = 37*result + opens.hashCode();
        result = 37*result + uses_index.hashCode();
        result = 37*result + provides.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj instanceof AttributeModule) {
            AttributeModule o = (AttributeModule) obj;
            if (
                module_name_index == o.module_name_index &&
                module_flags == o.module_flags &&
                module_version_index == o.module_version_index &&
                requires.equals(o.requires) &&
                exports.equals(o.exports) &&
                opens.equals(o.opens) &&
                uses_index.equals(o.uses_index) &&
                provides.equals(o.provides) &&
                attribute_name_index == o.attribute_name_index
            ) return true;
        }
        return false;
    }
}
