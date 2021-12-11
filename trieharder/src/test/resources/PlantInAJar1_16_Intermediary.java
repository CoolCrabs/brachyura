package io.github.coolmineman.plantinajar;

import io.github.coolmineman.plantinajar.config.AutoConfigurater;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.minecraft.class_1747;
import net.minecraft.class_1761;
import net.minecraft.class_1792;
import net.minecraft.class_2246;
import net.minecraft.class_2248;
import net.minecraft.class_2378;
import net.minecraft.class_2591;
import net.minecraft.class_2960;
import net.minecraft.class_3914;
import net.minecraft.class_3917;

public class PlantInAJar implements ModInitializer {

	public static final AutoConfigurater CONFIG;

	static {
		AutoConfig.register(AutoConfigurater.class, GsonConfigSerializer::new);
		CONFIG = AutoConfig.getConfigHolder(AutoConfigurater.class).getConfig();
	}

	public static final class_2248 PLANT_JAR = new JarBlock(FabricBlockSettings.copyOf(class_2246.field_10033));
	public static class_2591<JarBlockEntity> PLANT_JAR_ENTITY;
	public static class_3917<JarGuiDescription> EPIC_SCREEN_HAND_YES;
	

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.
		class_2378.method_10230(class_2378.field_11146, new class_2960("plantinajar", "plant_jar"), PLANT_JAR);
		class_2378.method_10230(class_2378.field_11142, new class_2960("plantinajar", "plant_jar"), new class_1747(PLANT_JAR, new class_1792.class_1793().method_7892(class_1761.field_7932)));
		PLANT_JAR_ENTITY = class_2378.method_10226(class_2378.field_11137, "plantinajar:plant_jar", class_2591.class_2592.method_20528(JarBlockEntity::new, PLANT_JAR).method_11034(null));
		EPIC_SCREEN_HAND_YES = ScreenHandlerRegistry.registerSimple(new class_2960("plantinajar", "plant_jar"), (syncId, inventory) -> new JarGuiDescription(syncId, inventory, class_3914.field_17304));
		System.out.println("You Can Put Your Plants In Jars Now!");
		
	}
}
