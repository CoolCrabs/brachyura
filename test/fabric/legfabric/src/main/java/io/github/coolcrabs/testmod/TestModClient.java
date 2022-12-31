package io.github.coolcrabs.testmod;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.MinecraftClient;

public class TestModClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        System.out.println(MinecraftClient.getInstance().isDemo);
    }
    
}
