package dev.qixils.crowdcontrol.plugin.data.entity;

import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.manipulator.immutable.common.AbstractImmutableSingleCatalogData;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.entity.living.player.gamemode.GameMode;

import static dev.qixils.crowdcontrol.plugin.SpongeCrowdControlPlugin.GAME_MODE_EFFECT;
import static dev.qixils.crowdcontrol.plugin.data.entity.GameModeEffectData.CONTENT_VERSION;
import static dev.qixils.crowdcontrol.plugin.data.entity.GameModeEffectData.DEFAULT;

public class ImmutableGameModeEffectData extends AbstractImmutableSingleCatalogData
		<GameMode, ImmutableGameModeEffectData, GameModeEffectData> {

	public ImmutableGameModeEffectData(GameMode value) {
		super(
				GAME_MODE_EFFECT,
				value,
				DEFAULT
		);
	}

	public ImmutableGameModeEffectData() {
		this(DEFAULT);
	}

	public ImmutableValue<GameMode> viewerSpawned() {
		return type();
	}

	@Override
	public DataContainer toContainer() {
		return super.toContainer().set(GAME_MODE_EFFECT, getValue());
	}

	@Override
	public GameModeEffectData asMutable() {
		return new GameModeEffectData(getValue());
	}

	@Override
	public int getContentVersion() {
		return CONTENT_VERSION;
	}
}
