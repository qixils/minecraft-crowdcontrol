package dev.qixils.crowdcontrol.plugin.fabric.interfaces.impl;

import dev.qixils.crowdcontrol.plugin.fabric.interfaces.OriginalDisplayName;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OriginalDisplayNameImpl implements OriginalDisplayName {

	private @Nullable Component value = null;

	@Override
	public @Nullable Component getValue() {
		return value;
	}

	@Override
	public void setValue(@Nullable Component value) {
		this.value = value == null ? null : value.copy();
	}

	@Override
	public void writeToNbt(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registryLookup) {
		if (value != null) {
			tag.putString("value", Component.Serializer.toJson(value, registryLookup));
		} else {
			tag.remove("value");
		}
	}

	@Override
	public void readFromNbt(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registryLookup) {
		if (tag.contains("value")) {
			value = Component.Serializer.fromJson(tag.getString("value"), registryLookup);
		}
	}
}
