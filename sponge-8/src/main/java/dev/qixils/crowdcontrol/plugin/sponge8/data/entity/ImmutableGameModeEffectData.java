package dev.qixils.crowdcontrol.plugin.sponge8.data.entity;

import dev.qixils.crowdcontrol.plugin.sponge8.SpongeCrowdControlPlugin;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.manipulator.immutable.common.AbstractImmutableSingleCatalogData;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.entity.living.player.gamemode.GameMode;

public class ImmutableGameModeEffectData extends AbstractImmutableSingleCatalogData
		<GameMode, ImmutableGameModeEffectData, GameModeEffectData> {

	public ImmutableGameModeEffectData(GameMode value) {
		super(
				SpongeCrowdControlPlugin.GAME_MODE_EFFECT,
				value,
				GameModeEffectData.DEFAULT
		);
	}

	public ImmutableGameModeEffectData() {
		this(GameModeEffectData.DEFAULT);
	}

	public ImmutableValue<GameMode> viewerSpawned() {
		return type();
	}

	@Override
	public DataContainer toContainer() {
		return super.toContainer().set(SpongeCrowdControlPlugin.GAME_MODE_EFFECT, getValue());
	}

	@Override
	public GameModeEffectData asMutable() {
		return new GameModeEffectData(getValue());
	}

	@Override
	public int getContentVersion() {
		return GameModeEffectData.CONTENT_VERSION;
	}
}
