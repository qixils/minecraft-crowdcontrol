package dev.qixils.crowdcontrol.plugin.data.entity;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.manipulator.DataManipulatorBuilder;
import org.spongepowered.api.data.persistence.AbstractDataBuilder;
import org.spongepowered.api.data.persistence.InvalidDataException;

import java.util.Optional;

import static dev.qixils.crowdcontrol.plugin.SpongeCrowdControlPlugin.VIEWER_SPAWNED;

public class ViewerSpawnedDataBuilder extends AbstractDataBuilder<ViewerSpawnedData>
		implements DataManipulatorBuilder<ViewerSpawnedData, ImmutableViewerSpawnedData> {

	public ViewerSpawnedDataBuilder() {
		super(ViewerSpawnedData.class, ViewerSpawnedData.CONTENT_VERSION);
	}

	@Override
	public @NotNull ViewerSpawnedData create() {
		return new ViewerSpawnedData();
	}

	@Override
	public @NotNull Optional<ViewerSpawnedData> createFrom(@NotNull DataHolder dataHolder) {
		return create().fill(dataHolder);
	}

	@Override
	protected @NotNull Optional<ViewerSpawnedData> buildContent(@NotNull DataView container) throws InvalidDataException {
		if (container.contains(VIEWER_SPAWNED)) {
			//noinspection OptionalGetWithoutIsPresent -- check is implied by the if statement
			return Optional.of(new ViewerSpawnedData(container.getBoolean(VIEWER_SPAWNED.getQuery()).get()));
		}
		return Optional.empty();
	}
}
