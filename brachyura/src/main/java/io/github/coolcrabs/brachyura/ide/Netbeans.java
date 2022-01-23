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
import io.github.coolcrabs.brachyura.util.JvmUtil;
import io.github.coolcrabs.brachyura.util.PathUtil;
import io.github.coolcrabs.brachyura.util.Util;
import io.github.coolcrabs.brachyura.util.XmlUtil;
import java.util.ArrayList;

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
        new NetbeansProject(nb.resolve(ideProject.name), ideProject).write();
    }

    static class NetbeansProject {
        final Properties projectProperties = new Properties() {{
            putAll(defaultProperties);
        }};
        
        final Path dir;
        final IdeProject ideProject;

        NetbeansProject(Path dir, IdeProject ideProject) {
            if (ideProject.sourcePaths.size() != 1) throw new UnsupportedOperationException("Netbeans support for >1 source path not impl");
            projectProperties.setProperty("application.title", ideProject.name);
            projectProperties.setProperty("src.dir", ideProject.sourcePaths.values().iterator().next().toString());
            StringBuilder javacClasspath = new StringBuilder();
            for (JavaJarDependency j : ideProject.dependencies.get()) {
                if (javacClasspath.length() > 0) javacClasspath.append(File.pathSeparator);
                javacClasspath.append(createFileReference(j).getListString());
            }
            projectProperties.setProperty("javac.classpath", javacClasspath.toString());
            projectProperties.setProperty("javac.source", JvmUtil.javaVersionString(ideProject.javaVersion));
            projectProperties.setProperty("javac.target", JvmUtil.javaVersionString(ideProject.javaVersion));
            this.dir = dir;
            this.ideProject = ideProject;
        }

        @SuppressWarnings("all")
        void write() {
            try {
                try (AtomicDirectory d = new AtomicDirectory(dir)) {
                    cp(d.tempPath, "gamerbuild.xml");
                    cp(d.tempPath, "manifest.mf");
                    cp(d.tempPath, "nbproject", "build-impl.xml");
                    cp(d.tempPath, "nbproject", "genfiles.properties");
                    writeProjectXml(d.tempPath);
                    try (OutputStream o = PathUtil.outputStream(d.tempPath.resolve("nbproject").resolve("project.properties"))) {
                        projectProperties.store(o, null);
                    }
                    Path configs = PathUtil.resolveAndCreateDir(d.tempPath.resolve("nbproject"), "configs");
                    for (IdeProject.RunConfig rc : ideProject.runConfigs) {
                        writeRunConfig(configs.resolve(rc.name.replace(' ', '_') + ".properties"), rc);
                    }
                    d.commit();
                } 
            } catch (Exception e) {
                throw Util.sneak(e);
            }
        }
        
        void writeProjectXml(Path dir) {
            try {
                Path p = dir.resolve("nbproject").resolve("project.xml");
                try (XmlUtil.FormattedXMLStreamWriter w = XmlUtil.newStreamWriter(Files.newBufferedWriter(p))) {
                    w.writeStartDocument("UTF-8", "1.0");
                    w.newline();
                    w.writeStartElement("project");
                    w.writeAttribute("xmlns", "http://www.netbeans.org/ns/project/1");
                    w.indent();
                    w.newline();
                        w.writeStartElement("type");
                        w.writeCharacters("org.netbeans.modules.java.j2seproject");
                        w.writeEndElement();
                        w.newline();
                        w.writeStartElement("configuration");
                        w.indent();
                        w.newline();
                            w.writeStartElement("data");
                            w.writeAttribute("xmlns", "http://www.netbeans.org/ns/j2se-project/3");
                            w.indent();
                            w.newline();
                                w.writeStartElement("name");
                                w.writeCharacters(ideProject.name);
                                w.writeEndElement();
                                w.newline();
                                w.writeStartElement("source-roots");
                                w.indent();
                                w.newline();
                                    w.writeEmptyElement("root");
                                    w.writeAttribute("id", "src.dir");
                                w.unindent();
                                w.newline();
                                w.writeEndElement();
                                w.newline();
                                w.writeEmptyElement("test-roots");
                            w.unindent();
                            w.newline();
                            w.writeEndElement();
                        w.unindent();
                        w.newline();
                        w.writeEndElement();
                    w.unindent();
                    w.newline();
                    w.writeEndElement();
                }
            } catch (Exception ex) {
                throw Util.sneak(ex);
            }
        }
        
        void writeRunConfig(Path file, IdeProject.RunConfig rc) throws IOException {
            Properties config = new Properties();
            config.setProperty("$label", rc.name);
            config.setProperty("main.class", rc.mainClass);
            StringBuilder vmargs = new StringBuilder();
            for (String arg : rc.vmArgs.get()) {
                vmargs.append(quote(arg));
                vmargs.append(' ');
            }
            config.setProperty("run.jvmargs", vmargs.toString());
            StringBuilder args = new StringBuilder();
            for (String arg : rc.args.get()) {
                args.append(quote(arg));
                args.append(' ');
            }
            config.setProperty("application.args", args.toString());
            config.setProperty("work.dir", rc.cwd.toString());
            StringBuilder runCpStr = new StringBuilder();
            runCpStr.append("${build.classes.dir}");
            ArrayList<Path> cp = new ArrayList<>(rc.classpath.get());
            cp.addAll(rc.resourcePaths);
            for (Path p : cp) {
                runCpStr.append(File.pathSeparator);
                runCpStr.append(p.toString());
            }
            config.setProperty("run.classpath", runCpStr.toString());
            try (OutputStream o = PathUtil.outputStream(file)) {
                config.store(o, null);
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
    
    static String quote(String arg) {
        return '"' + arg.replace("\\", "\\\\").replace("\"", "\\\"") + '"';
    }
}
