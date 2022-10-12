package dev.qixils.crowdcontrol.plugin.fabric.interfaces.impl;

import dev.qixils.crowdcontrol.plugin.fabric.interfaces.ViewerMob;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;

public class ViewerMobImpl implements ViewerMob {
	private boolean value = false;

	@Override
	public boolean isViewerSpawned() {
		return value;
	}

	@Override
	public void setViewerSpawned() {
		value = true;
	}

	@Override
	public void writeToNbt(@NotNull CompoundTag tag) {
		tag.putBoolean("value", value);
	}

	@Override
	public void readFromNbt(@NotNull CompoundTag tag) {
		if (tag.contains("value")) {
			value = tag.getBoolean("value");
		}
	}
}
