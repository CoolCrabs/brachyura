package io.github.coolcrabs.brachyura.project;

import java.io.File;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import io.github.coolcrabs.brachyura.plugins.Plugin;
import io.github.coolcrabs.brachyura.plugins.Plugins;

class BuildscriptDevEntry {
    public static void main(String[] args) throws Throwable {
        List<Plugin> plugins = Plugins.getPlugins();
        for (Plugin plugin : plugins) {
            plugin.onEntry();
        }
        try {
            EntryGlobals.projectDir = Paths.get(args[0]);
            EntryGlobals.buildscriptClasspath = Arrays.stream(args[1].split(File.pathSeparator)).map(Paths::get).collect(Collectors.toList());
            Project buildscript = (Project) Class.forName("Buildscript").getDeclaredConstructor().newInstance();
            BuildscriptProject buildscriptProject = new BuildscriptProject() {
                @Override
                public Optional<Project> createProject() {
                    return Optional.of(buildscript);
                }
            };
            buildscript.setIdeProject(buildscriptProject);
            Tasks t = new Tasks();
            buildscript.getTasks(t);
            Task task = t.get(args[2]);
            task.doTask(new String[]{});
        } finally {
            for (Plugin plugin : plugins) {
                plugin.onExit();
            }
        }
    }
}
