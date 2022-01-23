package io.github.coolcrabs.brachyura.ide;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import javax.xml.stream.XMLStreamException;

import io.github.coolcrabs.brachyura.dependency.JavaJarDependency;
import io.github.coolcrabs.brachyura.ide.IdeProject.RunConfig;
import io.github.coolcrabs.brachyura.util.JvmUtil;
import io.github.coolcrabs.brachyura.util.Util;
import io.github.coolcrabs.brachyura.util.XmlUtil;
import io.github.coolcrabs.brachyura.util.XmlUtil.FormattedXMLStreamWriter;

public enum Eclipse implements Ide {
    INSTANCE;

    @Override
    public String ideName() {
        return "eclipse";
    }

    @Override
    public void updateProject(Path projectDir, IdeProject ideProject) {
        try {
            writeProject(ideProject, projectDir.resolve(".project"));
            writeClasspath(ideProject, projectDir.resolve(".classpath"));
            writeLaunchConfigs(projectDir, ideProject);
        } catch (Exception e) {
            throw Util.sneak(e);
        }
    }

    void writeProject(IdeProject project, Path dotProjectPath) throws IOException, XMLStreamException {
        try (FormattedXMLStreamWriter w = XmlUtil.newStreamWriter(Files.newBufferedWriter(dotProjectPath))) {
            w.writeStartDocument("UTF-8", "1.0");
            w.newline();
            w.writeStartElement("projectDescription");
            w.indent();
            w.newline();
                w.writeStartElement("name");
                w.writeCharacters(project.name);
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
                w.newline();
                w.writeStartElement("linkedResources");
                for (Entry<String, Path> e : project.sourcePaths.entrySet()) {
                    w.indent();
                    w.newline();
                    w.writeStartElement("link");
                    w.indent();
                    w.newline();
                        w.writeStartElement("name");
                        w.writeCharacters("\u200B" + e.getKey()); // Fix eclipse bugs with this one weird trick
                        w.writeEndElement();
                        w.newline();
                        w.writeStartElement("type");
                        w.writeCharacters("2"); // ???
                        w.writeEndElement();
                        w.newline();
                        w.writeStartElement("location");
                        w.writeCharacters(e.getValue().toString());
                        w.writeEndElement();
                        w.unindent();
                        w.newline();
                    w.writeEndElement();
                    w.unindent();
                }
                w.newline();
                w.writeEndElement();
                w.unindent();
                w.newline();
            w.writeEndElement();
            w.newline();
            w.writeEndDocument();
        }
    }

    void writeClasspath(IdeProject project, Path dotClasspath) throws IOException, XMLStreamException {
        try (FormattedXMLStreamWriter w = XmlUtil.newStreamWriter(Files.newBufferedWriter(dotClasspath))) {
            w.writeStartDocument("UTF-8", "1.0");
            w.newline();
            w.writeStartElement("classpath");
            w.indent();
            w.newline();
                w.writeEmptyElement("classpathentry");
                w.writeAttribute("kind", "con");
                w.writeAttribute("path", "org.eclipse.jdt.launching.JRE_CONTAINER/org.eclipse.jdt.internal.debug.ui.launcher.StandardVMType/JavaSE-" + JvmUtil.javaVersionString(JvmUtil.CURRENT_JAVA_VERSION));
                for (String src : project.sourcePaths.keySet()) {
                    w.newline();
                    w.writeEmptyElement("classpathentry");
                    w.writeAttribute("kind", "src");
                    w.writeAttribute("path", "\u200B" + src); // See above
                }
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

    void writeLaunchConfigs(Path projectDir, IdeProject ideProject) throws IOException, XMLStreamException {
        try {
            for (RunConfig rc : ideProject.runConfigs) {
                try (FormattedXMLStreamWriter w = XmlUtil.newStreamWriter(Files.newBufferedWriter(projectDir.resolve(rc.name + ".launch")))) {
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
                            w.writeAttribute("value", "/" + rc.name + "/");
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
                        w.newline();
                            List<Path> cp = new ArrayList<>(rc.classpath.get());
                            cp.addAll(rc.resourcePaths);
                            for (Path p : cp) {
                                w.writeEmptyElement("listEntry");
                                w.writeAttribute("value", libraryValue(p, projectDir, ideProject.name));
                                w.newline();
                            }
                            w.writeEmptyElement("listEntry");
                            w.writeAttribute("value", projectValue(ideProject.name));
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

    String libraryValue(Path lib, Path projectRoot, String projectName) throws XMLStreamException {
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

    static String quote(String arg) {
        return '"' + arg.replace("\\", "\\\\").replace("\"", "\\\"") + '"';
    }
}
