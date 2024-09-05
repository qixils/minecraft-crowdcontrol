package dev.qixils.crowdcontrol.plugin.fabric.interfaces;

import net.minecraft.world.level.GameType;
import org.jetbrains.annotations.Nullable;

public interface GameTypeEffectComponent {

	@Nullable
	default GameType cc$getGameTypeEffect() {
		return null;
	}

	default void cc$setGameTypeEffect(@Nullable GameType value) {

	}
}
