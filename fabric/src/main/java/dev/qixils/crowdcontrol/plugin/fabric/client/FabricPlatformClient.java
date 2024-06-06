package dev.qixils.crowdcontrol.plugin.fabric.client;

import dev.qixils.crowdcontrol.common.util.SemVer;
import dev.qixils.crowdcontrol.plugin.fabric.FabricCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.fabric.packets.PacketUtil;
import dev.qixils.crowdcontrol.plugin.fabric.packets.RequestVersionS2C;
import dev.qixils.crowdcontrol.plugin.fabric.packets.ResponseVersionC2S;
import dev.qixils.crowdcontrol.plugin.fabric.packets.SetShaderS2C;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Environment(EnvType.CLIENT)
public final class FabricPlatformClient implements ClientModInitializer {
	private final Logger logger = LoggerFactory.getLogger("CrowdControl/Client");
	private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
	private static @Nullable FabricPlatformClient INSTANCE = null;
	public static boolean SHADER_ACTIVE = false;
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
		PacketUtil.registerPackets();
		ClientPlayNetworking.registerGlobalReceiver(RequestVersionS2C.PACKET_ID, (payload, context) -> {
			logger.debug("Received version request from server!");
			context.responseSender().sendPacket(new ResponseVersionC2S(SemVer.MOD));
		});
		ClientPlayNetworking.registerGlobalReceiver(SetShaderS2C.PACKET_ID, (payload, context) -> {
			logger.debug("Received shader request from server!");
			ResourceLocation shader = new ResourceLocation("shaders/post/" + payload.shader() + ".json");

			client.execute(() -> {
				client.gameRenderer.loadEffect(shader);
				SHADER_ACTIVE = true;
			});
			executor.schedule(() -> client.execute(() -> {
				SHADER_ACTIVE = false;
				client.gameRenderer.checkEntityPostEffect(client.cameraEntity);
			}), payload.duration().toMillis(), TimeUnit.MILLISECONDS);
		});
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
		return Optional.ofNullable(client).map(minecraft -> minecraft.player);
	}
}
