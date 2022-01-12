package dev.qixils.crowdcontrol.plugin.sponge7.data.entity;

import dev.qixils.crowdcontrol.plugin.sponge7.SpongeCrowdControlPlugin;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.manipulator.immutable.common.AbstractImmutableBooleanData;
import org.spongepowered.api.data.value.immutable.ImmutableValue;

public class ImmutableViewerSpawnedData extends AbstractImmutableBooleanData
		<ImmutableViewerSpawnedData, ViewerSpawnedData> {

	public ImmutableViewerSpawnedData(boolean value) {
		super(SpongeCrowdControlPlugin.VIEWER_SPAWNED, value, false);
	}

	public ImmutableViewerSpawnedData() {
		this(false);
	}

	public ImmutableValue<Boolean> viewerSpawned() {
		return getValueGetter();
	}

	@Override
	public DataContainer toContainer() {
		return super.toContainer().set(SpongeCrowdControlPlugin.VIEWER_SPAWNED, getValue());
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
