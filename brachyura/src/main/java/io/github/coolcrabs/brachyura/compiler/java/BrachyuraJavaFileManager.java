package io.github.coolcrabs.brachyura.compiler.java;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import javax.tools.JavaFileObject.Kind;

import io.github.coolcrabs.brachyura.memurl.MemoryUrlProvider;
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
    InputFiles extraCp = new InputFiles();
    MemoryUrlProvider extraCpUrl = new MemoryUrlProvider(p -> extraCp.files.get(p).in);
    HashMap<URI, OutputFile> output = new HashMap<>();

    public BrachyuraJavaFileManager() {
        super(ToolProvider.getSystemJavaCompiler().getStandardFileManager(null, null, StandardCharsets.UTF_8));
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

    @Override
    public Iterable<JavaFileObject> list(Location location, String packageName, Set<Kind> kinds, boolean recurse) throws IOException {
        return () -> {
            try {
                return new Iterator<JavaFileObject>() {
                    Iterator<JavaFileObject> a = extraCp.it(packageName, kinds, recurse);
                    Iterator<JavaFileObject> b = BrachyuraJavaFileManager.super.list(location, packageName, kinds, recurse).iterator();

                    @Override
                    public boolean hasNext() {
                        return a.hasNext() || b.hasNext();
                    }

                    @Override
                    public JavaFileObject next() {
                        if (a.hasNext()) return a.next();
                        if (b.hasNext()) return b.next();
                        throw new NoSuchElementException();
                    }
                };
            } catch (IOException e) {
                throw Util.sneak(e);
            }
        };
    }

    @Override
    public JavaFileObject getJavaFileForOutput(Location location, String className, Kind kind, FileObject sibling) throws IOException {
        URI uri = uri(location, className.replace('.', '/') + kind.extension);
        return output.computeIfAbsent(uri, u -> new OutputFile(uri, kind, sibling));
    }

    @Override
    public FileObject getFileForOutput(Location location, String packageName, String relativeName, FileObject sibling) throws IOException {
        URI uri = uri(location, packageName, relativeName);
        return output.computeIfAbsent(uri, u -> new OutputFile(uri, Kind.OTHER, sibling));
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
            urls.add(extraCpUrl.getRootUrl());
            ClassLoader platformClassloader = ClassLoader.getSystemClassLoader().getParent(); // null (bootstrap) in java 8, an actual classloader in java 9
            return new URLClassLoader(urls.toArray(new URL[0]), platformClassloader);
        } catch (Exception e) {
            throw Util.sneak(e);
        }
    }

    @Override
    public String inferBinaryName(Location location, JavaFileObject file) {
        if (file instanceof InputFile) {
            InputFile f = (InputFile) file;
            if (!f.path.endsWith(".class")) {
                return null;
            }
            return f.path.substring(0, f.path.length() - 6).replace('/', '.');
        }
        return super.inferBinaryName(location, file);
    }

    @Override
    public void close() throws IOException {
        super.close();
        extraCpUrl.close();
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
