package io.github.coolcrabs.brachyura.bootstrap;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("all") // Sue me
public class Main {
    public static final int VERSION = 0;
    static final Path BOOTSTRAP_DIR = Paths.get(System.getProperty("user.home")).resolve(".brachyura").resolve("bootstrap");

    public static void main(String[] args) throws Throwable {
        System.out.println("Using brachyura bootstrap " + VERSION);
        // https://stackoverflow.com/a/2837287
        Path projectPath = Paths.get(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParent();
        Files.createDirectories(BOOTSTRAP_DIR);
        Path conf = projectPath.resolve("brachyurabootstrapconf.txt");
        List<Path> classpath = new ArrayList<>();
        BufferedReader confReader = null;
        try {
            if (Files.isRegularFile(conf)) {
                confReader = Files.newBufferedReader(conf);
            } else {
                InputStream confis = Main.class.getResourceAsStream("/brachyurabootstrapconf.txt");
                if (confis == null) {
                    throw new RuntimeException("Unable to find brachyurabootstrapconf.txt");
                }
                confReader = new BufferedReader(new InputStreamReader(confis));
            }
            int confVersion = Integer.parseInt(confReader.readLine());
            if (confVersion != VERSION) {
                throw new RuntimeException("Unsupported config version " + confVersion + ". Supported version is " + VERSION + " you need to update or downgrade bootstrap jar to use this brachyura version.");
            }
            String line = null;
            while ((line = confReader.readLine()) != null) {
                String[] a = line.split("\t");
                URL url = new URL(a[0]);
                String hash = a[1];
                String fileName = a[2];
                boolean isjar = Boolean.parseBoolean(a[3]);
                Path download = getDownload(url, hash, fileName);
                if (isjar) classpath.add(download);
            }
        } finally {
            if (confReader != null) {
                confReader.close();
            }
        }
        URL[] urls = new URL[classpath.size()];
        for (int i = 0; i < classpath.size(); i++) {
            urls[i] = classpath.get(i).toUri().toURL();
        }
        // https://kostenko.org/blog/2019/06/runtime-class-loading.html
        URLClassLoader classLoader = new URLClassLoader(urls, ClassLoader.getSystemClassLoader());
        Thread.currentThread().setContextClassLoader(classLoader);
        Class entry = Class.forName("io.github.coolcrabs.brachyura.project.BrachyuraEntry", true, classLoader);
        MethodHandles.publicLookup().findStatic(
            entry,
            "main",
            MethodType.methodType(void.class, String[].class, Path.class, List.class)
        )
        .invokeExact(args, projectPath, classpath);
    }

    static Path getDownload(URL url, String hash, String fileName) throws Exception {
        if ("file".equals(url.getProtocol())) return Paths.get(url.toURI()); // For debug usage
        Path target = BOOTSTRAP_DIR.resolve(fileName);
        if (!Files.isRegularFile(target)) {
            Path tempFile = Files.createTempFile(BOOTSTRAP_DIR, hash, ".tmp");
            try {
                MessageDigest md = MessageDigest.getInstance("SHA-1");
                try (InputStream is = new DigestInputStream(url.openStream(), md)) {
                    Files.copy(is, tempFile, StandardCopyOption.REPLACE_EXISTING);
                }
                String actualHash = toHexHash(md.digest());
                if (hash.equalsIgnoreCase(actualHash)) {
                    Files.move(tempFile, target, StandardCopyOption.ATOMIC_MOVE);
                } else {
                    throw new RuntimeException("Incorrect hash expected " + hash + " got " + actualHash);
                }
            } finally {
                Files.deleteIfExists(tempFile);
            }
        }

        return target;
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
}
