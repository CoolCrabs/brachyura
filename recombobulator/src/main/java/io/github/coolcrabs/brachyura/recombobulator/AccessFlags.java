package io.github.coolcrabs.brachyura.recombobulator;

public class AccessFlags {
    private AccessFlags() { }

    public static int ACC_PUBLIC = 0x0001;
    public static int ACC_PRIVATE = 0x0002;
    public static int ACC_PROTECTED = 0x0004;
    public static int ACC_STATIC = 0x0008;
    public static int ACC_FINAL = 0x0010;
    public static int ACC_SUPER = 0x0020;
    public static int ACC_SYNCHRONIZED = 0x0020;
    public static int ACC_BRIDGE = 0x0040;
    public static int ACC_VARARGS = 0x0080;
    public static int ACC_NATIVE = 0x0100;
    public static int ACC_INTERFACE = 0x0200;
    public static int ACC_ABSTRACT = 0x0400;
    public static int ACC_STRICT = 0x0800;
    public static int ACC_SYNTHETIC = 0x1000;
    public static int ACC_ANNOTATION = 0x2000;
    public static int ACC_ENUM = 0x4000;
    public static int ACC_MODULE = 0x8000;
}
