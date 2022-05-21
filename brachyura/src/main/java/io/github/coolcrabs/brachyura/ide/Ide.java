package io.github.coolcrabs.brachyura.ide;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;

import io.github.coolcrabs.brachyura.ide.IdeModule.RunConfig;

public interface Ide {
    public static Ide[] getIdes() {
        return new Ide[] {
            Netbeans.INSTANCE,
            Intellijank.INSTANCE,
            Eclipse.INSTANCE
        };
    }

    static void validate(IdeModule... ideModules) {
        HashSet<IdeModule> modules = new HashSet<>(Arrays.asList(ideModules));
        HashSet<String> names = new HashSet<>();
        for (IdeModule m0 : modules) {
            if (!names.add(m0.name)) throw new IllegalArgumentException("Duplicate modules for name " + m0.name);
            for (IdeModule m1 : m0.dependencyModules) {
                if (!modules.contains(m1)) throw new IllegalArgumentException("Module " + m0.name + " references module " + m1.name + " not in ide project as a dependency");
            }
            for (RunConfig rc : m0.runConfigs) {
                for (IdeModule m1 : rc.additionalModulesClasspath) {
                    if (!modules.contains(m1)) throw new IllegalArgumentException("Module " + m0.name + " references module " + m1.name + " not in ide project in a run config");
                }
            }
        }
    } 

    String ideName();
    void updateProject(Path projectRoot, IdeModule... ideModules);
}
