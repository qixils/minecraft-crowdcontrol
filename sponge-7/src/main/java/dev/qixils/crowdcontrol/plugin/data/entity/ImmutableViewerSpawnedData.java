package dev.qixils.crowdcontrol.plugin.data.entity;

import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.manipulator.immutable.common.AbstractImmutableBooleanData;
import org.spongepowered.api.data.value.immutable.ImmutableValue;

import static dev.qixils.crowdcontrol.plugin.SpongeCrowdControlPlugin.VIEWER_SPAWNED;

public class ImmutableViewerSpawnedData extends AbstractImmutableBooleanData
		<ImmutableViewerSpawnedData, ViewerSpawnedData> {

	public ImmutableViewerSpawnedData(boolean value) {
		super(VIEWER_SPAWNED, value, false);
	}

	public ImmutableViewerSpawnedData() {
		this(false);
	}

	public ImmutableValue<Boolean> viewerSpawned() {
		return getValueGetter();
	}

	@Override
	public DataContainer toContainer() {
		return super.toContainer().set(VIEWER_SPAWNED, getValue());
	}

	@Override
	public ViewerSpawnedData asMutable() {
		return new ViewerSpawnedData(getValue());
	}

	@Override
	public int getContentVersion() {
		return ViewerSpawnedData.CONTENT_VERSION;
	}
}
