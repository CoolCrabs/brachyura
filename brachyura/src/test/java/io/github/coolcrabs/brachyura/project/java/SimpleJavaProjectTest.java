package io.github.coolcrabs.brachyura.project.java;

import io.github.coolcrabs.brachyura.maven.MavenId;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import io.github.coolcrabs.brachyura.util.PathUtil;
import org.junit.jupiter.api.Assertions;

class SimpleJavaProjectTest {
    @Test
    void compile() {
        SimpleJavaProject project = new SimpleJavaProject() {
            @Override
            public MavenId getId() {
                return new MavenId("io.github.coolcrabs", "testprogram", "0.0");
            }

            @Override
            public int getJavaVersion() {
                return 8;
            }

            @Override
            public Path getProjectDir() {
                return PathUtil.CWD.getParent().resolve("testprogram");
            }
        };
        //Todo better api for this?
        project.getTasks(p -> {
            if (p.name.equals("vscode")) p.doTask(new String[]{});
            if (p.name.equals("netbeans")) p.doTask(new String[]{});
        });
        Assertions.assertNotNull(project.build());
        project.getTasks(p -> {
            if (p.name.equals("publishToMavenLocal")) p.doTask(new String[]{});
        });
    }
}
