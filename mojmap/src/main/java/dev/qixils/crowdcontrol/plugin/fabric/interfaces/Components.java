package dev.qixils.crowdcontrol.plugin.fabric.interfaces;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

public final class Components {
	public static final @NotNull String VIEWER_MOB = Key.key("crowdcontrol", "viewer-mob").asString();
	public static final @NotNull String ORIGINAL_DISPLAY_NAME = Key.key("crowdcontrol", "original-display-name").asString();
	public static final @NotNull String GAME_TYPE_EFFECT = Key.key("crowdcontrol", "game-type-effect").asString();
}
