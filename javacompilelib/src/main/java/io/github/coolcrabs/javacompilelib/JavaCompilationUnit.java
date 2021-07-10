package io.github.coolcrabs.javacompilelib;

import java.io.File;
import java.io.Serializable;

public class JavaCompilationUnit implements Serializable {
    public final File[] sourceFiles;
    public final File outputDir;
    public final File[] classpath;
    public final String[] annotationProcessors;
    public final String[] options;

    JavaCompilationUnit(File[] sourceFiles, File outputDir, File[] classpath, String[] annotationProcessors, String[] options) {
        this.sourceFiles = sourceFiles;
        this.outputDir = outputDir;
        this.classpath = classpath;
        this.annotationProcessors = annotationProcessors;
        this.options = options;
    }

    public static class Builder {
        private File[] sourceFiles;
        private File outputDir;
        private File[] classpath;
        public String[] annotationProcessors;
        public String[] options;

        public Builder sourceFiles(File[] sourceFiles) {
            this.sourceFiles = sourceFiles;
            return this;
        }

        public Builder outputDir(File outputDir) {
            this.outputDir = outputDir;
            return this;
        }

        public Builder classpath(File[] classpath) {
            this.classpath = classpath;
            return this;
        }

        public Builder annotationProcessors(String[] annotationProcessors) {
            this.annotationProcessors = annotationProcessors;
            return this;
        }

        public Builder options(String[] options) {
            this.options = options;
            return this;
        }

        public JavaCompilationUnit build() {
            return new JavaCompilationUnit(sourceFiles, outputDir, classpath, annotationProcessors, options);
        }
    }
}
