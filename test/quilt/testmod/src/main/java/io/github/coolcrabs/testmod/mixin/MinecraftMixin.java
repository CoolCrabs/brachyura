package io.github.coolcrabs.testmod.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.Minecraft;
import net.minecraft.client.main.GameConfig;

@Mixin(Minecraft.class)
public class MinecraftMixin {
    @Inject(at = @At("TAIL"), method = "<init>")
    void testmod_onConstruct(GameConfig gc, CallbackInfo cb) {
        System.out.println("We do a little trolling!");
        System.out.println(Minecraft.getInstance().demo); // aw field
    }
}
