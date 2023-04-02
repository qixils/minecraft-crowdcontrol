package dev.qixils.crowdcontrol.plugin.fabric.interfaces.impl;

import dev.qixils.crowdcontrol.plugin.fabric.interfaces.Components;
import dev.qixils.crowdcontrol.plugin.fabric.interfaces.MovementStatus;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.EnumMap;
import java.util.Locale;

public class MovementStatusImpl implements MovementStatus {
	private static final Logger logger = LoggerFactory.getLogger("MovementStatus");
	private final @NotNull PlayerEntity provider;
	private final @NotNull EnumMap<Type, Value> prohibited = new EnumMap<>(Type.class);

	public MovementStatusImpl(@NotNull PlayerEntity provider) {
		this.provider = provider;
	}

	public void sync() {
		Components.MOVEMENT_STATUS.sync(provider);
	}

	@Override
	public @NotNull Value get(@NotNull Type type) {
		return prohibited.getOrDefault(type, Value.ALLOWED);
	}

	@Override
	public void rawSet(@NotNull Type type, @NotNull Value value) {
		if (value == Value.ALLOWED)
			prohibited.remove(type);
		else if (value == Value.INVERTED && !type.canInvert())
			logger.warn("Attempted to set inverted value for type {} which cannot be inverted", type);
		else
			prohibited.put(type, value);
	}

	@Override
	public void set(@NotNull Type type, @NotNull Value value) {
		rawSet(type, value);
		sync();
	}

	private static String getTypeKey(@NotNull Type type) {
		return type.name().toLowerCase(Locale.ENGLISH).replace('_', '-');
	}

	@Override
	public void writeToNbt(@NotNull NbtCompound tag) {
		for (Type type : Type.values())
			tag.putString(getTypeKey(type), get(type).name());
	}

	@Override
	public void readFromNbt(@NotNull NbtCompound tag) {
		for (Type type : Type.values()) {
			String key = getTypeKey(type);
			if (tag.contains(key)) {
				try {
					rawSet(type, Value.valueOf(tag.getString(key)));
				} catch (Exception e) {
					logger.info("Invalid value for movement status type {}: {}", type, tag.getString(key));
				}
			}
		}
	}

	@Override
	public boolean shouldSyncWith(ServerPlayerEntity player) {
		return this.provider == player;
	}
}
