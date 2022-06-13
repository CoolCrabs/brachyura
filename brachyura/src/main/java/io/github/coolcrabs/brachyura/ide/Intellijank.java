package io.github.coolcrabs.brachyura.ide;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import io.github.coolcrabs.brachyura.dependency.JavaJarDependency;
import io.github.coolcrabs.brachyura.ide.IdeModule.RunConfig;
import io.github.coolcrabs.brachyura.util.JvmUtil;
import io.github.coolcrabs.brachyura.util.PathUtil;
import io.github.coolcrabs.brachyura.util.Util;
import io.github.coolcrabs.brachyura.util.XmlUtil;
import io.github.coolcrabs.brachyura.util.XmlUtil.FormattedXMLStreamWriter;

// NOTE: Intellij breaks on url formatted urls so the path.toString usage is "correct"
public enum Intellijank implements Ide {
    INSTANCE;

    @Override
    public String ideName() {
        return "idea";
    }

    // I just work here
    String toIntellijankPath(Path path) {
        if (Files.isDirectory(path)) {
            return jankFilePath(path);
        } else {
            return "jar://" +  path.toString() + "!/";
        }
    }

    String jankFilePath(Path path) {
        // Url encoding? Never heard of it
        return "file://" + path.toString();
    }

    @Override
    public void updateProject(Path projectRoot, IdeModule... ideModules) {
        Ide.validate(ideModules);
        try {
            Path ideaPath = projectRoot.resolve(".idea");

            if (Files.exists(ideaPath)) {
                // Delete everything in .idea except for vcs.xml
                // We skip vcs.xml since otherwise we'll reset people's Git configurations
                try (DirectoryStream<Path> files = Files.newDirectoryStream(ideaPath,
                        file -> !"vcs.xml".equals(file.getFileName().toString()))) {
                    for (Path file : files) {
                        if (Files.isDirectory(file)) {
                            PathUtil.deleteDirectory(file);
                        } else {
                            Files.delete(file);
                        }
                    }
                }
            }

            Files.walkFileTree(projectRoot, Collections.emptySet(), 1, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if (file.toString().endsWith(".iml")) {
                        Files.delete(file);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
            int maxJava = 0;
            for (IdeModule m : ideModules) {
                maxJava = Math.max(maxJava, m.javaVersion);
            }
            writeModules(projectRoot, ideModules);
            writeMisc(ideaPath.resolve("misc.xml"), maxJava);
            for (IdeModule m : ideModules) {
                writeModule(m);
                writeRunConfigurations(projectRoot, m);
                writeLibs(projectRoot, m.dependencies.get());
            }
        } catch (Exception e) {
            throw Util.sneak(e);
        }
    }

    void writeModules(Path rootDir, IdeModule[] ideModules) throws IOException, XMLStreamException {
        try (FormattedXMLStreamWriter w = XmlUtil.newStreamWriter(Files.newBufferedWriter(PathUtil.resolveAndCreateDir(rootDir, ".idea").resolve("modules.xml")))) {
            w.writeStartDocument("UTF-8", "1.0");
            w.newline();
            w.writeStartElement("project");
            w.writeAttribute("version", "4");
            w.indent();
            w.newline();
                w.writeStartElement("component");
                w.writeAttribute("name", "ProjectModuleManager");
                w.indent();
                w.newline();
                    w.writeStartElement("modules");
                    w.indent();
                        for (IdeModule m : ideModules) {
                            w.newline();
                            w.writeEmptyElement("module");
                            String path;
                            if (rootDir.equals(m.root)) {
                                path = "$PROJECT_DIR$/" + m.name + ".iml";
                            } else {
                                path = "$PROJECT_DIR$/" + rootDir.relativize(m.root).resolve(m.name + ".iml");
                            }
                            w.writeAttribute("fileurl", "file://" + path);
                            w.writeAttribute("filepath", path);
                        }
                        w.unindent();
                        w.newline();
                    w.writeEndElement();
                    w.unindent();
                    w.newline();
                w.writeEndElement();
                w.unindent();
                w.newline();
            w.writeEndElement();
            w.newline();
            w.writeEndDocument();
        }
    }

    void writeModule(IdeModule ideProject) throws IOException, XMLStreamException {
        try (FormattedXMLStreamWriter w = XmlUtil.newStreamWriter(Files.newBufferedWriter(ideProject.root.resolve(ideProject.name + ".iml")))) {
            w.writeStartDocument("UTF-8", "1.0");
            w.newline();
            w.writeStartElement("module");
            w.writeAttribute("type", "JAVA_MODULE");
            w.writeAttribute("version", "4");
                w.indent();
                w.newline();
                w.writeStartElement("component");
                w.writeAttribute("name", "NewModuleRootManager");
                w.writeAttribute("LANGUAGE_LEVEL", languageLevel(ideProject.javaVersion));
                w.writeAttribute("inherit-compiler-output", "true");
                w.indent();
                w.newline();
                    w.writeEmptyElement("exclude-output");
                    w.newline();
                    w.writeStartElement("content");
                    w.writeAttribute("url", "file://$MODULE_DIR$");
                    w.indent();
                        for (Path sourceDir : ideProject.sourcePaths) {
                            w.newline();
                            w.writeEmptyElement("sourceFolder");
                            w.writeAttribute("url", jankFilePath(sourceDir));
                        }
                        for (Path resourceDir : ideProject.resourcePaths) {
                            w.newline();
                            w.writeEmptyElement("sourceFolder");
                            w.writeAttribute("url", jankFilePath(resourceDir));
                            w.writeAttribute("type", "java-resource");
                        }
                        for (Path sourceDir : ideProject.testSourcePaths) {
                            w.newline();
                            w.writeEmptyElement("sourceFolder");
                            w.writeAttribute("url", jankFilePath(sourceDir));
                            w.writeAttribute("isTestSource", "true");
                        }
                        for (Path resourceDir : ideProject.testResourcePaths) {
                            w.newline();
                            w.writeEmptyElement("sourceFolder");
                            w.writeAttribute("url", jankFilePath(resourceDir));
                            w.writeAttribute("type", "java-test-resource");
                        }
                    w.unindent();
                    w.newline();
                    w.writeEndElement();
                    w.newline();
                    w.writeEmptyElement("orderEntry");
                    w.writeAttribute("type", "inheritedJdk");
                    w.newline();
                    w.writeEmptyElement("orderEntry");
                    w.writeAttribute("type", "sourceFolder");
                    w.writeAttribute("forTests", "false");
                    for (JavaJarDependency dep : ideProject.dependencies.get()) {
                        w.newline();
                        w.writeEmptyElement("orderEntry");
                        w.writeAttribute("type", "library");
                        w.writeAttribute("name", dep.jar.getFileName().toString());
                        w.writeAttribute("level", "project");
                    }
                w.unindent();
                w.newline();
                w.writeEndElement();
                w.unindent();
            w.newline();
            w.writeEndElement();
            w.newline();
            w.writeEndDocument();
        }
    }

    void writeRunConfigurations(Path projectDir, IdeModule ideProject) throws IOException, XMLStreamException {
        Path ideaPath = projectDir.resolve(".idea");
        Path runConfigPath = PathUtil.resolveAndCreateDir(ideaPath, "runConfigurations");
        for (RunConfig run : ideProject.runConfigs) {
            String rcname = ideProject.name + " - " + run.name;
            try (FormattedXMLStreamWriter w = XmlUtil.newStreamWriter(Files.newBufferedWriter(runConfigPath.resolve(rcname + ".xml")))) {
                w.writeStartDocument("UTF-8", "1.0");
                w.newline();
                w.writeStartElement("component");
                w.writeAttribute("name", "ProjectRunConfigurationManager");
                w.indent();
                w.newline();
                    w.writeStartElement("configuration");
                    w.writeAttribute("default", "false");
                    w.writeAttribute("name", rcname);
                    w.writeAttribute("type", "Application");
                    w.writeAttribute("nameIsGenerated", "false"); // Yeet
                    w.indent();
                    option(w, "MAIN_CLASS_NAME", run.mainClass);
                    w.newline();
                    w.writeEmptyElement("module");
                    w.writeAttribute("name", ideProject.name);
                    option(w, "name", "main");
                    option(w, "WORKING_DIRECTORY", run.cwd.toString());
                    StringBuilder vmParam = new StringBuilder();
                    for (String arg : run.vmArgs.get()) {
                        vmParam.append(quote(arg));
                        vmParam.append(' ');
                    }
                    vmParam.append(" -cp ");
                    ArrayList<Path> cp = new ArrayList<>(run.classpath.get());
                    cp.addAll(run.resourcePaths);
                    ArrayList<IdeModule> modules = new ArrayList<>();
                    modules.add(ideProject);
                    modules.addAll(run.additionalModulesClasspath);
                    for (IdeModule m : modules) {
                        cp.add(projectDir.resolve(".brachyura").resolve("ideaout").resolve("production").resolve(m.name)); // ???
                    }
                    StringBuilder cpbuilder = new StringBuilder();
                    for (Path cp0 : cp) {
                        cpbuilder.append(cp0.toString());
                        cpbuilder.append(File.pathSeparatorChar);
                    }
                    cpbuilder.setLength(Math.max(cpbuilder.length() - 1, 0));
                    vmParam.append(quote(cpbuilder.toString()));
                    option(w, "VM_PARAMETERS", vmParam.toString());
                    StringBuilder runArg = new StringBuilder();
                    for (String arg : run.args.get()) {
                        runArg.append(quote(arg));
                        runArg.append(' ');
                    }
                    runArg.setLength(Math.max(runArg.length() - 1, 0));
                    option(w, "PROGRAM_PARAMETERS", runArg.toString());
                    w.unindent();
                    w.newline();
                    w.writeEndElement();
                w.unindent();
                w.newline();
                w.writeEndElement();
                w.writeEndDocument();
            }
        }
    }

    void writeMisc(Path miscXml, int java) throws IOException, XMLStreamException {
        try (FormattedXMLStreamWriter w = new FormattedXMLStreamWriter(Files.newBufferedWriter(miscXml))) {
            w.writeStartDocument("UTF-8", "1.0");
            w.newline();
            w.writeStartElement("project");
            w.writeAttribute("version", "4");
            w.indent();
            w.newline();
                w.writeStartElement("component");
                w.writeAttribute("name", "ProjectRootManager");
                w.writeAttribute("version", "2");
                w.writeAttribute("lanaguageLevel", languageLevel(java));
                w.writeAttribute("default", "true");
                w.writeAttribute("project-jdk-name", JvmUtil.javaVersionString(java));
                w.writeAttribute("project-jdk-type", "JavaSDK");
                w.indent();
                w.newline();
                    w.writeEmptyElement("output");
                    w.writeAttribute("url", "file://$PROJECT_DIR$/.brachyura/ideaout");
                    w.unindent();
                    w.newline();
                w.writeEndElement();
                w.unindent();
                w.newline();
            w.writeEndElement();
            w.newline();
            w.writeEndDocument();
        }
    }

    String languageLevel(int java) {
        return "JDK_" + JvmUtil.javaVersionString(java).replace('.', '_');
    }

    void writeLibs(Path projectDir, List<JavaJarDependency> libs) throws IOException, XMLStreamException {
        Path ideaPath = projectDir.resolve(".idea");
        Path libsPath = PathUtil.resolveAndCreateDir(ideaPath, "libraries");
        for (JavaJarDependency dep : libs) {
            try (FormattedXMLStreamWriter w = XmlUtil.newStreamWriter(Files.newBufferedWriter(libsPath.resolve(dep.jar.getFileName().toString() + ".xml")))) {
                w.writeStartDocument("UTF-8", "1.0");
                w.newline();
                w.writeStartElement("component");
                w.writeAttribute("name", "libraryTable");
                w.indent();
                w.newline();
                    w.writeStartElement("library");
                    w.writeAttribute("name", dep.jar.getFileName().toString());
                    w.indent();
                    w.newline();
                        w.writeStartElement("CLASSES");
                        w.indent();
                        w.newline();
                            w.writeEmptyElement("root");
                            w.writeAttribute("url", toIntellijankPath(dep.jar));
                        w.unindent();
                        w.newline();
                        w.writeEndElement();
                        w.newline();
                        w.writeEmptyElement("JAVADOC");
                        if (dep.sourcesJar != null) {
                            w.newline();
                            w.writeStartElement("SOURCES");
                            w.indent();
                            w.newline();
                                w.writeEmptyElement("root");
                                w.writeAttribute("url", toIntellijankPath(dep.sourcesJar));
                            w.unindent();
                            w.newline();
                            w.writeEndElement();
                        }
                    w.unindent();
                    w.newline();
                    w.writeEndElement();
                w.unindent();
                w.newline();
                w.writeEndElement();
                w.writeEndDocument();
            }
        }
    }

    static void option(FormattedXMLStreamWriter w, String name, String value) throws XMLStreamException {
        w.newline();
        w.writeEmptyElement("option");
        w.writeAttribute("name", name);
        w.writeAttribute("value", value);
    }

    static String quote(String arg) {
        return '"' + arg.replace("\\", "\\\\").replace("\"", "\\\"") + '"';
    }
}
