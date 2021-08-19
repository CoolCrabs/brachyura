package io.github.coolcrabs.brachyura.ide;

import java.nio.file.Path;

public interface Ide {
    public static Ide[] getIdes() {
        return new Ide[] {
            Vscode.INSTANCE
        };
    }

    String ideName();
    void updateProject(Path projectDir, IdeProject ideProject);
}
