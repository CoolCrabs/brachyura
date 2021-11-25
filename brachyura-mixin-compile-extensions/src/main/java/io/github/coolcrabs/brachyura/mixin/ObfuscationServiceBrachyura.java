package io.github.coolcrabs.brachyura.mixin;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.spongepowered.tools.obfuscation.interfaces.IMixinAnnotationProcessor;
import org.spongepowered.tools.obfuscation.service.IObfuscationService;
import org.spongepowered.tools.obfuscation.service.ObfuscationTypeDescriptor;

public class ObfuscationServiceBrachyura implements IObfuscationService {
    public static final String IN_MAP_FILE = "brachyuraInMap";
    public static final String OUT_MAP_FILE = "brachyuraOutMap";
    public static final String IN_NAMESPACE = "brachyuraInNamespace";
    public static final String OUT_NAMESPACE = "brachyuraOutNamespace";

    @Override
    public Set<String> getSupportedOptions() {
        HashSet<String> options = new HashSet<>();
        options.add(IN_MAP_FILE);
        options.add(OUT_MAP_FILE);
        options.add(IN_NAMESPACE);
        options.add(OUT_NAMESPACE);
        return options;
    }

    @Override
    public Collection<ObfuscationTypeDescriptor> getObfuscationTypes(IMixinAnnotationProcessor ap) {
        return getObfuscationTypes();
    }

    // Mixin 0.7
    public Collection<ObfuscationTypeDescriptor> getObfuscationTypes() {
        return Collections.singletonList(new ObfuscationTypeDescriptor("brachyura", IN_MAP_FILE, OUT_MAP_FILE, ObfuscationEnvironmentBrachyura.class));
    }
}
