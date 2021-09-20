package io.github.coolcrabs.brachyura.ide;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.concurrent.ThreadLocalRandom;

import org.benf.cfr.reader.util.annotation.Nullable;
import org.tinylog.Logger;

import io.github.coolcrabs.brachyura.dependency.JavaJarDependency;
import io.github.coolcrabs.brachyura.util.AtomicDirectory;
import io.github.coolcrabs.brachyura.util.PathUtil;
import io.github.coolcrabs.brachyura.util.Util;

public enum Netbeans implements Ide {
    INSTANCE;

    static final Properties defaultProperties = new Properties();

    static {
        try {
            try (InputStreamReader reader = new InputStreamReader(Netbeans.class.getResourceAsStream("/nb/nbdefault.properties"))) {
                defaultProperties.load(reader);
            }
        } catch (Exception e) {
            throw Util.sneak(e);
        }
    }

    @Override
    public String ideName() {
        return "netbeans";
    }

    @Override
    public void updateProject(Path projectDir, IdeProject ideProject) {
        Path nb = PathUtil.resolveAndCreateDir(projectDir, "netbeans");
        PathUtil.deleteDirectoryChildren(nb);
        new NetbeansProject(nb.resolve("NetbeansProject"), ideProject).write();
    }

    static class NetbeansProject {
        final Properties projectProperties = new Properties(defaultProperties);
        final Path dir;

        NetbeansProject(Path dir, IdeProject ideProject) {
            if (ideProject.sourcePaths.size() != 1) throw new UnsupportedOperationException("Netbeans support for >1 source path not impl");
            projectProperties.setProperty("src.dir", ideProject.sourcePaths.get(0).toString());
            StringBuilder javacClasspath = new StringBuilder();
            for (JavaJarDependency j : ideProject.dependencies) {
                if (javacClasspath.length() > 0) javacClasspath.append(File.pathSeparator);
                javacClasspath.append(createFileReference(j).getListString());
            }
            projectProperties.setProperty("javac.classpath", javacClasspath.toString());
            this.dir = dir;
        }

        @SuppressWarnings("all")
        void write() {
            try {
                try (AtomicDirectory d = new AtomicDirectory(dir)) {
                    cp(d.tempPath, "gamerbuild.xml");
                    cp(d.tempPath, "manifest.mf");
                    cp(d.tempPath, "nbproject", "build-impl.xml");
                    cp(d.tempPath, "nbproject", "genfiles.properties");
                    cp(d.tempPath, "nbproject", "project.xml");
                    try (OutputStream o = PathUtil.outputStream(d.tempPath.resolve("nbproject").resolve("project.properties"))) {
                        projectProperties.store(o, null);
                    }
                    d.commit();
                } 
            } catch (Exception e) {
                throw Util.sneak(e);
            }
        }

        void cp(Path dir, String... p) throws IOException {
            Path target = dir;
            for (String p0 : p) {
                target = target.resolve(p0);
            }
            Files.createDirectories(target.getParent());
            try (InputStream is = Netbeans.class.getClassLoader().getResourceAsStream("nb/" + String.join("/", p))) {
                Files.copy(is, target);
            }
        }

        static class SourceSet {
            @Nullable String name;
        }

        static class FileReference {
            String property;
            String getListString() {
                return "${" + property + "}";
            }
        }

        FileReference createFileReference(Path path, @Nullable Path source) {
            FileReference result = new FileReference();
            String fileName = path.getFileName().toString();
            result.property = "file.reference." + fileName;
            if (projectProperties.containsKey(result.property) && !projectProperties.getProperty(result.property).equals(path.toString())) {
                Logger.warn("Duplicate file names for " + fileName);
                fileName = String.valueOf(ThreadLocalRandom.current().nextLong());
                result.property = "file.reference." + fileName;
            }
            projectProperties.setProperty(result.property, path.toString());
            if (source != null) {
                projectProperties.setProperty("source.reference." + fileName, source.toString());
            }
            return result;
        }

        FileReference createFileReference(JavaJarDependency dependency) {
            return createFileReference(dependency.jar, dependency.sourcesJar);
        }
    }

}
