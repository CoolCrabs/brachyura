package io.github.coolcrabs.brachyura.project;

import java.nio.file.Paths;

class BuildscriptDevEntry {
    public static void main(String[] args) throws Throwable {
        EntryGlobals.projectDir = Paths.get(args[0]);
        Tasks t = new Tasks();
        Project buildscript = (Project) Class.forName("Buildscript").getDeclaredConstructor().newInstance();
        buildscript.getTasks(t);
        Task task = t.get(args[1]);
        task.doTask(new String[]{});
    }
}
