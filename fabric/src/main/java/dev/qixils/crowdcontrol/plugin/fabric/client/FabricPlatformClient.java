package dev.qixils.crowdcontrol.plugin.fabric.client;

import dev.qixils.crowdcontrol.common.packets.util.ExtraFeature;
import dev.qixils.crowdcontrol.common.packets.util.LanguageState;
import dev.qixils.crowdcontrol.common.util.SemVer;
import dev.qixils.crowdcontrol.plugin.fabric.FabricCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.fabric.packets.*;
import jerozgen.languagereload.LanguageReload;
import jerozgen.languagereload.config.Config;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static net.minecraft.resources.ResourceLocation.withDefaultNamespace;

@Environment(EnvType.CLIENT)
public final class FabricPlatformClient implements ClientModInitializer {
	private final Logger logger = LoggerFactory.getLogger("CrowdControl/Client");
	private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
	private static @Nullable FabricPlatformClient INSTANCE = null;
	public static boolean SHADER_ACTIVE = false;
	public static LanguageState LANGUAGE_STATE = LanguageState.RESET;
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

	public @NotNull Set<ExtraFeature> getExtraFeatures() {
		Set<ExtraFeature> features = EnumSet.noneOf(ExtraFeature.class);
		if (FabricLoader.getInstance().isModLoaded("languagereload"))
			features.add(ExtraFeature.LANGUAGE_RELOAD);
		return features;
	}

	private void handleLanguage(SetLanguageS2C payload) {
		if (client == null) return;
		if (!getExtraFeatures().contains(ExtraFeature.LANGUAGE_RELOAD)) return;
		if (LANGUAGE_STATE == payload.state()) return;
		LANGUAGE_STATE = payload.state();

		switch (LANGUAGE_STATE) {
			case RESET -> {
				Config config = Config.getInstance();
				LanguageReload.setLanguage(config.previousLanguage, config.previousFallbacks);
			}
			case RANDOM -> {
				Config config = Config.getInstance();
				List<String> languages = new ArrayList<>(client.getLanguageManager().getLanguages().keySet());
				languages.remove(config.language);
				languages.removeAll(config.fallbacks);
				languages.remove("en_us");
				Collections.shuffle(languages);
				LinkedList<String> fallback = new LinkedList<>(languages.subList(1, 6));
				fallback.add("en_us"); // I assume this should always be loaded
				LanguageReload.setLanguage(languages.getFirst(), fallback);
			}
			case null, default -> logger.warn("Unknown language state {}", LANGUAGE_STATE);
		}

		if (LANGUAGE_STATE != LanguageState.RESET)
			executor.schedule(
				() -> handleLanguage(new SetLanguageS2C(LanguageState.RESET, Duration.ZERO)),
				payload.duration().toMillis(),
				TimeUnit.MILLISECONDS
			);
	}

	@Override
	public void onInitializeClient() {
		INSTANCE = this;
		FabricCrowdControlPlugin.CLIENT_INITIALIZED = true;
		ClientLifecycleEvents.CLIENT_STARTED.register(this::setClient);
		ClientLifecycleEvents.CLIENT_STOPPING.register(client -> setClient(null));
		PacketUtil.registerPackets();
		ClientPlayNetworking.registerGlobalReceiver(RequestVersionS2C.PACKET_ID, (payload, context) -> {
			logger.info("Received version request from server!");
			context.responseSender().sendPacket(new ResponseVersionC2S(SemVer.MOD));
			context.responseSender().sendPacket(new ExtraFeatureC2S(getExtraFeatures()));
		});
		ClientPlayNetworking.registerGlobalReceiver(SetShaderS2C.PACKET_ID, (payload, context) -> {
			logger.debug("Received shader request from server!");
			ResourceLocation shader = withDefaultNamespace("shaders/post/" + payload.shader() + ".json");

			client.execute(() -> {
				client.gameRenderer.loadEffect(shader);
				SHADER_ACTIVE = true;
			});
			executor.schedule(() -> client.execute(() -> {
				SHADER_ACTIVE = false;
				client.gameRenderer.checkEntityPostEffect(client.cameraEntity);
			}), payload.duration().toMillis(), TimeUnit.MILLISECONDS);
		});
		ClientPlayNetworking.registerGlobalReceiver(MovementStatusS2C.PACKET_ID, (payload, context) -> {
			if (payload.statusType() == null || payload.statusValue() == null) return;
			context.player().cc$setMovementStatus(payload.statusType(), payload.statusValue());
		});
		ClientPlayNetworking.registerGlobalReceiver(SetLanguageS2C.PACKET_ID, (payload, context) -> handleLanguage(payload));
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
