package dev.qixils.crowdcontrol.plugin.fabric.interfaces;

import org.jetbrains.annotations.NotNull;

import static net.minecraft.resources.ResourceLocation.fromNamespaceAndPath;

public final class Components {
	public static final @NotNull String VIEWER_MOB = fromNamespaceAndPath("crowdcontrol", "viewer-mob").asString();
	public static final @NotNull String ORIGINAL_DISPLAY_NAME = fromNamespaceAndPath("crowdcontrol", "original-display-name").asString();
	public static final @NotNull String GAME_TYPE_EFFECT = fromNamespaceAndPath("crowdcontrol", "game-type-effect").asString();
}
