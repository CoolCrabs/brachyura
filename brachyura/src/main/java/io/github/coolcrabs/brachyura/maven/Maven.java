package io.github.coolcrabs.brachyura.maven;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

import org.tinylog.Logger;

import static io.github.coolcrabs.brachyura.util.MessageDigestUtil.*;

import io.github.coolcrabs.brachyura.exception.IncorrectHashException;
import io.github.coolcrabs.brachyura.util.MessageDigestUtil;
import io.github.coolcrabs.brachyura.util.PathUtil;
import io.github.coolcrabs.brachyura.util.StreamUtil;
import io.github.coolcrabs.brachyura.util.Util;

public class Maven {
    private Maven() { }

    public static List<Path> getMavenDep(String mavenRepo, MavenId dep) {
        return getMavenDep(mavenRepo, dep, ".jar");
    }

    public static List<Path> getMavenDep(String mavenRepo, MavenId dep, String... extensions) {
        List<Path> result = new ArrayList<>();
        try {
            URI mavenRepoUri = new URI(addTrailSlash(mavenRepo));
            String mavenRepoHash = toHexHash(messageDigest(SHA256).digest((mavenRepoUri.getHost() + mavenRepoUri.getPath()).getBytes(StandardCharsets.UTF_8)));
            Path repoPath = mavenCache().resolve(mavenRepoHash);
            for (String extension : extensions) {
                String relativeDownload = "./" + dep.groupId.replace('.', '/') + "/" + dep.artifactId + "/" + dep.version + "/" + dep.artifactId + "-" + dep.version + extension;
                Path downloadPath = repoPath.resolve(relativeDownload);
                if (!Files.isRegularFile(downloadPath)) {
                    download(downloadPath, relativeDownload, mavenRepoUri);
                }
                if (extension.equals(".jar")) {
                    String nosourcesRelative = "./" + dep.groupId.replace('.', '/') + "/" + dep.artifactId + "/" + dep.version + "/" + dep.artifactId + "-" + dep.version + ".nosources";
                    Path nosources = repoPath.resolve(nosourcesRelative);
                    if (!Files.isRegularFile(nosources)) {
                        String sourcesRelativeDownload = "./" + dep.groupId.replace('.', '/') + "/" + dep.artifactId + "/" + dep.version + "/" + dep.artifactId + "-" + dep.version + "-sources.jar";
                        Path sourcesPath = repoPath.resolve(sourcesRelativeDownload);
                        if (!Files.isRegularFile(sourcesPath)) {
                            try {
                                download(sourcesPath, sourcesRelativeDownload, mavenRepoUri);
                            } catch (FileNotFoundException e) {
                                Logger.info("No sources found for " + dep.toString());
                                Files.createFile(nosources);
                            }
                        }
                    }
                }
                
                result.add(downloadPath);
            }
        } catch (Exception e) {
            throw Util.sneak(e);
        }
        return result;
    }

    private static void download(Path path, String relativeDownload, URI mavenRepoUri) throws Exception {
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
