package dev.qixils.crowdcontrol.plugin.fabric.interfaces.impl;

import dev.qixils.crowdcontrol.plugin.fabric.interfaces.JumpStatus;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public class JumpStatusImpl implements JumpStatus {
	private final @NotNull Player provider;
	private boolean prohibited = false;

	public JumpStatusImpl(@NotNull Player provider) {
		this.provider = provider;
	}

	@Override
	public boolean isProhibited() {
		return prohibited;
	}

	@Override
	public void setProhibited(boolean prohibited) {
		this.prohibited = prohibited;
	}

	@Override
	public void writeToNbt(CompoundTag tag) {
		tag.putBoolean("prohibited", prohibited);
	}

	@Override
	public void readFromNbt(CompoundTag tag) {
		if (tag.contains("prohibited"))
			prohibited = tag.getBoolean("prohibited");
	}

	@Override
	public boolean shouldSyncWith(ServerPlayer player) {
		return this.provider == player;
	}
}
