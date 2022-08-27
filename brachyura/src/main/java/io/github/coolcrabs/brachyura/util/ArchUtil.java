package io.github.coolcrabs.brachyura.util;

public class ArchUtil {
    private ArchUtil() { }

    public static final Arch ARCH;

    public enum Arch {
        I386,
        X86_64,
        PPC64,
        PPC64LE,
        AARCH64,
        UNKNOWN
    }

    static {
        // https://github.com/java-native-access/jna/blob/bf60e51eace6dffa18548019e2ba398ff84904ef/src/com/sun/jna/Platform.java
        String osArch = System.getProperty("os.arch");
        switch (osArch) {
            case "i386":
            case "i686":
            case "x86":
                ARCH = Arch.I386;
                break;
            case "x86_64":
            case "x86-64": // is this real?
            case "amd64":
                ARCH = Arch.X86_64;
                break;
            case "powerpc64":
            case "ppc64":
                if ("little".equals(System.getProperty("sun.cpu.endian"))) {
                    ARCH = Arch.PPC64LE;
                } else {
                    ARCH = Arch.PPC64;
                }
                break;
            case "powerpc64le":
            case "ppc64le":
                ARCH = Arch.PPC64LE;
                break;
            case "aarch64":
                ARCH = Arch.AARCH64;
                break;
            default:
                ARCH = Arch.UNKNOWN;
                break;
        }
    }
}
