package io.github.coolcrabs.brachyura.util;

import java.io.File;

import org.tinylog.Logger;

public class JvmUtil {
    private JvmUtil() { }

    public static final int CURRENT_JAVA_VERSION;
    public static final String CURRENT_JAVA_EXECUTABLE;

    private static final String[] NO_ARGS = new String[0];

    static {
        // https://stackoverflow.com/a/2591122
        String version = System.getProperty("java.version");
        if (version.startsWith("1.")) {
            version = version.substring(2, 3);
        } else {
            int dot = version.indexOf(".");
            if (dot != -1) {
                version = version.substring(0, dot);
            }
        }
        CURRENT_JAVA_VERSION = Integer.parseInt(version);
        // https://stackoverflow.com/a/46852384
        String javaHome = System.getProperty("java.home");
        File bin = new File(javaHome, "bin");
        File exe = new File(bin, "java");
        if (exe.exists()) {
            CURRENT_JAVA_EXECUTABLE = exe.getAbsolutePath();
        } else {
            exe = new File(bin, "java.exe");
            if (exe.exists()) {
                CURRENT_JAVA_EXECUTABLE = exe.getAbsolutePath();
            } else {
                // Give Up
                CURRENT_JAVA_EXECUTABLE = "java";
                Logger.error("Unable to find java executable in java.home");
            }
        }
    }

    public static boolean canCompile(int compilerversion, int targetversion) {
        return compilerversion == targetversion || (compilerversion >= 9 && targetversion >= 7);
    }

    public static String javaVersionString(int javaversion) {
        return javaversion < 9 ? "1." + javaversion : Integer.toString(javaversion);
    }

    public static String[] compileArgs(int compilerversion, int targetversion) {
        if (compilerversion == targetversion) return NO_ARGS;
        if (compilerversion >= 9 && targetversion >= 7) return new String[] {"--release", javaVersionString(targetversion)};
        throw new UnsupportedOperationException("Target Version: " + targetversion + " " + "Compiler Version: " + compilerversion);
    }
}
