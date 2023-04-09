package dev.qixils.crowdcontrol.plugin.fabric.client;

import dev.qixils.crowdcontrol.common.util.SemVer;
import dev.qixils.crowdcontrol.plugin.fabric.FabricCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.fabric.mixin.GameRendererAccessor;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
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
	private final Logger logger = LoggerFactory.getLogger(FabricPlatformClient.class);
	private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
	public final ProposalHandler proposalHandler = new ProposalHandler(this);
	private static @Nullable FabricPlatformClient INSTANCE = null;
	public static boolean SHADER_ACTIVE = false;
	private MinecraftClient client = null;

	public FabricPlatformClient() {
		INSTANCE = this;
	}

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

	/**
	 * Fetches the initialized client.
	 *
	 * @return the loaded client, or {@link Optional#empty()} if the client is uninitialized
	 */
	public static @NotNull Optional<FabricPlatformClient> getOptional() {
		return Optional.ofNullable(INSTANCE);
	}

	@Override
	public void onInitializeClient() {
		FabricCrowdControlPlugin.CLIENT_INITIALIZED = true;
		ClientLifecycleEvents.CLIENT_STARTED.register(this::setClient);
		ClientLifecycleEvents.CLIENT_STOPPING.register(client -> setClient(null));
		ClientPlayNetworking.registerGlobalReceiver(FabricCrowdControlPlugin.VERSION_REQUEST_ID, (client, handler, inputBuf, responseSender) -> {
			logger.debug("Received version request from server!");
			PacketByteBuf buf = PacketByteBufs.create();
			buf.writeString(SemVer.MOD_STRING, 32);
			responseSender.sendPacket(FabricCrowdControlPlugin.VERSION_RESPONSE_ID, buf);
		});
		ClientPlayNetworking.registerGlobalReceiver(FabricCrowdControlPlugin.SHADER_ID, (client, handler, inputBuf, responseSender) -> {
			logger.debug("Received shader request from server!");
			Identifier shader = new Identifier("shaders/post/" + inputBuf.readString(64) + ".json");
			long millis = inputBuf.readLong();

			client.execute(() -> {
				((GameRendererAccessor) client.gameRenderer).invokeLoadEffect(shader);
				SHADER_ACTIVE = true;
			});
			executor.schedule(() -> client.execute(() -> {
				SHADER_ACTIVE = false;
				client.gameRenderer.onCameraEntitySet(client.cameraEntity);
			}), millis, TimeUnit.MILLISECONDS);
		});
	}

	private void setClient(@Nullable MinecraftClient client) {
		if (client == null) {
			this.client = null;
			FabricCrowdControlPlugin.CLIENT_AVAILABLE = false;
		} else {
			this.client = client;
			FabricCrowdControlPlugin.CLIENT_AVAILABLE = true;
		}
	}

	public @NotNull Optional<MinecraftClient> client() {
		return Optional.ofNullable(client);
	}

	public @NotNull Optional<ClientPlayerEntity> player() {
		return client().map(minecraft -> minecraft.player);
	}
}
