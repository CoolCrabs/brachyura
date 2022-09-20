package io.github.coolcrabs.brachyura.project.java;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import io.github.coolcrabs.brachyura.dependency.JavaJarDependency;
import io.github.coolcrabs.brachyura.ide.IdeModule;
import io.github.coolcrabs.brachyura.maven.MavenId;
import io.github.coolcrabs.brachyura.maven.MavenPublishing;
import io.github.coolcrabs.brachyura.processing.ProcessorChain;
import io.github.coolcrabs.brachyura.processing.sinks.AtomicZipProcessingSink;
import io.github.coolcrabs.brachyura.processing.sources.DirectoryProcessingSource;
import io.github.coolcrabs.brachyura.project.Task;
import io.github.coolcrabs.brachyura.util.Lazy;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.tools.StandardLocation;

public abstract class SimpleJavaProject extends BaseJavaProject {
    public abstract MavenId getId();

    public int getJavaVersion() {
        return 8;
    }

    public List<JavaJarDependency> createDependencies() {
        return Collections.emptyList();
    }

    public final Lazy<SimpleJavaModule> projectModule = new Lazy<>(this::createProjectModule);
    public SimpleJavaModule createProjectModule() {
        return new SimpleJavaProjectModule();
    }

    public ProcessorChain getResourceProcessorChain() {
        return new ProcessorChain();
    }

    public class SimpleJavaProjectModule extends SimpleJavaModule {
        @Override
        public int getJavaVersion() {
            return SimpleJavaProject.this.getJavaVersion();
        }

        @Override
        public Path[] getSrcDirs() {
            return new Path[]{getModuleRoot().resolve("src").resolve("main").resolve("java")};
        }

        @Override
        public Path[] getResourceDirs() {
            return new Path[]{getProjectDir().resolve("src").resolve("main").resolve("resources")};
        }

        @Override
        protected List<JavaJarDependency> createDependencies() {
            return SimpleJavaProject.this.createDependencies();
        }

        @Override
        public String getModuleName() {
            return getId().artifactId;
        }

        @Override
        public Path getModuleRoot() {
            return getProjectDir();
        }
    }
    
    public String getJarBaseName() {
        return getId().artifactId + "-" + getId().version;
    }

    @Override
    public void getTasks(Consumer<Task> p) {
        super.getTasks(p);
        p.accept(Task.of("build", this.buildResult::get));
        getPublishTasks(p);
    }
    
    public void getPublishTasks(Consumer<Task> p) {
        createPublishTasks(p, this.buildResult);
    }
    
    public static void createPublishTasks(Consumer<Task> p, Supplier<JavaJarDependency> build) {
        p.accept(Task.of("publish", () -> MavenPublishing.publish(MavenPublishing.AuthenticatedMaven.ofEnv(), build.get())));
        p.accept(Task.of("publishToMavenLocal", () -> MavenPublishing.publish(MavenPublishing.AuthenticatedMaven.ofMavenLocal(), build.get())));
    }

    @Override
    public IdeModule[] getIdeModules() {
        return new IdeModule[] {projectModule.get().ideModule.get()};
    }

    public Lazy<JavaJarDependency> buildResult = new Lazy<>(this::build);
    protected JavaJarDependency build() {
        Path outjar = getBuildLibsDir().resolve(getJarBaseName() + ".jar");
        Path outjarsources = getBuildLibsDir().resolve(getJarBaseName() + "-sources.jar");
        try (
            AtomicZipProcessingSink jarSink = new AtomicZipProcessingSink(outjar);
            AtomicZipProcessingSink jarSourcesSink = new AtomicZipProcessingSink(outjarsources);
        ) {
            getResourceProcessorChain().apply(jarSink, Arrays.stream(projectModule.get().getResourceDirs()).map(DirectoryProcessingSource::new).collect(Collectors.toList()));
            projectModule.get().compilationOutput.get().getInputs(jarSink);
            for (Path p : projectModule.get().getSrcDirs()) {
                new DirectoryProcessingSource(p).getInputs(jarSourcesSink);
            }
            projectModule.get().compilationResult.get().getOutputLocation(StandardLocation.SOURCE_OUTPUT, jarSourcesSink);
            jarSink.commit();
            jarSourcesSink.commit();
        }
        return new JavaJarDependency(outjar, outjar, getId());
    }
}
