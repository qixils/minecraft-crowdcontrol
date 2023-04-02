package dev.qixils.crowdcontrol.plugin.fabric.interfaces.impl;

import dev.qixils.crowdcontrol.plugin.fabric.interfaces.OriginalDisplayName;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OriginalDisplayNameImpl implements OriginalDisplayName {

	private @Nullable Text value = null;

	@Override
	public @Nullable Text getValue() {
		return value;
	}

	@Override
	public void setValue(@Nullable Text value) {
		this.value = value == null ? null : value.copy();
	}

	@Override
	public void writeToNbt(@NotNull NbtCompound tag) {
		if (value != null) {
			tag.putString("value", Text.Serializer.toJson(value));
		} else {
			tag.remove("value");
		}
	}
	
	@Override
	public void readFromNbt(@NotNull NbtCompound tag) {
		if (tag.contains("value")) {
			value = Text.Serializer.fromJson(tag.getString("value"));
		}
	}
}
