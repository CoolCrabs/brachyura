package io.github.coolcrabs.brachyura.maven;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.DigestInputStream;
import java.security.MessageDigest;

import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;

import static io.github.coolcrabs.brachyura.util.MessageDigestUtil.*;

import io.github.coolcrabs.brachyura.dependency.Dependency;
import io.github.coolcrabs.brachyura.dependency.FileDependency;
import io.github.coolcrabs.brachyura.dependency.JavaJarDependency;
import io.github.coolcrabs.brachyura.exception.IncorrectHashException;
import io.github.coolcrabs.brachyura.util.MessageDigestUtil;
import io.github.coolcrabs.brachyura.util.PathUtil;
import io.github.coolcrabs.brachyura.util.StreamUtil;
import io.github.coolcrabs.brachyura.util.Util;

public class Maven {
    private Maven() { }

    public static String MAVEN_CENTRAL = "https://repo.maven.apache.org/maven2/";

    public static JavaJarDependency getMavenJarDep(String mavenRepo, MavenId dep) {
        return (JavaJarDependency) getMavenDep(mavenRepo, dep, ".jar", true, true);
    }

    public static FileDependency getMavenFileDep(String mavenRepo, MavenId dep, String extension) {
        return getMavenFileDep(mavenRepo, dep, extension, true);
    }

    public static @Nullable FileDependency getMavenFileDep(String mavenRepo, MavenId dep, String extension, boolean allowDownload) {
        return (FileDependency) getMavenDep(mavenRepo, dep, extension, false, allowDownload);
    }

    private static Dependency getMavenDep(String mavenRepo, MavenId dep, String extension, boolean isJavaJar, boolean allowDownload) {
        try {
            URI mavenRepoUri = new URI(addTrailSlash(mavenRepo));
            String mavenRepoHash = toHexHash(messageDigest(SHA256).digest((mavenRepoUri.getHost() + mavenRepoUri.getPath()).getBytes(StandardCharsets.UTF_8)));
            Path repoPath = mavenCache().resolve(mavenRepoHash);
            String relativeDownload = "./" + dep.groupId.replace('.', '/') + "/" + dep.artifactId + "/" + dep.version + "/" + dep.artifactId + "-" + dep.version + extension;
            Path downloadPath = repoPath.resolve(relativeDownload);
            if (!Files.isRegularFile(downloadPath)) {
                if (allowDownload) {
                    download(downloadPath, relativeDownload, mavenRepoUri);
                } else {
                    return null;
                }
            }
            if (isJavaJar) {
                String nosourcesRelative = "./" + dep.groupId.replace('.', '/') + "/" + dep.artifactId + "/" + dep.version + "/" + dep.artifactId + "-" + dep.version + ".nosources";
                Path nosources = repoPath.resolve(nosourcesRelative);
                boolean sources = false;
                Path sourcesPath = null;
                if (!Files.isRegularFile(nosources)) {
                    String sourcesRelativeDownload = "./" + dep.groupId.replace('.', '/') + "/" + dep.artifactId + "/" + dep.version + "/" + dep.artifactId + "-" + dep.version + "-sources.jar";
                    sourcesPath = repoPath.resolve(sourcesRelativeDownload);
                    if (Files.isRegularFile(sourcesPath)) {
                        sources = true;
                    } else {
                        if (!allowDownload) return null;
                        try {
                            download(sourcesPath, sourcesRelativeDownload, mavenRepoUri);
                            sources = true;
                        } catch (FileNotFoundException e) {
                            Logger.info("No sources found for " + dep.toString());
                            Files.createFile(nosources);
                        }
                    }
                }
                return new JavaJarDependency(downloadPath, sources ? sourcesPath : null, dep);
            } else {
                return new FileDependency(downloadPath);
            }
        } catch (Exception e) {
            throw Util.sneak(e);
        }
    }

    private static void download(Path path, String relativeDownload, URI mavenRepoUri) throws IOException {
        Path tempPath = PathUtil.tempFile(path);
        try {
            URI targetUri = mavenRepoUri.resolve(relativeDownload);
            String targetHash;
            try (InputStream hashStream = mavenRepoUri.resolve(relativeDownload + ".sha1").toURL().openStream()) {
                targetHash = StreamUtil.readFullyAsString(hashStream);
            }
            MessageDigest messageDigest = MessageDigestUtil.messageDigest(MessageDigestUtil.SHA1);
            try (DigestInputStream inputStream = new DigestInputStream(targetUri.toURL().openStream(), messageDigest)) {
                Files.copy(inputStream, tempPath, StandardCopyOption.REPLACE_EXISTING);
            }
            String hash = MessageDigestUtil.toHexHash(messageDigest.digest());
            if (!hash.equalsIgnoreCase(targetHash)) {
                throw new IncorrectHashException(targetHash, hash);
            }
        } catch (Exception e) {
            Files.delete(tempPath);
            throw e;
        }
        PathUtil.moveAtoB(tempPath, path);
    }

    private static String addTrailSlash(String string) {
        return string.endsWith("/") ? string : string + "/";
    }

    private static Path mavenCache() {
        return PathUtil.cachePath().resolve("maven");
    }
}
