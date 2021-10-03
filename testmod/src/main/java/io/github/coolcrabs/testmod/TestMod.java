package io.github.coolcrabs.testmod;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.GameRules.Category;

public class TestMod implements ModInitializer {
    @Override
    public void onInitialize() {
        Registry.register(Registry.ITEM, new Identifier("brachyuratestmod", "epic"), new Item(new Item.Settings()));
        GameRuleRegistry.register("gradleisgood", Category.MISC, GameRuleFactory.createBooleanRule(false, (mc, gr) -> gr.set(false, mc)));
    }
}
