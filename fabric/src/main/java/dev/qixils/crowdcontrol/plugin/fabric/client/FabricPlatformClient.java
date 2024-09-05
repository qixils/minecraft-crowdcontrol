package dev.qixils.crowdcontrol.plugin.fabric.client;

import dev.qixils.crowdcontrol.common.packets.util.ExtraFeature;
import dev.qixils.crowdcontrol.common.packets.util.LanguageState;
import dev.qixils.crowdcontrol.plugin.fabric.packets.SetLanguageS2C;
import jerozgen.languagereload.LanguageReload;
import jerozgen.languagereload.config.Config;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class FabricPlatformClient extends ModdedPlatformClient {
	@Override
	public @NotNull Set<ExtraFeature> getExtraFeatures() {
		Set<ExtraFeature> features = super.getExtraFeatures();
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
		super.onInitializeClient();
		ClientPlayNetworking.registerGlobalReceiver(SetLanguageS2C.PACKET_ID, (payload, context) -> handleLanguage(payload));
	}
}
