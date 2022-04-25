package io.github.coolcrabs.testmod.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.RunArgs;

@Mixin(MinecraftClient.class)
public class MinecraftMixin {
    @Inject(at = @At("TAIL"), method = "<init>")
    void testmod_onConstruct(RunArgs ra, CallbackInfo cb) {
        System.out.println("We do a little trolling!");
        System.out.println(MinecraftClient.getInstance().isDemo); // aw field
    }
}
