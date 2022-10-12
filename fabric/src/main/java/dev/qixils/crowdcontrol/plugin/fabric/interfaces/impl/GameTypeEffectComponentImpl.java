package dev.qixils.crowdcontrol.plugin.fabric.interfaces.impl;

import dev.qixils.crowdcontrol.plugin.fabric.interfaces.GameTypeEffectComponent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.GameType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GameTypeEffectComponentImpl implements GameTypeEffectComponent {
	private @Nullable GameType value = null;

	@Override
	public @Nullable GameType getValue() {
		return value;
	}

	@Override
	public void setValue(@Nullable GameType value) {
		this.value = value;
	}

	@Override
	public void writeToNbt(@NotNull CompoundTag tag) {
		if (value != null) {
			tag.putString("value", value.getName());
		} else {
			tag.remove("value");
		}
	}

	@Override
	public void readFromNbt(CompoundTag tag) {
		if (tag.contains("value")) {
			value = GameType.byName(tag.getString("value"));
		}
	}

	@Override
	public boolean shouldCopyForRespawn(boolean lossless, boolean keepInventory, boolean sameCharacter) {
		return true;
	}
}
