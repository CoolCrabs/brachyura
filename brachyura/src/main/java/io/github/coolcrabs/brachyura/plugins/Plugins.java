package io.github.coolcrabs.brachyura.plugins;

import java.util.ArrayList;
import java.util.List;

import io.github.coolcrabs.brachyura.profiler.ProfilePlugin;

public class Plugins {
    private Plugins() { }

    static ArrayList<Plugin> plugins = new ArrayList<>();
    
    static {
        plugins.add(ProfilePlugin.INSTANCE); // TODO: real plugin loading
    }

    public static List<Plugin> getPlugins() {
        return new ArrayList<>(plugins);
    }
}
