package io.github.coolcrabs.brachyura.ide;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import io.github.coolcrabs.brachyura.dependency.JavaJarDependency;

public final class IdeProject {
    public final List<JavaJarDependency> dependencies;
    public final List<RunConfig> runConfigs;
    public final List<Path> sourcePaths;
    public final List<Path> resourcePaths;

    IdeProject(List<JavaJarDependency> dependencies, List<RunConfig> runConfigs, List<Path> sourcePaths, List<Path> resourcePaths) {
        this.dependencies = dependencies;
        this.runConfigs = runConfigs;
        this.sourcePaths = sourcePaths;
        this.resourcePaths = resourcePaths;
    }

    public static class IdeProjectBuilder {
        private List<JavaJarDependency> dependencies = Collections.emptyList();
        private List<RunConfig> runConfigs = Collections.emptyList();
        private List<Path> sourcePaths = Collections.emptyList();
        private List<Path> resourcePaths = Collections.emptyList();
        
        public IdeProjectBuilder dependencies(List<JavaJarDependency> dependencies) {
            this.dependencies = dependencies;
            return this;
        }

        public IdeProjectBuilder dependencies(JavaJarDependency... dependencies) {
            this.dependencies = Arrays.asList(dependencies);
            return this;
        }

        public IdeProjectBuilder runConfigs(List<RunConfig> runConfigs) {
            this.runConfigs = runConfigs;
            return this;
        }

        public IdeProjectBuilder runConfigs(RunConfig... runConfigs) {
            this.runConfigs = Arrays.asList(runConfigs);
            return this;
        }

        public IdeProjectBuilder sourcePaths(List<Path> sourcePaths) {
            this.sourcePaths = sourcePaths;
            return this;
        }

        public IdeProjectBuilder sourcePaths(Path... sourcePaths) {
            this.sourcePaths = Arrays.asList(sourcePaths);
            return this;
        }

        public IdeProjectBuilder resourcePaths(List<Path> resourcePaths) {
            this.resourcePaths = resourcePaths;
            return this;
        }

        public IdeProjectBuilder resourcePaths(Path... resourcePaths) {
            this.resourcePaths = Arrays.asList(resourcePaths);
            return this;
        }

        public IdeProject build() {
            return new IdeProject(dependencies, runConfigs, sourcePaths, resourcePaths);
        }
    }

    public static class RunConfig {
        public final String name;
        public final String mainClass;
        public final Path cwd; // Make sure this exists
        public final List<String> vmArgs;
        public final List<String> args;
        public final List<Path> classpath;

        RunConfig(String name, String mainClass, Path cwd, List<String> vmArgs, List<String> args, List<Path> classpath) {
            this.name = name;
            this.mainClass = mainClass;
            this.cwd = cwd;
            this.vmArgs = vmArgs;
            this.args = args;
            this.classpath = classpath;
        }

        public static class RunConfigBuilder {
            private String name;
            private String mainClass;
            private Path cwd;
            private List<String> vmArgs = Collections.emptyList();
            public List<String> args = Collections.emptyList();
            private List<Path> classpath = Collections.emptyList();

            public RunConfigBuilder name(String name) {
                this.name = name;
                return this;
            }

            public RunConfigBuilder mainClass(String mainClass) {
                this.mainClass = mainClass;
                return this;
            }

            public RunConfigBuilder cwd(Path cwd) {
                this.cwd = cwd;
                return this;
            }

            public RunConfigBuilder vmArgs(List<String> vmArgs) {
                this.vmArgs = vmArgs;
                return this;
            }

            public RunConfigBuilder vmArgs(String... vmArgs) {
                this.vmArgs = Arrays.asList(vmArgs);
                return this;
            }

            public RunConfigBuilder args(List<String> args) {
                this.args = args;
                return this;
            }

            public RunConfigBuilder args(String... args) {
                this.args = Arrays.asList(args);
                return this;
            }

            public RunConfigBuilder classpath(List<Path> classpath) {
                this.classpath = classpath;
                return this;
            }

            public RunConfigBuilder classpath(Path... classpath) {
                this.classpath = Arrays.asList(classpath);
                return this;
            }

            public RunConfig build() {
                return new RunConfig(name, mainClass, cwd, vmArgs, args, classpath);
            }
        }
    }
}
