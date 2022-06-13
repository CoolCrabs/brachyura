package io.github.coolcrabs.brachyura.ide;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import com.google.gson.stream.JsonWriter;

import io.github.coolcrabs.brachyura.dependency.JavaJarDependency;
import io.github.coolcrabs.brachyura.ide.IdeModule.RunConfig;
import io.github.coolcrabs.brachyura.util.AtomicFile;
import io.github.coolcrabs.brachyura.util.JvmUtil;
import io.github.coolcrabs.brachyura.util.PathUtil;
import io.github.coolcrabs.brachyura.util.Util;
import io.github.coolcrabs.brachyura.util.XmlUtil;
import io.github.coolcrabs.brachyura.util.XmlUtil.FormattedXMLStreamWriter;

public enum Eclipse implements Ide {
    INSTANCE;

    @Override
    public String ideName() {
        return "jdt";
    }

    @Override
    public void updateProject(Path projectRoot, IdeModule... ideModules) {
        Ide.validate(ideModules);
        try {
            Files.walkFileTree(projectRoot, Collections.emptySet(), 1, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if (file.toString().endsWith(".launch")) {
                        Files.delete(file);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
            for (IdeModule module : ideModules) {
                if (Files.exists(module.root.resolve(".brachyura").resolve("eclipseout"))) PathUtil.deleteDirectoryChildren(module.root.resolve(".brachyura").resolve("eclipseout"));
                if (Files.exists(module.root.resolve(".brachyura").resolve("eclipsetestout"))) PathUtil.deleteDirectoryChildren(module.root.resolve(".brachyura").resolve("eclipsetestout"));
                writeModule(module);
                writeClasspath(module);
                writeLaunchConfigs(projectRoot, module);
                vscodeLaunchJson(projectRoot, ideModules);
            }
        } catch (Exception e) {
            throw Util.sneak(e);
        }
    }

    void writeModule(IdeModule module) throws IOException, XMLStreamException {
        try (FormattedXMLStreamWriter w = XmlUtil.newStreamWriter(Files.newBufferedWriter(module.root.resolve(".project")))) {
            w.writeStartDocument("UTF-8", "1.0");
            w.newline();
            w.writeStartElement("projectDescription");
            w.indent();
            w.newline();
                w.writeStartElement("name");
                w.writeCharacters(module.name);
                w.writeEndElement();
                w.newline();
                w.writeStartElement("comment");
                w.writeEndElement();
                w.newline();
                w.writeStartElement("projects");
                w.writeEndElement();
                w.newline();
                w.writeStartElement("buildSpec");
                w.indent();
                w.newline();
                    w.writeStartElement("buildCommand");
                    w.indent();
                    w.newline();
                        w.writeStartElement("name");
                        w.writeCharacters("org.eclipse.jdt.core.javabuilder");
                        w.writeEndElement();
                        w.newline();
                        w.writeStartElement("arguments");
                        w.writeEndElement();
                        w.unindent();
                        w.newline();
                    w.writeEndElement();
                    w.unindent();
                    w.newline();
                w.writeEndElement();
                w.newline();
                w.writeStartElement("natures");
                w.indent();
                w.newline();
                    w.writeStartElement("nature");
                    w.writeCharacters("org.eclipse.jdt.core.javanature");
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
        try (BufferedWriter prefs = Files.newBufferedWriter(PathUtil.resolveAndCreateDir(module.root, ".settings").resolve("org.eclipse.jdt.core.prefs"))) {
            prefs.write("eclipse.preferences.version=1\n");
            String j = JvmUtil.javaVersionString(module.javaVersion);
            prefs.write("org.eclipse.jdt.core.compiler.codegen.targetPlatform="); prefs.write(j); prefs.write('\n');
            prefs.write("org.eclipse.jdt.core.compiler.compliance="); prefs.write(j); prefs.write('\n');
            prefs.write("org.eclipse.jdt.core.compiler.source="); prefs.write(j); prefs.write('\n');
        }
    }

    void writeClasspath(IdeModule project) throws IOException, XMLStreamException {
        Path dotClasspath = project.root.resolve(".classpath");
        try (FormattedXMLStreamWriter w = XmlUtil.newStreamWriter(Files.newBufferedWriter(dotClasspath))) {
            w.writeStartDocument("UTF-8", "1.0");
            w.newline();
            w.writeStartElement("classpath");
            w.indent();
            w.newline();
                w.writeEmptyElement("classpathentry");
                w.writeAttribute("kind", "con");
                w.writeAttribute("path", "org.eclipse.jdt.launching.JRE_CONTAINER/org.eclipse.jdt.internal.debug.ui.launcher.StandardVMType/JavaSE-" + JvmUtil.javaVersionString(project.javaVersion));
                sourceClasspathEntryAttributes(w, project.root, project.sourcePaths, false);
                sourceClasspathEntryAttributes(w, project.root, project.resourcePaths, false);
                sourceClasspathEntryAttributes(w, project.root, project.testSourcePaths, true);
                sourceClasspathEntryAttributes(w, project.root, project.testResourcePaths, true);
                moduleDepClasspathEntries(w, project);
                for (JavaJarDependency dep : project.dependencies.get()) {
                    w.newline();
                    w.writeEmptyElement("classpathentry");
                    w.writeAttribute("kind", "lib");
                    w.writeAttribute("path", dep.jar.toString());
                    if (dep.sourcesJar != null) {
                        w.writeAttribute("sourcepath", dep.sourcesJar.toString());
                    }
                }
                w.newline();
                w.writeEmptyElement("classpathentry");
                w.writeAttribute("kind", "output");
                w.writeAttribute("path", ".brachyura/eclipseout");
            w.unindent();
            w.newline();
            w.writeEndElement();
            w.newline();
            w.writeEndDocument();
        }
    }

    void writeLaunchConfigs(Path projectDir, IdeModule ideProject) throws IOException, XMLStreamException {
        try {
            for (RunConfig rc : ideProject.runConfigs) {
                String rcname = ideProject.name + " - " + rc.name;
                try (FormattedXMLStreamWriter w = XmlUtil.newStreamWriter(Files.newBufferedWriter(projectDir.resolve(rcname + ".launch")))) {
                    w.writeStartDocument("UTF-8", "1.0");
                    w.writeStartElement("launchConfiguration");
                    w.writeAttribute("type", "org.eclipse.jdt.launching.localJavaApplication");
                    w.indent();
                    w.newline();
                        w.writeStartElement("listAttribute");
                        w.writeAttribute("key", "org.eclipse.debug.core.MAPPED_RESOURCE_PATHS");
                        w.indent();
                        w.newline();
                            w.writeEmptyElement("listEntry");
                            w.writeAttribute("value", "/" + rcname + "/");
                            w.unindent();
                            w.newline();
                        w.writeEndElement();
                        w.newline();
                        w.writeStartElement("listAttribute");
                        w.writeAttribute("key", "org.eclipse.debug.core.MAPPED_RESOURCE_TYPES");
                        w.indent();
                        w.newline();
                            w.writeEmptyElement("listEntry");
                            w.writeAttribute("value", "4");  // ???
                            w.unindent();
                            w.newline();
                        w.writeEndElement();
                        w.newline();
                        booleanAttribute(w, "org.eclipse.jdt.launching.ATTR_ATTR_USE_ARGFILE", false);
                        w.newline();
                        booleanAttribute(w, "org.eclipse.jdt.launching.ATTR_SHOW_CODEDETAILS_IN_EXCEPTION_MESSAGES", false);
                        w.newline();
                        booleanAttribute(w, "org.eclipse.jdt.launching.ATTR_USE_CLASSPATH_ONLY_JAR", false);
                        w.newline();
                        booleanAttribute(w, "org.eclipse.jdt.launching.ATTR_USE_START_ON_FIRST_THREAD", true);
                        w.newline();
                        w.writeStartElement("listAttribute");
                        w.writeAttribute("key", "org.eclipse.jdt.launching.CLASSPATH");
                        w.indent();
                            List<Path> cp = new ArrayList<>(rc.classpath.get());
                            cp.addAll(rc.resourcePaths);
                            for (Path p : cp) {
                                w.newline();
                                w.writeEmptyElement("listEntry");
                                w.writeAttribute("value", libraryValue(p));
                            }
                            List<IdeModule> modules = new ArrayList<>();
                            modules.add(ideProject);
                            modules.addAll(rc.additionalModulesClasspath);
                            for (IdeModule mod : modules) {
                                w.newline();
                                w.writeEmptyElement("listEntry");
                                w.writeAttribute("value", projectValue(mod.name));
                            }
                            w.unindent();
                            w.newline();
                        w.writeEndElement();
                        w.newline();
                        booleanAttribute(w, "org.eclipse.jdt.launching.DEFAULT_CLASSPATH", false);
                        w.newline();
                        stringAttribute(w, "org.eclipse.jdt.launching.MAIN_TYPE", rc.mainClass);
                        w.newline();
                        StringBuilder args = new StringBuilder();
                        for (String arg : rc.args.get()) {
                            args.append(quote(arg));
                            args.append(' ');
                        }
                        stringAttribute(w, "org.eclipse.jdt.launching.PROGRAM_ARGUMENTS", args.toString());
                        w.newline();
                        stringAttribute(w, "org.eclipse.jdt.launching.PROJECT_ATTR", ideProject.name);
                        w.newline();
                        StringBuilder vmargs = new StringBuilder();
                        for (String vmarg : rc.vmArgs.get()) {
                            vmargs.append(quote(vmarg));
                            vmargs.append(' ');
                        }
                        stringAttribute(w, "org.eclipse.jdt.launching.VM_ARGUMENTS", vmargs.toString());
                        w.newline();
                        stringAttribute(w, "org.eclipse.jdt.launching.WORKING_DIRECTORY", rc.cwd.toString());
                        w.unindent();
                        w.newline();
                    w.writeEndElement();
                    w.newline();
                    w.writeEndDocument();
                }
            }
        } catch (Exception e) {
            throw Util.sneak(e);
        }
    }

    String libraryValue(Path lib) throws XMLStreamException {
        StringWriter writer = new StringWriter();
        try (FormattedXMLStreamWriter w = XmlUtil.newStreamWriter(writer)) {
            w.writeStartDocument("UTF-8", "1.0");
            w.newline();
            w.writeEmptyElement("runtimeClasspathEntry");
            w.writeAttribute("externalArchive", lib.toString());
            w.writeAttribute("path", "5"); // ???
            w.writeAttribute("type", "2"); // ???
            w.newline();
            w.writeEndDocument();
        }
        return writer.toString();
    }

    String projectValue(String project) throws XMLStreamException {
        StringWriter writer = new StringWriter();
        try (FormattedXMLStreamWriter w = XmlUtil.newStreamWriter(writer)) {
            w.writeStartDocument("UTF-8", "1.0");
            w.newline();
            w.writeEmptyElement("runtimeClasspathEntry");
            w.writeAttribute("projectName", project);
            w.writeAttribute("path", "5"); // ???
            w.writeAttribute("type", "1"); // ???
            w.newline();
            w.writeEndDocument();
        }
        return writer.toString();
    }

    void booleanAttribute(FormattedXMLStreamWriter w, String key, boolean value) throws XMLStreamException {
        w.writeEmptyElement("booleanAttribute");
        w.writeAttribute("key", key);
        w.writeAttribute("value", Boolean.toString(value));
    }

    void stringAttribute(FormattedXMLStreamWriter w, String key, String value) throws XMLStreamException {
        w.writeEmptyElement("stringAttribute");
        w.writeAttribute("key", key);
        w.writeAttribute("value", value);
    }
    
    void sourceClasspathEntryAttributes(FormattedXMLStreamWriter w, Path projectDir, List<Path> paths, boolean isTest) throws XMLStreamException {
        for (Path src : paths) {
            w.newline();
            if (isTest) {
                w.writeStartElement("classpathentry");
            } else {
                w.writeEmptyElement("classpathentry");
            }
            w.writeAttribute("kind", "src");
            w.writeAttribute("path", projectDir.relativize(src).toString());
            if (isTest) {
                w.writeAttribute("output", ".brachyura/eclipsetestout");
                w.indent();
                w.newline();
                w.writeStartElement("attributes");
                w.indent();
                w.newline();
                w.writeEmptyElement("attribute");
                w.writeAttribute("name", "test");
                w.writeAttribute("value", "true");
                w.unindent();
                w.newline();
                w.writeEndElement();
                w.unindent();
                w.newline();
                w.writeEndElement();
            }
        }
    }

    void moduleDepClasspathEntries(FormattedXMLStreamWriter w, IdeModule module) throws XMLStreamException {
        for (IdeModule mod : module.dependencyModules) {
            w.newline();
            w.writeEmptyElement("classpathentry");
            w.writeAttribute("combineaccessrules", "false");
            w.writeAttribute("kind", "src");
            w.writeAttribute("path", "/" + mod.name);
        }
    }

    static String quote(String arg) {
        return '"' + arg.replace("\\", "\\\\").replace("\"", "\\\"") + '"';
    }

    void vscodeLaunchJson(Path rootDir, IdeModule... basemodules) throws IOException {
        try (AtomicFile atomicFile = new AtomicFile(rootDir.resolve(".vscode").resolve("launch.json"))) {
            try (JsonWriter jsonWriter = new JsonWriter(PathUtil.newBufferedWriter(atomicFile.tempPath))) {
                jsonWriter.setIndent("  ");
                jsonWriter.beginObject();
                jsonWriter.name("version").value("0.2.0");
                jsonWriter.name("configurations");
                jsonWriter.beginArray();
                for (IdeModule mod : basemodules) {
                    for (RunConfig runConfig : mod.runConfigs) {
                        jsonWriter.beginObject();
                        jsonWriter.name("type").value("java");
                        jsonWriter.name("name").value(mod.name + " - " + runConfig.name);
                        jsonWriter.name("request").value("launch");
                        jsonWriter.name("cwd").value(runConfig.cwd.toString());
                        jsonWriter.name("console").value("internalConsole");
                        jsonWriter.name("mainClass").value(runConfig.mainClass);
                        jsonWriter.name("vmArgs");
                        jsonWriter.beginArray();
                        for (String vmArg : runConfig.vmArgs.get()) {
                            jsonWriter.value(vmArg);
                        }
                        jsonWriter.endArray();
                        jsonWriter.name("args");
                        jsonWriter.beginArray();
                        for (String arg : runConfig.args.get()) {
                            jsonWriter.value(arg);
                        }
                        jsonWriter.endArray();
                        jsonWriter.name("stopOnEntry").value(false);
                        jsonWriter.name("projectName").value(mod.name);
                        jsonWriter.name("classPaths");
                        jsonWriter.beginArray();
                        jsonWriter.value(mod.root.resolve(".brachyura").resolve("eclipseout").toString());
                        for (IdeModule m : mod.dependencyModules) {
                            jsonWriter.value(m.root.resolve(".brachyura").resolve("eclipseout").toString());
                        }
                        for (Path path : runConfig.resourcePaths) {
                            jsonWriter.value(path.toString());
                        }
                        for (Path path : runConfig.classpath.get()) {
                            jsonWriter.value(path.toString());
                        }
                        jsonWriter.endArray();
                        jsonWriter.endObject();
                    }
                }
                jsonWriter.endArray();
                jsonWriter.endObject();
                jsonWriter.flush();
            }
            atomicFile.commit();
        }
    }
}
