package dev.qixils.crowdcontrol.plugin.sponge8.data.entity;

import dev.qixils.crowdcontrol.plugin.sponge8.SpongeCrowdControlPlugin;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.manipulator.mutable.common.AbstractSingleCatalogData;
import org.spongepowered.api.data.merge.MergeFunction;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.entity.living.player.gamemode.GameMode;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;

import java.util.Optional;

public class GameModeEffectData extends AbstractSingleCatalogData
		<GameMode, GameModeEffectData, ImmutableGameModeEffectData> {

	static final int CONTENT_VERSION = 1;
	static final GameMode DEFAULT = GameModes.SURVIVAL;

	public GameModeEffectData(GameMode value) {
		super(
				SpongeCrowdControlPlugin.GAME_MODE_EFFECT,
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
		return super.toContainer().set(SpongeCrowdControlPlugin.GAME_MODE_EFFECT, getValue());
	}

	@Override
	public Optional<GameModeEffectData> fill(DataHolder dataHolder, MergeFunction overlap) {
		GameModeEffectData merged = overlap.merge(this, dataHolder.get(GameModeEffectData.class).orElse(null));
		setValue(merged.viewerSpawned().get());
		return Optional.of(this);
	}

	@Override
	public Optional<GameModeEffectData> from(DataContainer container) {
		if (container.contains(SpongeCrowdControlPlugin.GAME_MODE_EFFECT)) {
			//noinspection OptionalGetWithoutIsPresent -- check is implied by the if statement
			return Optional.of(setValue(container.getCatalogType(SpongeCrowdControlPlugin.GAME_MODE_EFFECT.getQuery(), GameMode.class).get()));
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
