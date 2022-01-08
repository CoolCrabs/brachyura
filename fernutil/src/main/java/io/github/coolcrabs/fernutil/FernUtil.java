package io.github.coolcrabs.fernutil;

import java.lang.invoke.MethodHandles;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;

public class FernUtil {
    FernUtil() { }

    public static void decompile(Path fernflower, Path inJar, Path outSources, List<Path> cp, Consumer<LineNumbers> lines, JavadocProvider provider) {
        try {
            try (FUClassLoader classLoader = new FUClassLoader(new URL[]{fernflower.toUri().toURL(), FernUtil.class.getProtectionDomain().getCodeSource().getLocation()})) {
                Class<?> jump = Class.forName("io.github.coolcrabs.fernutil.TJump$PackageHack", true, classLoader); // Different classloaders so can't package private lookup :()
                MethodHandles.publicLookup().unreflect(jump.getMethods()[0]).invokeWithArguments(inJar, outSources, cp, lines, provider == null ? NullJavadocProvider.INSTANCE : provider);
            }
        } catch (Throwable e) {
            sneak(e);
        }
    }

    public static <E extends Throwable> void sneak(Throwable e) throws E {
        throw (E) e;
    }

    public static class LineNumbers {
        public final String clazz;
        public final int[] mapping;

        public LineNumbers(String clazz, int[] mapping) {
            this.clazz = clazz;
            this.mapping = mapping;
        }
    }

    public interface JavadocProvider {
        String clazzDoc(String clazz);

        String methodDoc(String clazz, String desc, String method);

        String fieldDoc(String clazz, String desc, String field);
    }

    enum NullJavadocProvider implements JavadocProvider {
        INSTANCE;

        @Override
        public String clazzDoc(String clazz) {
            return null;
        }

        @Override
        public String methodDoc(String clazz, String desc, String method) {
            return null;
        }

        @Override
        public String fieldDoc(String clazz, String desc, String field) {
            return null;
        }
    }
}
