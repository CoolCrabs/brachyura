package io.github.coolcrabs.testmod;

import net.fabricmc.api.ModInitializer;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class TestMod implements ModInitializer {
    @Override
    public void onInitialize() {
        Registry.register(Registry.ITEM, new Identifier("brachyuratestmod", "epic"), new Item(new Item.Settings()));
    }
}
