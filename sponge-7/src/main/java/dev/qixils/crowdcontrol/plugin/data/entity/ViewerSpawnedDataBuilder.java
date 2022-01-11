package dev.qixils.crowdcontrol.plugin.data.entity;

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
	public ViewerSpawnedData create() {
		return new ViewerSpawnedData();
	}

	@Override
	public Optional<ViewerSpawnedData> createFrom(DataHolder dataHolder) {
		return create().fill(dataHolder);
	}

	@Override
	protected Optional<ViewerSpawnedData> buildContent(DataView container) throws InvalidDataException {
		if (container.contains(VIEWER_SPAWNED)) {
			//noinspection OptionalGetWithoutIsPresent -- check is implied by the if statement
			return Optional.of(new ViewerSpawnedData(container.getBoolean(VIEWER_SPAWNED.getQuery()).get()));
		}
		return Optional.empty();
	}
}
