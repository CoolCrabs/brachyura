package io.github.coolcrabs.brachyura.plugins;

public interface Plugin {
    /**
     * Called before compilation of buildscript
     */
    default void onEntry() { }

    /**
     * Called once requested task has been completed or errors
     */
    default void onExit() { }
}
