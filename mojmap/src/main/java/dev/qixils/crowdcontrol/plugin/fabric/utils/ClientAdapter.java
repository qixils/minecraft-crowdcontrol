package dev.qixils.crowdcontrol.plugin.fabric.utils;

import dev.qixils.crowdcontrol.plugin.fabric.ModdedCrowdControlPlugin;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

public class ClientAdapter {

	private static Supplier<Optional<UUID>> localPlayerId = Optional::empty;

	public static boolean isAvailable() {
		return ModdedCrowdControlPlugin.CLIENT_AVAILABLE;
	}

	public static Optional<UUID> getLocalPlayerId() {
		return localPlayerId.get();
	}

	public static void setLocalPlayerIdSupplier(Supplier<Optional<UUID>> supplier) {
		localPlayerId = supplier == null ? Optional::empty : supplier;
	}
}
