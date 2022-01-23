package io.github.coolcrabs.brachyura.ide;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

import javax.xml.stream.XMLStreamException;

import io.github.coolcrabs.brachyura.dependency.JavaJarDependency;
import io.github.coolcrabs.brachyura.ide.IdeProject.RunConfig;
import io.github.coolcrabs.brachyura.util.PathUtil;
import io.github.coolcrabs.brachyura.util.Util;
import io.github.coolcrabs.brachyura.util.XmlUtil;
import io.github.coolcrabs.brachyura.util.XmlUtil.FormattedXMLStreamWriter;

public enum Intellijank implements Ide {
    INSTANCE;

    @Override
    public String ideName() {
        return "idea";
    }

    // I just work here
    String toIntellijankPath(Path path) {
        if (Files.isDirectory(path)) {
            return path.toUri().toASCIIString();
        } else {
            return "jar" +  path.toUri().toASCIIString().substring(4) + "!/";
        }
    }

    @Override
    public void updateProject(Path projectDir, IdeProject ideProject) {
        try {
            Path ideaPath = projectDir.resolve(".idea");
            if (Files.exists(ideaPath)) PathUtil.deleteDirectory(ideaPath);
            Files.copy(Intellijank.class.getResourceAsStream("/idea/modules.xml"), PathUtil.resolveAndCreateDir(projectDir, ".idea").resolve("modules.xml"));
            Files.copy(Intellijank.class.getResourceAsStream("/idea/misc.xml"), ideaPath.resolve("misc.xml"));
            try (FormattedXMLStreamWriter w = XmlUtil.newStreamWriter(Files.newBufferedWriter(projectDir.resolve("main.iml")))) {
                w.writeStartDocument("UTF-8", "1.0");
                w.newline();
                w.writeStartElement("module");
                w.writeAttribute("type", "JAVA_MODULE");
                w.writeAttribute("version", "4");
                    w.indent();
                    w.newline();
                    w.writeStartElement("component");
                    w.writeAttribute("name", "NewModuleRootManager");
                    w.writeAttribute("inherit-compiler-output", "true");
                    w.indent();
                    w.newline();
                        w.writeEmptyElement("exclude-output");
                        w.newline();
                        w.writeStartElement("content");
                        w.writeAttribute("url", "file://$MODULE_DIR$");
                        w.indent();
                            for (Path sourceDir : ideProject.sourcePaths.values()) {
                                w.newline();
                                w.writeEmptyElement("sourceFolder");
                                w.writeAttribute("url", sourceDir.toUri().toASCIIString());
                            }
                            for (Path resourceDir : ideProject.resourcePaths) {
                                w.newline();
                                w.writeEmptyElement("sourceFolder");
                                w.writeAttribute("url", resourceDir.toUri().toASCIIString());
                                w.writeAttribute("type", "java-resource");
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
            Path libsPath = PathUtil.resolveAndCreateDir(ideaPath, "libraries");
            for (JavaJarDependency dep : ideProject.dependencies.get()) {
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
            Path runConfigPath = PathUtil.resolveAndCreateDir(ideaPath, "runConfigurations");
            for (RunConfig run : ideProject.runConfigs) {
                try (FormattedXMLStreamWriter w = XmlUtil.newStreamWriter(Files.newBufferedWriter(runConfigPath.resolve(run.name + ".xml")))) {
                    w.writeStartDocument("UTF-8", "1.0");
                    w.newline();
                    w.writeStartElement("component");
                    w.writeAttribute("name", "ProjectRunConfigurationManager");
                    w.indent();
                    w.newline();
                        w.writeStartElement("configuration");
                        w.writeAttribute("default", "false");
                        w.writeAttribute("name", run.name);
                        w.writeAttribute("type", "Application");
                        w.writeAttribute("nameIsGenerated", "false"); // Yeet
                        w.indent();
                        option(w, "MAIN_CLASS_NAME", run.mainClass);
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
                        cp.add(projectDir.resolve(".brachyura").resolve("ideaout").resolve("production").resolve("main")); // ???
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
        } catch (Exception e) {
            throw Util.sneak(e);
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
