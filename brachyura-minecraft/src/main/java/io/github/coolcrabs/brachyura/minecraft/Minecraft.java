package io.github.coolcrabs.brachyura.minecraft;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import com.google.gson.Gson;

import org.tinylog.Logger;

import io.github.coolcrabs.brachyura.dependency.Dependency;
import io.github.coolcrabs.brachyura.dependency.JavaJarDependency;
import io.github.coolcrabs.brachyura.dependency.NativesJarDependency;
import io.github.coolcrabs.brachyura.exception.IncorrectHashException;
import io.github.coolcrabs.brachyura.mappings.Namespaces;
import io.github.coolcrabs.brachyura.maven.Maven;
import io.github.coolcrabs.brachyura.maven.MavenId;
import io.github.coolcrabs.brachyura.minecraft.LauncherMeta.Version;
import io.github.coolcrabs.brachyura.minecraft.Minecraft.AssetsIndex.SizeHash;
import io.github.coolcrabs.brachyura.minecraft.VersionMeta.VMAssets;
import io.github.coolcrabs.brachyura.minecraft.VersionMeta.VMDependency;
import io.github.coolcrabs.brachyura.minecraft.VersionMeta.VMDownload;
import io.github.coolcrabs.brachyura.util.ArchUtil;
import io.github.coolcrabs.brachyura.util.AtomicFile;
import io.github.coolcrabs.brachyura.util.FileSystemUtil;
import io.github.coolcrabs.brachyura.util.MessageDigestUtil;
import io.github.coolcrabs.brachyura.util.NetUtil;
import io.github.coolcrabs.brachyura.util.OsUtil;
import io.github.coolcrabs.brachyura.util.PathUtil;
import io.github.coolcrabs.brachyura.util.StreamUtil;
import io.github.coolcrabs.brachyura.util.Util;
import io.github.coolcrabs.brachyura.util.ArchUtil.Arch;
import io.github.coolcrabs.brachyura.util.OsUtil.Os;
import net.fabricmc.mappingio.MappingVisitor;
import net.fabricmc.mappingio.adapter.MappingSourceNsSwitch;
import net.fabricmc.mappingio.format.ProGuardReader;
import net.fabricmc.mappingio.tree.MappingTree;
import net.fabricmc.mappingio.tree.MemoryMappingTree;

public class Minecraft {
    private Minecraft() { }

    public static VersionMeta getVersion(String version) {
        try {
            Path versionJsonPath = mcVersions().resolve(version).resolve("version.json");
            if (!Files.isRegularFile(versionJsonPath)) {
                for (Version metaVersion : LauncherMetaDownloader.getLauncherMeta().versions) {
                    if (metaVersion.id.equals(version)) {
                        Path tempPath = PathUtil.tempFile(versionJsonPath);
                        try {
                            try (InputStream inputStream = NetUtil.inputStream(NetUtil.url(metaVersion.url))) {
                                Files.copy(inputStream, tempPath, StandardCopyOption.REPLACE_EXISTING);
                            }
                        } catch (Exception e) {
                            Files.delete(tempPath);
                            throw e;
                        }
                        PathUtil.moveAtoB(tempPath, versionJsonPath);
                        break;
                    }
                }
            }
            try (BufferedReader reader = Files.newBufferedReader(versionJsonPath)) {
                return new VersionMeta(reader);
            }
        } catch (Exception e) {
            throw Util.sneak(e);
        }
    }

    /**
     * Used for getting an experimental snapshot
     * @param zipUrl eg: https://launcher.mojang.com/experiments/combat/52263d42a626b40c947e523128f7a195ec5af76a/1_15_combat-6.zip or
     * https://launcher.mojang.com/v1/objects/b1e589c1d6ed73519797214bc796e53f5429ac46/1_19_deep_dark_experimental_snapshot-1.zip
     */
    public static VersionMeta getExperimentalVersion(String zipUrl) {
        try {
            MessageDigest md = MessageDigestUtil.messageDigest(MessageDigestUtil.SHA256);
            MessageDigestUtil.update(md, zipUrl);
            String urlHash = MessageDigestUtil.toHexHash(md.digest());
            Path zip = PathUtil.resolveAndCreateDir(mcExp(), urlHash).resolve(zipUrl.substring(zipUrl.lastIndexOf('/') + 1));
            if (!Files.isRegularFile(zip)) {
                try (
                    AtomicFile a = new AtomicFile(zip);
                    InputStream is = NetUtil.inputStream(new URL(zipUrl))
                ) {
                    Files.copy(is, a.tempPath, StandardCopyOption.REPLACE_EXISTING);
                    a.commit();
                }
            }
            try (
                FileSystem fs = FileSystemUtil.newJarFileSystem(zip);
                Stream<Path> s = Files.walk(fs.getPath("/"));
            ) {
                for (Iterator<Path> it = s.iterator(); it.hasNext();) {
                    Path p = it.next();
                    if (p.toString().endsWith(".json")) {
                        try (BufferedReader reader = Files.newBufferedReader(p)) {
                            return new VersionMeta(reader);
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw Util.sneak(e);
        }
        throw new IllegalArgumentException("Unable to find experimental version json in requested url");
    }

    public static Path getDownload(VersionMeta meta, String download) {
        try {
            Path downloadPath = mcVersions().resolve(meta.version).resolve(download);
            if (!Files.isRegularFile(downloadPath)) {
                VMDownload downloadDownload = meta.getDownload(download);
                Path tempPath = PathUtil.tempFile(downloadPath);
                try {
                    MessageDigest messageDigest = MessageDigestUtil.messageDigest(MessageDigestUtil.SHA1);
                    try (DigestInputStream inputStream = new DigestInputStream(NetUtil.inputStream(NetUtil.url(downloadDownload.url)), messageDigest)) {
                        Files.copy(inputStream, tempPath, StandardCopyOption.REPLACE_EXISTING);
                    }
                    String hash = MessageDigestUtil.toHexHash(messageDigest.digest());
                    if (!hash.equalsIgnoreCase(downloadDownload.sha1)) {
                        throw new IncorrectHashException(downloadDownload.sha1, hash);
                    }
                } catch (Exception e) {
                    Files.delete(tempPath);
                    throw e;
                }
                PathUtil.moveAtoB(tempPath, downloadPath);
            }
            return downloadPath;
        } catch (Exception e) {
            throw Util.sneak(e);
        }
    }

    /**
     * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
     * !!!DON'T USE THIS IF YOU DON'T KNOW EXACTLY WHAT YOU ARE DOING!!!
     * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
     * 
     * From obf to named. You likely want to merge this with intermediary
     */
    public static MappingTree getMojmap(VersionMeta meta) {
        try {
            MemoryMappingTree r = new MemoryMappingTree(true);
            try (
                Reader a = Files.newBufferedReader(getDownload(meta, "client_mappings"));
                Reader b = Files.newBufferedReader(getDownload(meta, "server_mappings"));
            ) {
                MappingVisitor v = new MappingSourceNsSwitch(r, Namespaces.OBF);
                ProGuardReader.read(a, Namespaces.NAMED, Namespaces.OBF, v);
                ProGuardReader.read(b, Namespaces.NAMED, Namespaces.OBF, v);
            }
            return r;
        } catch (Exception e) {
            throw Util.sneak(e);
        }
    }

    public static List<Dependency> getDependencies(VersionMeta meta) {
        try {
            List<VMDependency> dependencyDownloads = meta.getDependencies();
            ArrayList<Dependency> result = new ArrayList<>();
            for (VMDependency dependency : dependencyDownloads) {
                MavenId mavenId = new MavenId(dependency.name);
                // Work around for strange issue with Windows AMD drivers
                // Not sure what is going on there
                if (ArchUtil.ARCH == Arch.X86_64 && OsUtil.OS == Os.WINDOWS && "natives-windows-x86".equals(mavenId.classifier)) {
                    continue;
                }
                Path artifactPath = null;
                Path nativesPath = null;
                Path sourcesPath = null;
                if (dependency.artifact != null) {
                    artifactPath = mcLibCache().resolve(dependency.artifact.path);
                    if (!Files.isRegularFile(artifactPath)) {
                        downloadDep(artifactPath, new URL(dependency.artifact.url), dependency.artifact.sha1);
                    }
                    Path noSourcesPath = mcLibCache().resolve(dependency.artifact.path + ".nosources");
                    if (!Files.isRegularFile(noSourcesPath)) {
                        Path sourcesPath2 = mcLibCache().resolve(dependency.artifact.path.replace(".jar", "-sources.jar"));
                        if (Files.isRegularFile(sourcesPath2)) {
                            sourcesPath = sourcesPath2;
                        } else {
                            String sourcesUrl = dependency.artifact.url.replace(".jar", "-sources.jar");
                            URL sourcesHashUrl = new URL(sourcesUrl + ".sha1");
                            String targetHash;
                            try {
                                try (InputStream hashStream = NetUtil.inputStream(sourcesHashUrl)) {
                                    targetHash = StreamUtil.readFullyAsString(hashStream);
                                }
                                // If we got this far sources exist
                                sourcesPath = sourcesPath2;
                                downloadDep(sourcesPath, new URL(sourcesUrl), targetHash);
                            } catch (FileNotFoundException e) {
                                try {
                                    sourcesUrl = sourcesUrl.replace("https://libraries.minecraft.net/", Maven.MAVEN_CENTRAL); // WHY ???
                                    sourcesHashUrl = new URL(sourcesUrl + ".sha1");
                                    try (InputStream hashStream = NetUtil.inputStream(sourcesHashUrl)) {
                                        targetHash = StreamUtil.readFullyAsString(hashStream);
                                    }
                                    // If we got this far sources exist
                                    sourcesPath = sourcesPath2;
                                    downloadDep(sourcesPath, new URL(sourcesUrl), targetHash);
                                } catch (FileNotFoundException e2) {
                                    Logger.info("No sources found for " + dependency.name + " (" + dependency.artifact.url + ")");
                                    Files.createFile(noSourcesPath);
                                }
                            }
                        }
                    }
                }
                if (dependency.natives != null) {
                    nativesPath = mcLibCache().resolve(dependency.natives.path);
                    if (!Files.isRegularFile(nativesPath)) {
                        downloadDep(nativesPath, new URL(dependency.natives.url), dependency.natives.sha1);
                    }
                }
                if (artifactPath != null) {
                    result.add(new JavaJarDependency(artifactPath, sourcesPath, mavenId));
                }
                if (nativesPath != null) {
                    result.add(new NativesJarDependency(nativesPath));
                }
            }
            return result;
        } catch (Exception e) {
            throw Util.sneak(e);
        }
    }

    public static String downloadAssets(VersionMeta vm) {
        try {
            VMAssets vmAssets = vm.getVmAssets();
            Path assetsIndex = assets().resolve("indexes").resolve(vmAssets.id + ".json");
            if (!Files.isRegularFile(assetsIndex)) {
                try (AtomicFile atomicFile = new AtomicFile(assetsIndex)) {
                    Files.deleteIfExists(atomicFile.tempPath);
                    MessageDigest messageDigest = MessageDigestUtil.messageDigest(MessageDigestUtil.SHA1);
                    try (DigestInputStream inputStream = new DigestInputStream(NetUtil.inputStream(NetUtil.url(vmAssets.url)), messageDigest)) {
                        Files.copy(inputStream, atomicFile.tempPath);
                    }
                    String hash = MessageDigestUtil.toHexHash(messageDigest.digest());
                    if (!hash.equalsIgnoreCase(vmAssets.sha1)) {
                        throw new IncorrectHashException(vmAssets.sha1, hash);
                    }
                    atomicFile.commit();
                }
            }
            downloadAssets0(assetsIndex);
            return vmAssets.id;
        } catch (Exception e) {
            throw Util.sneak(e);
        }
    }

    private static void downloadAssets0(Path assetsIndexPath) throws IOException {
        AssetsIndex assetsIndex = null;
        try (BufferedReader reader = Files.newBufferedReader(assetsIndexPath)) {
            assetsIndex = new Gson().fromJson(reader, AssetsIndex.class);
        }
        Path objects = assets().resolve("objects");
        for (Map.Entry<String, SizeHash> entry : assetsIndex.objects.entrySet()) {
            String a = entry.getValue().hash.substring(0, 2); // first 2 chars
            URL url = NetUtil.url("https://resources.download.minecraft.net/" + a + "/" + entry.getValue().hash);
            Path target = objects.resolve(a).resolve(entry.getValue().hash);
            if (!Files.isRegularFile(target)) {
                try (AtomicFile atomicFile = new AtomicFile(target)) {
                    Files.deleteIfExists(atomicFile.tempPath);
                    MessageDigest messageDigest = MessageDigestUtil.messageDigest(MessageDigestUtil.SHA1);
                    try (DigestInputStream inputStream = new DigestInputStream(NetUtil.inputStream(url), messageDigest)) {
                        Files.copy(inputStream, atomicFile.tempPath);
                    }
                    String hash = MessageDigestUtil.toHexHash(messageDigest.digest());
                    if (!hash.equalsIgnoreCase(entry.getValue().hash)) {
                        throw new IncorrectHashException(entry.getValue().hash, hash);
                    }
                    atomicFile.commit();
                }
            }
        }
    }

    static class AssetsIndex {
        Map<String, SizeHash> objects;

        static class SizeHash {
            String hash;
            int size;
        }
    }

    private static void downloadDep(Path downloadPath, URL url, String sha1) throws IOException {
        Path tempPath = PathUtil.tempFile(downloadPath);
        try {
            MessageDigest messageDigest = MessageDigestUtil.messageDigest(MessageDigestUtil.SHA1);
            try (DigestInputStream inputStream = new DigestInputStream(NetUtil.inputStream(url), messageDigest)) {
                Files.copy(inputStream, tempPath, StandardCopyOption.REPLACE_EXISTING);
            }
            String hash = MessageDigestUtil.toHexHash(messageDigest.digest());
            if (!hash.equalsIgnoreCase(sha1)) {
                throw new IncorrectHashException(sha1, hash);
            }
        } catch (Exception e) {
            Files.delete(tempPath);
            throw e;
        }
        PathUtil.moveAtoB(tempPath, downloadPath);
    }

    public static Path mcLibCache() {
        return PathUtil.resolveAndCreateDir(mcCache(), "libraries");
    }

    public static Path mcVersions() {
        return PathUtil.resolveAndCreateDir(mcCache(), "versions");
    }

    public static Path assets() {
        return PathUtil.resolveAndCreateDir(mcCache(), "assets");
    }

    public static Path mcExp() {
        return PathUtil.resolveAndCreateDir(mcCache(), "experimental");
    }

    public static Path mcCache() {
        return PathUtil.resolveAndCreateDir(PathUtil.cachePath(), "minecraft");
    }
}
