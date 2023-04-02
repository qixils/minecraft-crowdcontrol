package dev.qixils.crowdcontrol.plugin.fabric.interfaces;

import dev.onyxstudios.cca.api.v3.entity.PlayerComponent;
import net.minecraft.world.GameMode;
import org.jetbrains.annotations.Nullable;

public interface GameTypeEffectComponent extends PlayerComponent<GameTypeEffectComponent> {

	@Nullable GameMode getValue();

	void setValue(@Nullable GameMode value);
}
