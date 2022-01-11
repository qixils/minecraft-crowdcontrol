package dev.qixils.crowdcontrol.plugin.data.entity;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.manipulator.mutable.common.AbstractBooleanData;
import org.spongepowered.api.data.merge.MergeFunction;
import org.spongepowered.api.data.value.mutable.Value;

import java.util.Optional;

import static dev.qixils.crowdcontrol.plugin.SpongeCrowdControlPlugin.VIEWER_SPAWNED;

public class ViewerSpawnedData extends AbstractBooleanData
		<ViewerSpawnedData, ImmutableViewerSpawnedData> {

	static final int CONTENT_VERSION = 1;

	public ViewerSpawnedData(boolean value) {
		super(VIEWER_SPAWNED, value, false);
	}

	public ViewerSpawnedData() {
		this(false);
	}

	public Value<Boolean> viewerSpawned() {
		return getValueGetter();
	}

	@Override
	public @NotNull DataContainer toContainer() {
		return super.toContainer().set(VIEWER_SPAWNED, getValue());
	}

	@Override
	public @NotNull Optional<ViewerSpawnedData> fill(@NotNull DataHolder dataHolder, @NotNull MergeFunction overlap) {
		ViewerSpawnedData merged = overlap.merge(this, dataHolder.get(ViewerSpawnedData.class).orElse(null));
		setValue(merged.viewerSpawned().get());

		return Optional.of(this);
	}

	@Override
	public @NotNull Optional<ViewerSpawnedData> from(DataContainer container) {
		if (container.contains(VIEWER_SPAWNED)) {
			//noinspection OptionalGetWithoutIsPresent -- check is implied by the if statement
			return Optional.of(setValue(container.getBoolean(VIEWER_SPAWNED.getQuery()).get()));
		}
		return Optional.empty();
	}

	@Override
	public @NotNull ViewerSpawnedData copy() {
		return new ViewerSpawnedData(value);
	}

	@Override
	public @NotNull ImmutableViewerSpawnedData asImmutable() {
		return new ImmutableViewerSpawnedData(value);
	}

	@Override
	public int getContentVersion() {
		return CONTENT_VERSION;
	}
}
