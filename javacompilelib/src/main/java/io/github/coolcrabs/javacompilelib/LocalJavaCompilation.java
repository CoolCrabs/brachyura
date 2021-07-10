package io.github.coolcrabs.javacompilelib;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.tools.JavaCompiler;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;
import javax.tools.JavaCompiler.CompilationTask;

public enum LocalJavaCompilation implements JavaCompilation {
    INSTANCE;

    @Override
    public boolean compile(JavaCompilationUnit javaCompilationUnit) {
        try {
            JavaCompiler javaCompiler = ToolProvider.getSystemJavaCompiler();
            StandardJavaFileManager fileManager = javaCompiler.getStandardFileManager(null, null, Charset.forName("UTF-8"));
            fileManager.setLocation(StandardLocation.CLASS_PATH, asList(javaCompilationUnit.classpath));
            fileManager.setLocation(StandardLocation.CLASS_OUTPUT, Collections.singleton(javaCompilationUnit.outputDir));
            CompilationTask compilationTask = javaCompiler.getTask(null, fileManager, null, asList(javaCompilationUnit.options), asList(javaCompilationUnit.annotationProcessors), fileManager.getJavaFileObjects(javaCompilationUnit.sourceFiles));
            return compilationTask.call();
        } catch (Exception e) {
            Util.doThrow(e);
            throw null; // unreachable
        }
    }

    private static <T> List<T> asList(T... a) {
        if (a == null) {
            return null;
        } else {
            return Arrays.asList(a);
        } 
    }
}
