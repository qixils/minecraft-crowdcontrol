package dev.qixils.crowdcontrol.plugin.fabric.interfaces;

import net.minecraft.world.level.GameType;
import org.jetbrains.annotations.Nullable;
import org.ladysnake.cca.api.v3.entity.RespawnableComponent;

public interface GameTypeEffectComponent extends RespawnableComponent<GameTypeEffectComponent> {

	@Nullable GameType getValue();

	void setValue(@Nullable GameType value);
}
