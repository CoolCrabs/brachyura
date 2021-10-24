package io.github.coolcrabs.brachyura.build;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.Comparator;
import java.util.stream.Stream;

import org.kohsuke.github.GHRelease;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHTagObject;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;

public class Main {
    static String[] localLibs = new String[] {"javacompilelib", "fabricmerge", "cfr", "brachyura", "bootstrap"};
    static String[] mavenLibs = new String[] {
        "https://repo.maven.apache.org/maven2/org/ow2/asm/asm/9.2/asm-9.2.jar",
        "https://repo.maven.apache.org/maven2/org/ow2/asm/asm/9.2/asm-9.2-sources.jar",
        "https://repo.maven.apache.org/maven2/org/ow2/asm/asm-analysis/9.2/asm-analysis-9.2.jar",
        "https://repo.maven.apache.org/maven2/org/ow2/asm/asm-analysis/9.2/asm-analysis-9.2-sources.jar",
        "https://repo.maven.apache.org/maven2/org/ow2/asm/asm-commons/9.2/asm-commons-9.2.jar",
        "https://repo.maven.apache.org/maven2/org/ow2/asm/asm-commons/9.2/asm-commons-9.2-sources.jar",
        "https://repo.maven.apache.org/maven2/org/ow2/asm/asm-tree/9.2/asm-tree-9.2.jar",
        "https://repo.maven.apache.org/maven2/org/ow2/asm/asm-tree/9.2/asm-tree-9.2-sources.jar",
        "https://repo.maven.apache.org/maven2/org/ow2/asm/asm-util/9.2/asm-util-9.2.jar",
        "https://repo.maven.apache.org/maven2/org/ow2/asm/asm-util/9.2/asm-util-9.2-sources.jar",
        "https://repo.maven.apache.org/maven2/com/google/code/gson/gson/2.8.7/gson-2.8.7.jar",
        "https://repo.maven.apache.org/maven2/com/google/code/gson/gson/2.8.7/gson-2.8.7-sources.jar",
        "https://repo.maven.apache.org/maven2/org/tinylog/tinylog-api/2.3.2/tinylog-api-2.3.2.jar",
        "https://repo.maven.apache.org/maven2/org/tinylog/tinylog-api/2.3.2/tinylog-api-2.3.2-sources.jar",
        "https://repo.maven.apache.org/maven2/org/tinylog/tinylog-impl/2.3.2/tinylog-impl-2.3.2.jar",
        "https://repo.maven.apache.org/maven2/org/tinylog/tinylog-impl/2.3.2/tinylog-impl-2.3.2-sources.jar",
        "https://maven.fabricmc.net/net/fabricmc/mapping-io/0.1.7/mapping-io-0.1.7.jar",
        "https://maven.fabricmc.net/net/fabricmc/mapping-io/0.1.7/mapping-io-0.1.7-sources.jar",
        "https://maven.fabricmc.net/net/fabricmc/tiny-remapper/0.4.2/tiny-remapper-0.4.2.jar",
        "https://maven.fabricmc.net/net/fabricmc/tiny-remapper/0.4.2/tiny-remapper-0.4.2-sources.jar",
        "https://maven.fabricmc.net/net/fabricmc/access-widener/2.0.0/access-widener-2.0.0.jar",
        "https://maven.fabricmc.net/net/fabricmc/access-widener/2.0.0/access-widener-2.0.0-sources.jar",
        "https://maven.hydos.cf/releases/io/github/coolmineman/trieharder/0.1.2/trieharder-0.1.2.jar",
        "https://maven.hydos.cf/releases/io/github/coolmineman/trieharder/0.1.2/trieharder-0.1.2-sources.jar"
    };
    static boolean github = Boolean.parseBoolean(System.getenv("CI"));
    static String commit = github ? getCommitHash() : null;
    static final String GITHUB_TOKEN = System.getenv("GITHUB_TOKEN");
    static GitHub gitHub2;

    public static void main(String[] args) throws Exception {
        if (github) {
            gitHub2 = new GitHubBuilder().withOAuthToken(GITHUB_TOKEN).build();
        }
        Path cwd = Paths.get("").toAbsolutePath();
        Path outDir = cwd.resolve("out");
        if (Files.isDirectory(outDir)) deleteDirectory(outDir);
        Files.createDirectories(outDir);
        try (BufferedWriter w = Files.newBufferedWriter(outDir.resolve("brachyurabootstrapconf.txt"))) {
            w.write(String.valueOf(0) + "\n");
            for (String lib : localLibs) {
                Path a = cwd.getParent().resolve(lib).resolve("target");
                Stream<Path> b = Files.walk(a, 1);
                Path jar = 
                    b.filter(p -> p.toString().endsWith(".jar") && !p.toString().endsWith("-sources.jar") && !p.toString().endsWith("-javadoc.jar"))
                    .sorted(Comparator.comparingLong(p -> {
                        try {
                            return Files.getLastModifiedTime(p).toMillis();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    })).findFirst()
                    .orElseThrow(() -> new RuntimeException(lib));
                b.close();
                Path sources = jar.getParent().resolve(jar.getFileName().toString().replace(".jar", "-sources.jar"));
                Path targetjar = outDir.resolve(jar.getFileName());
                Path targetSources = outDir.resolve(sources.getFileName());
                if ("bootstrap".equals(lib)) {
                    doLocalDep(jar, targetjar, true);
                    doLocalDep(sources, targetSources, false);
                } else {
                    if (github) {
                        w.write(doGithubDep(jar, targetjar, true));
                        w.write(doGithubDep(sources, targetSources, false));
                    } else {
                        w.write(doLocalDep(jar, targetjar, true));
                        w.write(doLocalDep(sources, targetSources, false));
                    }
                }                
            }
            for (String lib : mavenLibs) {
                boolean isJar = !lib.endsWith("-sources.jar");
                String filename = lib.substring(lib.lastIndexOf('/') + 1);
                HttpURLConnection connection = (HttpURLConnection) new URL(lib + ".sha1").openConnection();
                connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2"); // hydos moment
                String hash;
                try (InputStream is = connection.getInputStream()) {
                    hash = readFullyAsString(is);
                }
                w.write(lib + "\t" + hash + "\t" + filename + "\t" + isJar + "\n");
            }
        }
        if (github) {
            uploadGithub(outDir);
        }
    }

    static void uploadGithub(Path outDir) throws Exception {
        GHRepository repo = gitHub2.getRepository("CoolCrabs/brachyura");
        System.out.println("Creating tag " + commit);
        GHTagObject tag = repo.createTag("v_" + commit, commit, commit, "commit");
        GHRelease release = repo.createRelease(tag.getTag()).commitish(commit).create();
        Files.walkFileTree(outDir, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                release.uploadAsset(file.toFile(), file.toString().endsWith(".jar") ? "application/zip" : "text/strings");
                return FileVisitResult.CONTINUE;
            }
        });
    }

    static String doLocalDep(Path source, Path target, boolean isJar) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        try (InputStream is = new DigestInputStream(new BufferedInputStream(Files.newInputStream(source)), md)) {
            Files.copy(is, target);
        }
        String hash = toHexHash(md.digest());
        return target.toUri().toString() + "\t" + hash + "\t" + target.getFileName().toString() + "\t" + isJar + "\n";
    }

    static String doGithubDep(Path source, Path target, boolean isJar) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        try (InputStream is = new DigestInputStream(new BufferedInputStream(Files.newInputStream(source)), md)) {
            Files.copy(is, target);
        }
        String hash = toHexHash(md.digest());
        return "https://github.com/CoolCrabs/brachyura/releases/download/" + "v_" + commit + "/" + target.getFileName().toString() + "\t" + hash + "\t" + target.getFileName().toString() + "\t" + isJar + "\n";
    }

    static void deleteDirectory(Path dir) throws IOException {
        Files.walkFileTree(dir, new SimpleFileVisitor<Path>(){
            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    static String getCommitHash() {
        try {
            Process process = new ProcessBuilder("git", "rev-parse", "--verify", "HEAD").start();
            String result = readFullyAsString(process.getInputStream()).replaceAll("[^a-zA-Z0-9]","");
            int exit = process.waitFor();
            if (exit != 0) {
                throw new RuntimeException("Git returned " + exit);
            }
            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static String readFullyAsString(InputStream inputStream) throws IOException {
        return readFully(inputStream).toString(StandardCharsets.UTF_8.name());
    }

    // https://stackoverflow.com/a/10505933
    private static ByteArrayOutputStream readFully(InputStream inputStream) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length = 0;
        while ((length = inputStream.read(buffer)) != -1) {
            baos.write(buffer, 0, length);
        }
        return baos;
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
