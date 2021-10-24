package io.github.coolcrabs.brachyura.project;

import java.nio.file.Paths;
import java.util.List;

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
            Tasks t = new Tasks();
            Project buildscript = (Project) Class.forName("Buildscript").getDeclaredConstructor().newInstance();
            buildscript.getTasks(t);
            Task task = t.get(args[1]);
            task.doTask(new String[]{});
        } finally {
            for (Plugin plugin : plugins) {
                plugin.onExit();
            }
        }
    }
}
