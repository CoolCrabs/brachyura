package io.github.coolcrabs.testmod;

import org.apache.logging.log4j.Logger;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.MinecraftClient;

public class TestModClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        System.out.println(MinecraftClient.getInstance().isDemo);
    }
    
}
