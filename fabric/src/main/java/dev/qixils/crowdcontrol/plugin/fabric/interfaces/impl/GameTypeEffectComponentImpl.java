package dev.qixils.crowdcontrol.plugin.fabric.interfaces.impl;

import dev.qixils.crowdcontrol.plugin.fabric.interfaces.GameTypeEffectComponent;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.GameMode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GameTypeEffectComponentImpl implements GameTypeEffectComponent {
	private @Nullable GameMode value = null;

	@Override
	public @Nullable GameMode getValue() {
		return value;
	}

	@Override
	public void setValue(@Nullable GameMode value) {
		this.value = value;
	}

	@Override
	public void writeToNbt(@NotNull NbtCompound tag) {
		if (value != null) {
			tag.putString("value", value.getName());
		} else {
			tag.remove("value");
		}
	}

	@Override
	public void readFromNbt(NbtCompound tag) {
		if (tag.contains("value")) {
			value = GameMode.byName(tag.getString("value"));
		}
	}

	@Override
	public boolean shouldCopyForRespawn(boolean lossless, boolean keepInventory, boolean sameCharacter) {
		return true;
	}
}
