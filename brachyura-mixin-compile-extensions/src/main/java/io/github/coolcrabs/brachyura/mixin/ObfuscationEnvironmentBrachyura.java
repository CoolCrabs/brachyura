package io.github.coolcrabs.brachyura.mixin;


import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;

import org.spongepowered.tools.obfuscation.ObfuscationEnvironment;
import org.spongepowered.tools.obfuscation.ObfuscationType;
import org.spongepowered.tools.obfuscation.mapping.IMappingProvider;
import org.spongepowered.tools.obfuscation.mapping.IMappingWriter;

class ObfuscationEnvironmentBrachyura extends ObfuscationEnvironment {
    protected ObfuscationEnvironmentBrachyura(ObfuscationType type) {
        super(type);
    }

    @Override
    protected IMappingProvider getMappingProvider(Messager messager, Filer filer) {
        return new BrachyuraMappingProvider(ap.getOption(ObfuscationServiceBrachyura.IN_NAMESPACE), ap.getOption(ObfuscationServiceBrachyura.OUT_NAMESPACE), filer);
    }

    @Override
    protected IMappingWriter getMappingWriter(Messager messager, Filer filer) {
        return new BrachyuraMappingWriter(ap.getOption(ObfuscationServiceBrachyura.IN_NAMESPACE), ap.getOption(ObfuscationServiceBrachyura.OUT_NAMESPACE), filer);
    }
}
