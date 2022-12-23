package dev.qixils.crowdcontrol.plugin.fabric.interfaces.impl;

import dev.qixils.crowdcontrol.plugin.fabric.interfaces.Components;
import dev.qixils.crowdcontrol.plugin.fabric.interfaces.MovementStatus;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.EnumSet;
import java.util.Locale;

public class MovementStatusImpl implements MovementStatus {
	private static final Logger logger = LoggerFactory.getLogger("MovementStatus");
	private final @NotNull Player provider;
	private final @NotNull EnumSet<Type> prohibited = EnumSet.noneOf(Type.class);
	private final @NotNull EnumSet<Type> inverted = EnumSet.noneOf(Type.class);

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

	@Override
	public boolean isInverted(@NotNull Type type) {
		return inverted.contains(type);
	}

	private void rawSetProhibited(@NotNull Type type, boolean prohibited) {
		if (prohibited)
			this.prohibited.add(type);
		else
			this.prohibited.remove(type);
	}

	private void rawSetInverted(@NotNull Type type, boolean inverted) {
		if (inverted)
			this.inverted.add(type);
		else
			this.inverted.remove(type);
	}

	@Override
	public void setProhibited(@NotNull Type type, boolean prohibited) {
		rawSetProhibited(type, prohibited);
		sync();
	}

	@Override
	public void setInverted(@NotNull Type type, boolean inverted) {
		if (!type.canInvert())
			throw new IllegalArgumentException("Type " + type + " cannot be inverted");
		rawSetInverted(type, inverted);
		sync();
	}

	private static String getTypeKey(@NotNull Type type) {
		return type.name().toLowerCase(Locale.ENGLISH).replace('_', '-');
	}

	@Override
	public void writeToNbt(@NotNull CompoundTag tag) {
		for (Type type : Type.values()) {
			String key = getTypeKey(type);
			tag.putBoolean(key + "-prohibited", isProhibited(type));
			if (type.canInvert())
				tag.putBoolean(key + "-inverted", isInverted(type));
		}
	}

	@Override
	public void readFromNbt(@NotNull CompoundTag tag) {
		for (Type type : Type.values()) {
			String key = getTypeKey(type);
			if (tag.contains(key + "-prohibited"))
				rawSetProhibited(type, tag.getBoolean(key + "-prohibited"));
			if (tag.contains(key + "-inverted")) {
				if (!type.canInvert())
					logger.warn("Type {} cannot be inverted, but {}-inverted is set to {} for {}",
							type, key, tag.getBoolean(key + "-inverted"), provider.getStringUUID());
				else
					rawSetInverted(type, tag.getBoolean(key + "-inverted"));
			}
		}
	}

	@Override
	public boolean shouldSyncWith(ServerPlayer player) {
		return this.provider == player;
	}
}
