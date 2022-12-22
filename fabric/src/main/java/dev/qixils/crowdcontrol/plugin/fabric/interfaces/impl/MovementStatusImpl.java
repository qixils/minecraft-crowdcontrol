package dev.qixils.crowdcontrol.plugin.fabric.interfaces.impl;

import dev.qixils.crowdcontrol.plugin.fabric.interfaces.Components;
import dev.qixils.crowdcontrol.plugin.fabric.interfaces.MovementStatus;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;
import java.util.Locale;

public class MovementStatusImpl implements MovementStatus {
	private final @NotNull Player provider;
	private final @NotNull EnumSet<Type> prohibited = EnumSet.noneOf(Type.class);

	public MovementStatusImpl(@NotNull Player provider) {
		this.provider = provider;
	}

	private void sync() {
		Components.MOVEMENT_STATUS.sync(provider);
	}

	@Override
	public boolean isProhibited(@NotNull Type type) {
		return prohibited.contains(type);
	}

	private void rawSetProhibited(@NotNull Type type, boolean prohibited) {
		if (prohibited)
			this.prohibited.add(type);
		else
			this.prohibited.remove(type);
	}

	@Override
	public void setProhibited(@NotNull Type type, boolean prohibited) {
		rawSetProhibited(type, prohibited);
		sync();
	}

	private static String getTypeKey(@NotNull Type type) {
		return type.name().toLowerCase(Locale.ENGLISH).replace('_', '-') + "-prohibited";
	}

	@Override
	public void writeToNbt(@NotNull CompoundTag tag) {
		for (Type type : Type.values())
			tag.putBoolean(getTypeKey(type), isProhibited(type));
	}

	@Override
	public void readFromNbt(@NotNull CompoundTag tag) {
		for (Type type : Type.values()) {
			String key = getTypeKey(type);
			if (tag.contains(key))
				rawSetProhibited(type, tag.getBoolean(key));
		}
		sync();
	}

	@Override
	public boolean shouldSyncWith(ServerPlayer player) {
		return this.provider == player;
	}
}
