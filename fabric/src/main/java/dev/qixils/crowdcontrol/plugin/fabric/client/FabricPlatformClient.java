package dev.qixils.crowdcontrol.plugin.fabric.client;

import dev.qixils.crowdcontrol.common.packets.*;
import dev.qixils.crowdcontrol.common.packets.util.ExtraFeature;
import dev.qixils.crowdcontrol.common.packets.util.LanguageState;
import dev.qixils.crowdcontrol.plugin.fabric.packets.fabric.PacketUtilImpl;
import io.netty.buffer.Unpooled;
import jerozgen.languagereload.LanguageReload;
import jerozgen.languagereload.config.Config;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.resources.language.LanguageInfo;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Environment(EnvType.CLIENT)
public class FabricPlatformClient extends ModdedPlatformClient implements ClientModInitializer {

	public static @NotNull FabricPlatformClient get() {
		return (FabricPlatformClient) ModdedPlatformClient.get();
	}

	@Override
	public void onInitializeClient() {
		super.onInitializeClient();

		PacketUtilImpl.registerPackets();

		ClientSidePacketRegistry.INSTANCE.register(new ResourceLocation(VersionRequestPacketS2C.METADATA.channel()), (ctx, buf) -> handleRequestVersion(VersionRequestPacketS2C.INSTANCE));
		ClientSidePacketRegistry.INSTANCE.register(new ResourceLocation(ShaderPacketS2C.METADATA.channel()), (ctx, buf) -> handleSetShader(new ShaderPacketS2C(buf)));
		ClientSidePacketRegistry.INSTANCE.register(new ResourceLocation(MovementStatusPacketS2C.METADATA.channel()), (ctx, buf) -> handleMovementStatus(new MovementStatusPacketS2C(buf)));
		ClientSidePacketRegistry.INSTANCE.register(new ResourceLocation(SetLanguagePacketS2C.METADATA.channel()), (ctx, buf) -> handleLanguage(new SetLanguagePacketS2C(buf)));
	}

	@Override
	public @NotNull Set<ExtraFeature> getExtraFeatures() {
		Set<ExtraFeature> features = super.getExtraFeatures();
		if (FabricLoader.getInstance().isModLoaded("languagereload"))
			features.add(ExtraFeature.LANGUAGE_RELOAD);
		return features;
	}

	public void handleLanguage(SetLanguagePacketS2C payload) {
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
				List<String> languages = client.getLanguageManager().getLanguages().stream().map(LanguageInfo::getCode).collect(Collectors.toList());
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
				() -> handleLanguage(new SetLanguagePacketS2C(LanguageState.RESET, Duration.ZERO)),
				payload.duration().toMillis(),
				TimeUnit.MILLISECONDS
			);
	}

	public void sendToServer(@NotNull PluginPacket payload) {
		ResourceLocation loc = new ResourceLocation(payload.metadata().channel());
		if (!ClientSidePacketRegistry.INSTANCE.canServerReceive(loc)) return;
		try {
			FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
			payload.write(buf);
			ClientSidePacketRegistry.INSTANCE.sendToServer(loc, new FriendlyByteBuf(buf.copy()));
		} catch (UnsupportedOperationException e) {
			logger.debug("Server cannot receive packet {}", payload);
		}
	}
}
