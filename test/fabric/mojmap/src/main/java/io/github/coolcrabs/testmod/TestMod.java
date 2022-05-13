package io.github.coolcrabs.testmod;

import java.util.Objects;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.GameRules.Category;

public class TestMod implements ModInitializer {
    @Override
    public void onInitialize() {
        Registry.register(Registry.ITEM, new ResourceLocation("brachyuratestmod", "epic"), new Item(new Item.Properties()));
        GameRuleRegistry.register("gradleisgood", Category.MISC, GameRuleFactory.createBooleanRule(false, (mc, gr) -> {
            if (gr.get()) gr.set(false, mc);
        }));
    }

    void thonk() {
        Projectile e = new Projectile(null, null) {
            @Override
            protected void defineSynchedData() {
                //noop
            }
            
        };
        Objects.requireNonNull(e);
    }
}
