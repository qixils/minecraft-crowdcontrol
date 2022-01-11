package dev.qixils.crowdcontrol.plugin.data.entity;

import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.manipulator.mutable.common.AbstractSingleCatalogData;
import org.spongepowered.api.data.merge.MergeFunction;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.entity.living.player.gamemode.GameMode;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;

import java.util.Optional;

import static dev.qixils.crowdcontrol.plugin.SpongeCrowdControlPlugin.GAME_MODE_EFFECT;

public class GameModeEffectData extends AbstractSingleCatalogData
		<GameMode, GameModeEffectData, ImmutableGameModeEffectData> {

	static final int CONTENT_VERSION = 1;
	static final GameMode DEFAULT = GameModes.SURVIVAL;

	public GameModeEffectData(GameMode value) {
		super(
				GAME_MODE_EFFECT,
				value,
				DEFAULT
		);
	}

	public GameModeEffectData() {
		this(DEFAULT);
	}

	public Value<GameMode> viewerSpawned() {
		return type();
	}

	@Override
	public DataContainer toContainer() {
		return super.toContainer().set(GAME_MODE_EFFECT, getValue());
	}

	@Override
	public Optional<GameModeEffectData> fill(DataHolder dataHolder, MergeFunction overlap) {
		GameModeEffectData merged = overlap.merge(this, dataHolder.get(GameModeEffectData.class).orElse(null));
		setValue(merged.viewerSpawned().get());
		return Optional.of(this);
	}

	@Override
	public Optional<GameModeEffectData> from(DataContainer container) {
		if (container.contains(GAME_MODE_EFFECT)) {
			//noinspection OptionalGetWithoutIsPresent -- check is implied by the if statement
			return Optional.of(setValue(container.getCatalogType(GAME_MODE_EFFECT.getQuery(), GameMode.class).get()));
		}
		return Optional.empty();
	}

	@Override
	public GameModeEffectData copy() {
		return new GameModeEffectData(getValue());
	}

	@Override
	public ImmutableGameModeEffectData asImmutable() {
		return new ImmutableGameModeEffectData(getValue());
	}

	@Override
	public int getContentVersion() {
		return CONTENT_VERSION;
	}
}
