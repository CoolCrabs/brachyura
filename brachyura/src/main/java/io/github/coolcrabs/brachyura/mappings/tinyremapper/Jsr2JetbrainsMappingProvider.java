package io.github.coolcrabs.brachyura.mappings.tinyremapper;

import net.fabricmc.tinyremapper.IMappingProvider;

public enum Jsr2JetbrainsMappingProvider implements IMappingProvider {
    INSTANCE;

    @Override
    public void load(MappingAcceptor out) {
        out.acceptClass("javax/annotation/Nullable", "org/jetbrains/annotations/Nullable");
        out.acceptClass("javax/annotation/Nonnull", "org/jetbrains/annotations/NotNull");
        out.acceptClass("javax/annotation/concurrent/Immutable", "org/jetbrains/annotations/Unmodifiable");
    }
    
}
