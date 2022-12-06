package io.github.coolcrabs.brachyura.recombobulator.attribute;

import java.nio.ByteBuffer;
import java.util.EnumSet;
import java.util.HashMap;

import io.github.coolcrabs.brachyura.recombobulator.ConstantPool;
import io.github.coolcrabs.brachyura.recombobulator.Mutf8Slice;
import io.github.coolcrabs.brachyura.recombobulator.RecombobulatorOptions;

public abstract class AttributeType {
    public static final HashMap<Mutf8Slice, AttributeType> attributeMap = GenAttributeMap.attributeMap;

    final int major;
    final int minor;
    final EnumSet<Location> locations;

    public enum Location {
        CLASS_FILE,
        FIELD_INFO,
        METHOD_INFO,
        CODE,
        RECORD_COMPONENT;
    }

    AttributeType(int major, int minor, EnumSet<Location> locations) {
        this.major = major;
        this.minor = minor;
        this.locations = locations;
    }

    boolean supported(int major, int minor, Location location) {
        return (major > this.major || (major == this.major && minor >= this.minor)) && locations.contains(location);
    }

    abstract Attribute read(ByteBuffer b, int pos, int attribute_name_index, int attribute_length, RecombobulatorOptions options, ConstantPool pool, int major, int minor);
}
