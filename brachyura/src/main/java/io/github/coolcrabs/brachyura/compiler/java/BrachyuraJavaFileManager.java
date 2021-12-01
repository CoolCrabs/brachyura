package io.github.coolcrabs.brachyura.compiler.java;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;

import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import javax.tools.JavaFileObject.Kind;

import io.github.coolcrabs.brachyura.processing.ProcessingSource;
import io.github.coolcrabs.brachyura.util.ByteArrayOutputStreamEx;
import io.github.coolcrabs.brachyura.util.PathUtil;
import io.github.coolcrabs.brachyura.util.Util;

// References:
// https://github.com/openjdk/jdk/blob/739769c8fc4b496f08a92225a12d07414537b6c0/src/jdk.jshell/share/classes/jdk/jshell/MemoryFileManager.java
// http://atamur.blogspot.com/2009/10/using-built-in-javacompiler-with-custom.html
// https://web.archive.org/web/20080117045744/http://www.ibm.com/developerworks/java/library/j-jcomp/index.html
// https://github.com/openjdk/jdk/blob/2b02b6f513b062261195ca1edd059d16abb7bec6/src/jdk.compiler/share/classes/com/sun/tools/javac/file/JavacFileManager.java
// https://github.com/openjdk/jdk/blob/2b02b6f513b062261195ca1edd059d16abb7bec6/src/jdk.compiler/share/classes/com/sun/tools/javac/file/BaseFileManager.java
// https://www.soulmachine.me/blog/2015/07/22/compile-and-run-java-source-code-in-memory/
// https://github.com/OpenHFT/Java-Runtime-Compiler

class BrachyuraJavaFileManager extends ForwardingJavaFileManager<StandardJavaFileManager> implements StandardJavaFileManager {
    HashMap<URI, OutputFile> output = new HashMap<>();

    public BrachyuraJavaFileManager() {
        super(ToolProvider.getSystemJavaCompiler().getStandardFileManager(null, null, StandardCharsets.UTF_8));
    }

    public ProcessingSource getProcessingSource() {
        return new BrachyuraJavaFileManagerProcessingSource(this);
    }

    private URI uri(Location location, String packageName, String relativeName) {
        if (packageName != null) {
            return uri(location, packageName.replace('.', '/') + relativeName);
        } else {
            return uri(location, relativeName);
        }
        
    }

    private URI uri(Location location, String path) {
        try {
            return new URI("crabmoment", location.getName().replaceAll("[^a-zA-Z0-9]", "."), path.startsWith("/") ? path : "/" + path, null);
        } catch (Exception e) {
            throw Util.sneak(e);
        }
    }

    static class OutputFile extends SimpleJavaFileObject {
        ByteArrayOutputStreamEx bytes = new ByteArrayOutputStreamEx();

        protected OutputFile(URI uri, Kind kind) {
            super(uri, kind);
        }

        URI rawUri() {
            return super.toUri();
        }

        @Override
        public URI toUri() {
            // https://github.com/SpongePowered/Mixin/blob/1e1aa7fb52dec78630f3f2f53fd70a4c496a7d66/src/ap/java/org/spongepowered/tools/obfuscation/ReferenceManager.java#L158
            boolean workaround = false;
            for (StackTraceElement e : Thread.currentThread().getStackTrace()) {
                if (e.getClassName().equals("org.spongepowered.tools.obfuscation.ReferenceManager")) {
                    workaround = true;
                }
                if (e.getMethodName().equals("createResource")) {
                    return super.toUri();
                }
            }
            if (workaround) {
                return PathUtil.CWD.resolve("MIXINBUGWORKAROUND").toFile().toURI();
            }
            return super.toUri();
        }

        @Override
        public InputStream openInputStream() {
            return new ByteArrayInputStream(bytes.buf(), 0, bytes.size());
        }

        @Override
        public OutputStream openOutputStream() {
            bytes.reset();
            return bytes;
        }
    }

    @Override
    public JavaFileObject getJavaFileForOutput(Location location, String className, Kind kind, FileObject sibling) throws IOException {
        URI uri = uri(location, className.replace('.', '/') + kind.extension);
        return output.computeIfAbsent(uri, u -> new OutputFile(uri, kind));
    }

    @Override
    public FileObject getFileForOutput(Location location, String packageName, String relativeName, FileObject sibling) throws IOException {
        URI uri = uri(location, packageName, relativeName);
        return output.computeIfAbsent(uri, u -> new OutputFile(uri, Kind.OTHER));
    }

    @Override
    public boolean isSameFile(FileObject a, FileObject b) {
        if (a instanceof OutputFile || b instanceof OutputFile) {
            return false; // Bypass's filer's restrictions that break mixin
        }
        return super.isSameFile(a, b);
    }

    // Overriden to ignore brachyura's classpath
    // https://github.com/openjdk/jdk/blob/41daa88dcc89e509f21d1685c436874d6479cf62/src/jdk.compiler/share/classes/com/sun/tools/javac/file/JavacFileManager.java#L742
    // Problem: https://github.com/openjdk/jdk/blob/41daa88dcc89e509f21d1685c436874d6479cf62/src/jdk.compiler/share/classes/com/sun/tools/javac/file/BaseFileManager.java#L199
    @Override
    public ClassLoader getClassLoader(Location location) {
        try {
            ArrayList<URL> urls = new ArrayList<>();
            for (File f : getLocation(location)) {
                urls.add(f.toURI().toURL());
            }
            ClassLoader platformClassloader = ClassLoader.getSystemClassLoader().getParent(); // null (bootstrap) in java 8, an actual classloader in java 9
            return new URLClassLoader(urls.toArray(new URL[0]), platformClassloader);
        } catch (Exception e) {
            throw Util.sneak(e);
        }
    }

    //---
    // StandardJavaFileManager
    //---

    @Override
    public Iterable<? extends JavaFileObject> getJavaFileObjectsFromFiles(Iterable<? extends File> files) {
        return fileManager.getJavaFileObjectsFromFiles(files);
    }

    @Override
    public Iterable<? extends JavaFileObject> getJavaFileObjects(File... files) {
        return fileManager.getJavaFileObjects(files);
    }

    @Override
    public Iterable<? extends JavaFileObject> getJavaFileObjectsFromStrings(Iterable<String> names) {
        return fileManager.getJavaFileObjectsFromStrings(names);
    }

    @Override
    public Iterable<? extends JavaFileObject> getJavaFileObjects(String... names) {
        return fileManager.getJavaFileObjects(names);
    }

    @Override
    public void setLocation(Location location, Iterable<? extends File> path) throws IOException {
        fileManager.setLocation(location, path);
    }

    @Override
    public Iterable<? extends File> getLocation(Location location) {
        return fileManager.getLocation(location);
    }
    
}
