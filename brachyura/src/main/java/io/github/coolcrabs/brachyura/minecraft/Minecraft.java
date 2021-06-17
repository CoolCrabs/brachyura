package io.github.coolcrabs.brachyura.minecraft;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

import io.github.coolcrabs.brachyura.exception.IncorrectHashException;
import io.github.coolcrabs.brachyura.minecraft.LauncherMeta.Version;
import io.github.coolcrabs.brachyura.minecraft.VersionMeta.Dependency;
import io.github.coolcrabs.brachyura.minecraft.VersionMeta.DependencyDownload;
import io.github.coolcrabs.brachyura.minecraft.VersionMeta.Download;
import io.github.coolcrabs.brachyura.util.MessageDigestUtil;
import io.github.coolcrabs.brachyura.util.NetUtil;
import io.github.coolcrabs.brachyura.util.PathUtil;
import io.github.coolcrabs.brachyura.util.Util;

public class Minecraft {
    private Minecraft() { }

    public static VersionMeta getVersion(String version) {
        try {
            Path versionJsonPath = mcCache().resolve(version).resolve("version.json");
            if (!Files.isRegularFile(versionJsonPath)) {
                for (Version metaVersion : LauncherMetaDownloader.getLauncherMeta().versions) {
                    if (metaVersion.id.equals(version)) {
                        Path tempPath = PathUtil.tempFile(versionJsonPath);
                        try (InputStream inputStream = NetUtil.inputStream(NetUtil.url(metaVersion.url))) {
                            Files.copy(inputStream, tempPath, StandardCopyOption.REPLACE_EXISTING);
                        }
                        PathUtil.moveAtoB(tempPath, versionJsonPath);
                        break;
                    }
                }
            }
            return new VersionMeta(PathUtil.inputStream(versionJsonPath));
        } catch (Exception e) {
            throw Util.sneak(e);
        }
    }

    public static Path getDownload(String version, VersionMeta meta, String download) {
        try {
            Path downloadPath = mcCache().resolve(version).resolve(download);
            if (!Files.isRegularFile(downloadPath)) {
                Download downloadDownload = meta.getDownload(download);
                Path tempPath = PathUtil.tempFile(downloadPath);
                MessageDigest messageDigest = MessageDigestUtil.messageDigest(MessageDigestUtil.SHA1);
                try (DigestInputStream inputStream = new DigestInputStream(NetUtil.inputStream(NetUtil.url(downloadDownload.url)), messageDigest)) {
                    Files.copy(inputStream, tempPath, StandardCopyOption.REPLACE_EXISTING);
                }
                String hash = MessageDigestUtil.toHexHash(messageDigest.digest());
                if (!hash.equalsIgnoreCase(downloadDownload.sha1)) {
                    Files.delete(tempPath);
                    throw new IncorrectHashException(downloadDownload.sha1, hash);
                }
                PathUtil.moveAtoB(tempPath, downloadPath);
            }
            return downloadPath;
        } catch (Exception e) {
            throw Util.sneak(e);
        }
    }

    //TODO Get sources jars not explicitly listed in a way that won't make unneeded network requests
    public static List<Path> getDependencies(VersionMeta meta) {
        try {
            List<Dependency> dependencyDownloads = meta.getDependencies();
            ArrayList<Path> result = new ArrayList<>();
            for (Dependency dependency : dependencyDownloads) {
                for (DependencyDownload download : dependency.downloads) {
                    Path downloadPath = mcLibCache().resolve(download.path);
                    if (!Files.isRegularFile(downloadPath)) {
                        Path tempPath = PathUtil.tempFile(downloadPath);
                        MessageDigest messageDigest = MessageDigestUtil.messageDigest(MessageDigestUtil.SHA1);
                        try (DigestInputStream inputStream = new DigestInputStream(NetUtil.inputStream(NetUtil.url(download.url)), messageDigest)) {
                            Files.copy(inputStream, tempPath, StandardCopyOption.REPLACE_EXISTING);
                        }
                        String hash = MessageDigestUtil.toHexHash(messageDigest.digest());
                        if (!hash.equalsIgnoreCase(download.sha1)) {
                            Files.delete(tempPath);
                            throw new IncorrectHashException(download.sha1, hash);
                        }
                        PathUtil.moveAtoB(tempPath, downloadPath);
                        result.add(downloadPath);
                    }
                }
            }
            return result;
        } catch (Exception e) {
            throw Util.sneak(e);
        }
    }

    public static Path mcLibCache() {
        return mcCache().resolve("libraries");
    }

    public static Path mcCache() {
        return PathUtil.cachePath().resolve("minecraft");
    }
}
