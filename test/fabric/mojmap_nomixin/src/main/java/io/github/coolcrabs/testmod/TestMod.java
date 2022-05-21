package io.github.coolcrabs.testmod;

import net.fabricmc.api.ModInitializer;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.GameRules.Category;

public class TestMod implements ModInitializer {
    @Override
    public void onInitialize() {
        Registry.register(Registry.ITEM, new ResourceLocation("brachyuratestmod", "epic"), new Item(new Item.Properties()));
    }
}
