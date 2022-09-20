import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributeView;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.tools.JavaCompiler;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

/*
 * Written to avoid inner/annon classes so it can be run with java Buildscript.java without creating class files
 */
public class Buildscript {
    public static final int CURRENT_JAVA_VERSION;

    public static final Path ROOT;
    public static final Path BRACHYURA;
    public static final Path DOT_BUILDSCRIPT;
    public static final Path DEPS;
    public static final Path BUILD;

    static {
        try {
            // https://stackoverflow.com/a/2591122
            // Changed to java.specification.version to avoid -ea and other various odditites
            // See https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/lang/System.html#getProperties()
            // and https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/lang/Runtime.Version.html
            String version = System.getProperty("java.specification.version");
            if (version.startsWith("1.")) {
                version = version.substring(2, 3);
            } else {
                int dot = version.indexOf(".");
                if (dot != -1) {
                    version = version.substring(0, dot);
                }
            }
            CURRENT_JAVA_VERSION = Integer.parseInt(version);
            ROOT = Paths.get("").toAbsolutePath();
            BRACHYURA = ROOT.resolve("brachyura");
            DOT_BUILDSCRIPT = ROOT.resolve(".buildscript");
            Files.createDirectories(DOT_BUILDSCRIPT);
            DEPS = DOT_BUILDSCRIPT.resolve("deps");
            Files.createDirectories(DEPS);
            BUILD = DOT_BUILDSCRIPT.resolve("build");
            Files.createDirectories(BUILD);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static final String[] NO_ARGS = new String[0];

    public static String[] compileArgs(int compilerversion, int targetversion) {
        if (compilerversion == targetversion) return NO_ARGS;
        if (compilerversion >= 9 && targetversion >= 7) return new String[] {"--release", String.valueOf(targetversion)}; // Doesn't accept 1.8 etc for some reason
        throw new UnsupportedOperationException("Target Version: " + targetversion + " " + "Compiler Version: " + compilerversion);
    }

    public static void main(String[] args) throws Throwable {
        List<Path> deps = getDeps();
        List<Path> cp = new ArrayList<>(deps.size() + 2);
        Path brachyuraPath = getBrachyura(deps);
        cp.add(brachyuraPath);
        cp.add(BRACHYURA.resolve("src").resolve("main").resolve("resources"));
        cp.addAll(deps);
        System.setProperty("brachyurasrcdir", BRACHYURA.resolve("src").resolve("main").resolve("java").toString());
        URL[] urls = new URL[cp.size()];
        for (int i = 0; i < cp.size(); i++) {
            urls[i] = cp.get(i).toUri().toURL();
        }
        URLClassLoader cl = new URLClassLoader(urls, ClassLoader.getSystemClassLoader().getParent());
        Class<?> c = Class.forName("io.github.coolcrabs.brachyura.project.BrachyuraEntry", true, cl);
        MethodHandles.publicLookup().findStatic(
            c,
            "main",
            MethodType.methodType(void.class, String[].class, Path.class, List.class)
        )
        .invokeExact(args, ROOT, cp);
    }

    public static Path getBrachyura(List<Path> deps) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-512");
        for (Path p : deps) {
            update(md, p.toString());
            update(md, Files.getFileAttributeView(p, BasicFileAttributeView.class).readAttributes().lastModifiedTime().toMillis());
        }
        try (Stream<Path> s = Files.find(BRACHYURA.resolve("src").resolve("main").resolve("java"), Integer.MAX_VALUE, (p, bfa) -> bfa.isRegularFile())) {
            s.sorted().forEach(p -> {
                update(md, p.toString());
                try {
                    update(md, Files.getFileAttributeView(p, BasicFileAttributeView.class).readAttributes().lastModifiedTime().toMillis());
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
        }
        String hash = toHexHash(md.digest());
        Path buildPath = BUILD.resolve(hash);
        if (!Files.exists(buildPath) || Boolean.getBoolean("recompile")) {
            for (File f : BUILD.toFile().listFiles()) deleteDirectory(f);
            System.out.println("Compiling brachyura...");
            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            StandardJavaFileManager fm = compiler.getStandardFileManager(null, null, null);
            ArrayList<File> files = new ArrayList<>();
            try (Stream<Path> s = Files.find(BRACHYURA.resolve("src").resolve("main").resolve("java"), Integer.MAX_VALUE, (p, bfa) -> bfa.isRegularFile())) {
                s.forEach(p -> files.add(p.toFile()));
            }
            ArrayList<String> args = new ArrayList<>(Arrays.asList("-d", buildPath.toString(), "-cp", deps.stream().map(Path::toString).collect(Collectors.joining(File.pathSeparator))));
            Collections.addAll(args, compileArgs(CURRENT_JAVA_VERSION, 8));
            compiler.getTask(
                null,
                null,
                null,
                args,
                null,
                fm.getJavaFileObjects(files.toArray(new File[0]))
            )
            .call();
            System.out.println("Finished compiling");
        } else {
            System.out.println("Brachyura up to date");
        }
        return buildPath;
    }

    // https://www.baeldung.com/java-delete-directory
    public static boolean deleteDirectory(File directoryToBeDeleted) {
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }
        return directoryToBeDeleted.delete();
    }
    
    public static void update(MessageDigest md, String string) {
        md.update(string.getBytes(StandardCharsets.UTF_8));
    }

    public static void update(MessageDigest md, long i) {
        md.update(
            new byte[] {
                (byte)(i >>> 56),
                (byte)(i >>> 48),
                (byte)(i >>> 40),
                (byte)(i >>> 32),
                (byte)(i >>> 24),
                (byte)(i >>> 16),
                (byte)(i >>> 8),
                (byte)i
            }
        );
    }

    static final String HEXES = "0123456789ABCDEF";
    // https://www.rgagnon.com/javadetails/java-0596.html
    public static String toHexHash(byte[] hash) {
        if (hash == null) {
            return null;
        }
        final StringBuilder hex = new StringBuilder(2 * hash.length);
        for (final byte b : hash) {
            hex.append(HEXES.charAt((b & 0xF0) >> 4)).append(HEXES.charAt((b & 0x0F)));
        }
        return hex.toString();
    }

    static final String[] exts = {".jar", "-sources.jar"};
    public static List<Path> getDeps() throws Exception {
        ArrayList<Path> r = new ArrayList<>();
        try (BufferedReader w = Files.newBufferedReader(BRACHYURA.resolve("deps.txt"))) {
            String line;
            while ((line = w.readLine()) != null) {
                String[] a = line.split(" ");
                String baseUrl = a[0];
                String[] mavenId = a[1].split(":");
                for (String ext : exts) {
                    String fileName = mavenId[1] + "-" + mavenId[2] + ext;
                    Path p = DEPS.resolve(fileName);
                    if (!Files.exists(p)) {
                        Path tmpPath = DEPS.resolve(String.valueOf(ThreadLocalRandom.current().nextLong()));
                        String url = baseUrl + mavenId[0].replace('.', '/') + "/" + mavenId[1] + "/" + mavenId[2] + "/" + fileName;
                        try (InputStream in = new URL(url).openStream()) {
                            Files.copy(in, tmpPath);
                        }
                        Files.move(tmpPath, p, StandardCopyOption.ATOMIC_MOVE);
                    }
                    if (ext.equals(".jar")) r.add(p);
                }
            }
        }
        return r;
    }
}
