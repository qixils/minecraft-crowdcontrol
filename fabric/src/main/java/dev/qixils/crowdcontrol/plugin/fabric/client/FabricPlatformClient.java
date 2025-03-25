package dev.qixils.crowdcontrol.plugin.fabric.client;

import dev.qixils.crowdcontrol.common.packets.util.ExtraFeature;
import dev.qixils.crowdcontrol.common.packets.util.LanguageState;
import dev.qixils.crowdcontrol.plugin.fabric.client.fabric.ClientPacketContextImpl;
import dev.qixils.crowdcontrol.plugin.fabric.packets.MovementStatusS2C;
import dev.qixils.crowdcontrol.plugin.fabric.packets.RequestVersionS2C;
import dev.qixils.crowdcontrol.plugin.fabric.packets.SetLanguageS2C;
import dev.qixils.crowdcontrol.plugin.fabric.packets.SetShaderS2C;
import dev.qixils.crowdcontrol.plugin.fabric.packets.fabric.PacketUtilImpl;
import jerozgen.languagereload.LanguageReload;
import jerozgen.languagereload.config.Config;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Environment(EnvType.CLIENT)
public class FabricPlatformClient extends ModdedPlatformClient implements ClientModInitializer {

	public static @NotNull FabricPlatformClient get() {
		return (FabricPlatformClient) ModdedPlatformClient.get();
	}

	@Override
	public void onInitializeClient() {
		super.onInitializeClient();

		PacketUtilImpl.registerPackets();

		ClientPlayNetworking.registerGlobalReceiver(RequestVersionS2C.PACKET_ID, (payload, context) -> handleRequestVersion(payload, new ClientPacketContextImpl(context)));
		ClientPlayNetworking.registerGlobalReceiver(SetShaderS2C.PACKET_ID, (payload, context) -> handleSetShader(payload, new ClientPacketContextImpl(context)));
		ClientPlayNetworking.registerGlobalReceiver(MovementStatusS2C.PACKET_ID, (payload, context) -> handleMovementStatus(payload, new ClientPacketContextImpl(context)));
		ClientPlayNetworking.registerGlobalReceiver(SetLanguageS2C.PACKET_ID, (payload, context) -> handleLanguage(payload));
	}

	@Override
	public @NotNull Set<ExtraFeature> getExtraFeatures() {
		Set<ExtraFeature> features = super.getExtraFeatures();
		if (FabricLoader.getInstance().isModLoaded("languagereload"))
			features.add(ExtraFeature.LANGUAGE_RELOAD);
		return features;
	}

	public void handleLanguage(SetLanguageS2C payload) {
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

	public void sendToServer(@NotNull CustomPacketPayload payload) {
		if (!ClientPlayNetworking.canSend(payload.type())) return;
		try {
			ClientPlayNetworking.send(payload);
		} catch (UnsupportedOperationException e) {
			logger.debug("Server cannot receive packet {}", payload);
		}
	}
}
