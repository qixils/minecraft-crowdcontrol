package dev.qixils.crowdcontrol.plugin.fabric.interfaces;

import dev.onyxstudios.cca.api.v3.entity.PlayerComponent;
import net.minecraft.world.level.GameType;
import org.jetbrains.annotations.Nullable;

public interface GameTypeEffectComponent extends PlayerComponent<GameTypeEffectComponent> {

	@Nullable GameType getValue();

	void setValue(@Nullable GameType value);
}
