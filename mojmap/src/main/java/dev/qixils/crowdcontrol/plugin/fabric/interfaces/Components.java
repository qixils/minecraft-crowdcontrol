package dev.qixils.crowdcontrol.plugin.fabric.interfaces;

import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public final class Components {
	public static final @NotNull String VIEWER_MOB = new ResourceLocation("crowdcontrol", "viewer-mob").asString();
	public static final @NotNull String ORIGINAL_DISPLAY_NAME = new ResourceLocation("crowdcontrol", "original-display-name").asString();
	public static final @NotNull String GAME_TYPE_EFFECT = new ResourceLocation("crowdcontrol", "game-type-effect").asString();
}
