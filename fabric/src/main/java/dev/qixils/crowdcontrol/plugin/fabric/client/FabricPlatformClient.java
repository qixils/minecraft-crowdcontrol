package dev.qixils.crowdcontrol.plugin.fabric.client;

import dev.qixils.crowdcontrol.plugin.fabric.FabricCrowdControlPlugin;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

@Environment(EnvType.CLIENT)
public final class FabricPlatformClient implements ClientModInitializer {
	private static @Nullable FabricPlatformClient INSTANCE = null;
	private Minecraft client = null;

	/**
	 * Fetches the initialized client.
	 * <p>
	 * Calling code should first ensure that the client is
	 * {@link FabricCrowdControlPlugin#CLIENT_INITIALIZED initialized}, otherwise this will throw an
	 * {@link IllegalStateException}.
	 *
	 * @return the loaded client
	 * @throws IllegalStateException if the client is uninitialized
	 */
	public static @NotNull FabricPlatformClient get() {
		if (INSTANCE == null)
			throw new IllegalStateException("Client instance is uninitialized. " +
					"Please query `FabricCrowdControlPlugin.CLIENT_INITIALIZED` before calling this method.");
		return INSTANCE;
	}

	@Override
	public void onInitializeClient() {
		INSTANCE = this;
		FabricCrowdControlPlugin.CLIENT_INITIALIZED = true;
		ClientLifecycleEvents.CLIENT_STARTED.register(this::setClient);
		ClientLifecycleEvents.CLIENT_STOPPING.register(client -> setClient(null));
	}

	private void setClient(@Nullable Minecraft client) {
		if (client == null) {
			this.client = null;
			FabricCrowdControlPlugin.CLIENT_AVAILABLE = false;
		} else {
			this.client = client;
			FabricCrowdControlPlugin.CLIENT_AVAILABLE = true;
		}
	}

	public @NotNull Optional<LocalPlayer> player() {
		if (client == null) return Optional.empty();
		return Optional.ofNullable(client.player);
	}
}
