package dev.qixils.crowdcontrol.plugin.sponge7.data.entity;

import dev.qixils.crowdcontrol.plugin.sponge7.SpongeCrowdControlPlugin;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.manipulator.immutable.common.AbstractImmutableSingleCatalogData;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.entity.living.player.gamemode.GameMode;

import static dev.qixils.crowdcontrol.plugin.sponge7.data.entity.GameModeEffectData.CONTENT_VERSION;
import static dev.qixils.crowdcontrol.plugin.sponge7.data.entity.GameModeEffectData.DEFAULT;

public class ImmutableGameModeEffectData extends AbstractImmutableSingleCatalogData
		<GameMode, ImmutableGameModeEffectData, GameModeEffectData> {

	public ImmutableGameModeEffectData(GameMode value) {
		super(
				SpongeCrowdControlPlugin.GAME_MODE_EFFECT,
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
		return super.toContainer().set(SpongeCrowdControlPlugin.GAME_MODE_EFFECT, getValue());
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
