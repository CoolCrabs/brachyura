package io.github.coolcrabs.brachyura.maven;

import io.github.coolcrabs.brachyura.dependency.JavaJarDependency;
import io.github.coolcrabs.brachyura.util.AtomicFile;
import io.github.coolcrabs.brachyura.util.ByteArrayOutputStreamEx;
import io.github.coolcrabs.brachyura.util.MessageDigestUtil;
import io.github.coolcrabs.brachyura.util.NetUtil;
import io.github.coolcrabs.brachyura.util.PathUtil;
import io.github.coolcrabs.brachyura.util.Util;
import io.github.coolcrabs.brachyura.util.XmlUtil;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;

public class MavenPublishing {
    private MavenPublishing() { }
    
    public static class AuthenticatedMaven {
        final String mavenUrl;
        final String username;
        final String password;
        
        public AuthenticatedMaven(String mavenUrl, @Nullable String username, @Nullable String password) {
            Objects.requireNonNull(mavenUrl, "Unset maven url");
            if ((username == null) != (password == null)) throw new UnsupportedOperationException("Username and password should both be set or not set");
            this.mavenUrl = mavenUrl;
            this.username = username;
            this.password = password;
        }
        
        public static AuthenticatedMaven ofMavenLocal() {
            return new AuthenticatedMaven(Maven.MAVEN_LOCAL, null, null);
        }
        
        public static AuthenticatedMaven ofEnv() {
            return new AuthenticatedMaven(
                System.getenv("BRACHYURA_PUBLISH_MAVEN"),
                System.getenv("BRACHYURA_PUBLISH_USERNAME"),
                System.getenv("BRACHYURA_PUBLISH_PASSWORD")
            );
        }
    }
    
    /**
     * Publishes to a maven with a stub pom
     * @param maven
     * @param dep
     */
    public static void publish(AuthenticatedMaven maven, JavaJarDependency dep) {
        publish(maven, dep, stubPom(dep.mavenId));
    }
    
    /**
     * Publish to a maven
     * @param maven
     * @param dep
     * @param pom
     */
    public static void publish(AuthenticatedMaven maven, JavaJarDependency dep, Supplier<InputStream> pom) {
        Objects.requireNonNull(pom, "null pom");
        Objects.requireNonNull(dep.mavenId, "null mavenId");
        Objects.requireNonNull(dep.jar, "null jar file");
        ArrayList<MavenPublishFile> a = new ArrayList<>(3);
        a.add(new MavenPublishFile(getMavenPath(dep.mavenId, ".pom"), pom));
        a.add(new MavenPublishFile(getMavenPath(dep.mavenId, ".jar"), () -> PathUtil.inputStream(dep.jar)));
        if (dep.sourcesJar != null) {
            a.add(new MavenPublishFile(getMavenPath(dep.mavenId, "-sources.jar"), () -> PathUtil.inputStream(dep.sourcesJar)));
        }
        publish(maven, a);
    }
    
    public static void publish(AuthenticatedMaven maven, List<MavenPublishFile> files) {
        try {
            ArrayList<MavenPublishFile> a = new ArrayList<>(files.size() * 2);
            for (MavenPublishFile f : files) {
                MessageDigest md = MessageDigestUtil.messageDigest(MessageDigestUtil.SHA1);
                try (InputStream is = f.in.get()) {
                    int r;
                    while ((r = is.read()) != -1) {
                       md.update((byte) r);
                    }
                }
                String hash = MessageDigestUtil.toHexHash(md.digest());
                a.add(new MavenPublishFile(f.fileName + ".sha1", () -> new ByteArrayInputStream(hash.getBytes(StandardCharsets.UTF_8))));
            }
            a.addAll(files);
            rawPublish(maven, a);
        } catch (Exception e) {
            throw Util.sneak(e);
        }
    }
    
    public static void rawPublish(AuthenticatedMaven maven, List<MavenPublishFile> files) {
        try {
            URI mavenRepoUri = new URI(Maven.addTrailSlash(maven.mavenUrl));
            if ("file".equals(mavenRepoUri.getScheme())) {
                Path localMaven = Paths.get(mavenRepoUri);
                Logger.info("Publishing to local maven {}", localMaven);
                for (MavenPublishFile f : files) {
                    Logger.info("Publishing {}", f.fileName);
                    try (
                        AtomicFile a = new AtomicFile(localMaven.resolve(f.fileName));
                        InputStream is = f.in.get();
                    ) {
                        Files.copy(is, a.tempPath, StandardCopyOption.REPLACE_EXISTING);
                        a.commit();
                    }
                }
            } else {
                for (MavenPublishFile f : files) {
                    URL url = mavenRepoUri.resolve(f.fileName).toURL();
                    try (InputStream is = f.in.get()) {
                        NetUtil.put(url, is, maven.username, maven.password);
                    }
                }
            }
        } catch (Exception e) {
            throw Util.sneak(e);
        }
    }
    
    static String getMavenPath(MavenId id, String ext) {
        return id.groupId.replace('.', '/') + "/" + id.artifactId + "/" + id.version + "/" + id.artifactId + "-" + id.version + ext;
    }
    
    public static class MavenPublishFile {
        final String fileName;
        final Supplier<InputStream> in;
        
        public MavenPublishFile(String fileName, Supplier<InputStream> in) {
            this.fileName = fileName;
            this.in = in;
        }
    }
    
    public static Supplier<InputStream> stubPom(MavenId id) {
        try {
            ByteArrayOutputStreamEx o = new ByteArrayOutputStreamEx();
            try (XmlUtil.FormattedXMLStreamWriter w = XmlUtil.newStreamWriter(new OutputStreamWriter(o, StandardCharsets.UTF_8))) {
                w.writeStartDocument("UTF-8", "1.0");
                w.newline();
                w.writeStartElement("project");
                w.writeAttribute("xsi:schemaLocation", "http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd");
                w.writeAttribute("xmlns", "http://maven.apache.org/POM/4.0.0");
                w.writeAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
                w.indent();
                w.newline();
                    w.writeStartElement("modelVersion");
                    w.writeCharacters("4.0.0");
                    w.writeEndElement();
                    w.newline();
                    w.writeStartElement("groupId");
                    w.writeCharacters(id.groupId);
                    w.writeEndElement();
                    w.newline();
                    w.writeStartElement("artifactId");
                    w.writeCharacters(id.artifactId);
                    w.writeEndElement();
                    w.newline();
                    w.writeStartElement("version");
                    w.writeCharacters(id.version);
                    w.writeEndElement();
                w.unindent();
                w.newline();
                w.writeEndElement();
                w.newline();
                return o::toIs;
            }
        } catch (Exception e) {
            throw Util.sneak(e);
        }
    }
}
