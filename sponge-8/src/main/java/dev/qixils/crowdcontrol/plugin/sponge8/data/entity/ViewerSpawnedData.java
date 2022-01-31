package dev.qixils.crowdcontrol.plugin.sponge8.data.entity;

import dev.qixils.crowdcontrol.plugin.sponge8.SpongeCrowdControlPlugin;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.manipulator.mutable.common.AbstractBooleanData;
import org.spongepowered.api.data.merge.MergeFunction;
import org.spongepowered.api.data.value.mutable.Value;

import java.util.Optional;

public class ViewerSpawnedData extends AbstractBooleanData
		<ViewerSpawnedData, ImmutableViewerSpawnedData> {

	static final int CONTENT_VERSION = 1;

	public ViewerSpawnedData(boolean value) {
		super(SpongeCrowdControlPlugin.VIEWER_SPAWNED, value, false);
	}

	public ViewerSpawnedData() {
		this(false);
	}

	public Value<Boolean> viewerSpawned() {
		return getValueGetter();
	}

	@Override
	public DataContainer toContainer() {
		return super.toContainer().set(SpongeCrowdControlPlugin.VIEWER_SPAWNED, getValue());
	}

	@Override
	public Optional<ViewerSpawnedData> fill(DataHolder dataHolder, MergeFunction overlap) {
		ViewerSpawnedData merged = overlap.merge(this, dataHolder.get(ViewerSpawnedData.class).orElse(null));
		setValue(merged.viewerSpawned().get());
		return Optional.of(this);
	}

	@Override
	public Optional<ViewerSpawnedData> from(DataContainer container) {
		if (container.contains(SpongeCrowdControlPlugin.VIEWER_SPAWNED)) {
			//noinspection OptionalGetWithoutIsPresent -- check is implied by the if statement
			return Optional.of(setValue(container.getBoolean(SpongeCrowdControlPlugin.VIEWER_SPAWNED.getQuery()).get()));
		}
		return Optional.empty();
	}

	@Override
	public ViewerSpawnedData copy() {
		return new ViewerSpawnedData(getValue());
	}

	@Override
	public ImmutableViewerSpawnedData asImmutable() {
		return new ImmutableViewerSpawnedData(getValue());
	}

	@Override
	public int getContentVersion() {
		return CONTENT_VERSION;
	}
}
