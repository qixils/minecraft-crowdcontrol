package dev.qixils.crowdcontrol.plugin.data.entity;

import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.manipulator.DataManipulatorBuilder;
import org.spongepowered.api.data.persistence.AbstractDataBuilder;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.entity.living.player.gamemode.GameMode;

import java.util.Optional;

import static dev.qixils.crowdcontrol.plugin.SpongeCrowdControlPlugin.GAME_MODE_EFFECT;

public class GameModeEffectDataBuilder extends AbstractDataBuilder<GameModeEffectData>
		implements DataManipulatorBuilder<GameModeEffectData, ImmutableGameModeEffectData> {

	public GameModeEffectDataBuilder() {
		super(GameModeEffectData.class, GameModeEffectData.CONTENT_VERSION);
	}

	@Override
	public GameModeEffectData create() {
		return new GameModeEffectData();
	}

	@Override
	public Optional<GameModeEffectData> createFrom(DataHolder dataHolder) {
		return create().fill(dataHolder);
	}

	@Override
	protected Optional<GameModeEffectData> buildContent(DataView container) throws InvalidDataException {
		if (container.contains(GAME_MODE_EFFECT)) {
			//noinspection OptionalGetWithoutIsPresent -- check is implied by the if statement
			return Optional.of(new GameModeEffectData(container.getCatalogType(GAME_MODE_EFFECT.getQuery(), GameMode.class).get()));
		}
		return Optional.empty();
	}
}
