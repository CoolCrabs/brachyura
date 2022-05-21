package io.github.coolcrabs.brachyura.maven;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;

import static io.github.coolcrabs.brachyura.util.MessageDigestUtil.*;

import io.github.coolcrabs.brachyura.dependency.Dependency;
import io.github.coolcrabs.brachyura.dependency.FileDependency;
import io.github.coolcrabs.brachyura.dependency.JavaJarDependency;
import io.github.coolcrabs.brachyura.util.AtomicFile;
import io.github.coolcrabs.brachyura.util.NetUtil;
import io.github.coolcrabs.brachyura.util.PathUtil;
import io.github.coolcrabs.brachyura.util.Util;

public class Maven {
    private Maven() { }

    public static final String MAVEN_CENTRAL = "https://repo.maven.apache.org/maven2/";
    public static final String MAVEN_LOCAL = PathUtil.HOME.resolve(".m2").resolve("repository").toUri().toString(); // This is wrong, too bad https://stackoverflow.com/a/47833316

    public static JavaJarDependency getMavenJarDep(String mavenRepo, MavenId dep) {
        return (JavaJarDependency) getMavenDep(mavenRepo, dep, ".jar", true, true);
    }

    public static FileDependency getMavenFileDep(String mavenRepo, MavenId dep, String extension) {
        return getMavenFileDep(mavenRepo, dep, extension, true);
    }

    @Deprecated
    public static @Nullable FileDependency getMavenFileDep(String mavenRepo, MavenId dep, String extension, boolean allowDownload) {
        return (FileDependency) getMavenDep(mavenRepo, dep, extension, false, allowDownload);
    }

    private static Dependency getMavenDep(String mavenRepo, MavenId dep, String extension, boolean isJavaJar, boolean allowDownload) {
        try {
            URI mavenRepoUri = new URI(addTrailSlash(mavenRepo));
            boolean local = "file".equals(mavenRepoUri.getScheme());
            String mavenRepoHash = toHexHash(messageDigest(SHA256).digest((mavenRepoUri.getHost() + mavenRepoUri.getPath()).getBytes(StandardCharsets.UTF_8)));
            Path repoPath = local ? Paths.get(mavenRepoUri) : mavenCache().resolve(mavenRepoHash);
            String relativeRoot = "./" + dep.groupId.replace('.', '/') + "/" + dep.artifactId + "/" + dep.version + "/" + dep.artifactId + "-" + dep.version;
            if (dep.classifier != null) relativeRoot += "-" + dep.classifier; 
            String relativeDownload = relativeRoot + extension;
            Path downloadPath = repoPath.resolve(relativeDownload);
            if (!Files.isRegularFile(downloadPath)) {
                if (allowDownload) {
                    if (local) throw new FileNotFoundException(downloadPath.toString());
                    download(downloadPath, relativeDownload, mavenRepoUri);
                } else {
                    return null;
                }
            }
            if (isJavaJar) {
                String nosourcesRelative = relativeRoot + ".nosources";
                Path nosources = repoPath.resolve(nosourcesRelative);
                boolean sources = false;
                Path sourcesPath = null;
                if (!Files.isRegularFile(nosources)) {
                    String sourcesRelativeDownload = relativeRoot + "-sources.jar";
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
                            if (!local) Files.createFile(nosources);
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
        try (AtomicFile f = new AtomicFile(path)) {
            URI targetUri = mavenRepoUri.resolve(relativeDownload);
            try (InputStream inputStream = NetUtil.inputStream(targetUri.toURL())) {
                Files.copy(inputStream, f.tempPath, StandardCopyOption.REPLACE_EXISTING);
            }
            f.commit();
        }
    }

    static String addTrailSlash(String string) {
        return string.endsWith("/") ? string : string + "/";
    }

    private static Path mavenCache() {
        return PathUtil.cachePath().resolve("maven");
    }
}
