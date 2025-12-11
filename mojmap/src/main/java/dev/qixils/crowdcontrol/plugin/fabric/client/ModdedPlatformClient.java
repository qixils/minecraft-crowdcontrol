package dev.qixils.crowdcontrol.plugin.fabric.client;

import dev.qixils.crowdcontrol.common.HideNames;
import dev.qixils.crowdcontrol.common.packets.util.ExtraFeature;
import dev.qixils.crowdcontrol.common.packets.util.LanguageState;
import dev.qixils.crowdcontrol.common.util.SemVer;
import dev.qixils.crowdcontrol.plugin.fabric.ModdedCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.fabric.packets.*;
import dev.qixils.crowdcontrol.plugin.fabric.utils.ClientAdapter;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Environment(EnvType.CLIENT)
public abstract class ModdedPlatformClient {
	protected final Logger logger = LoggerFactory.getLogger("CrowdControl/Client");
	protected final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
	protected static @Nullable ModdedPlatformClient INSTANCE = null;
	public static boolean SHADER_ACTIVE = false;
	public static LanguageState LANGUAGE_STATE = LanguageState.RESET;
	protected Minecraft client = null;

	/**
	 * Fetches the initialized client.
	 * <p>
	 * Calling code should first ensure that the client is
	 * {@link ModdedCrowdControlPlugin#CLIENT_INITIALIZED initialized}, otherwise this will throw an
	 * {@link IllegalStateException}.
	 *
	 * @return the loaded client
	 * @throws IllegalStateException if the client is uninitialized
	 */
	public static @NotNull ModdedPlatformClient get() {
		if (INSTANCE == null)
			throw new IllegalStateException("Client instance is uninitialized. " +
					"Please query `ModdedCrowdControlPlugin.CLIENT_INITIALIZED` before calling this method.");
		return INSTANCE;
	}

	public @NotNull Set<ExtraFeature> getExtraFeatures() {
		return EnumSet.noneOf(ExtraFeature.class);
	}

	public void onInitializeClient() {
		INSTANCE = this;
		ModdedCrowdControlPlugin.CLIENT_INITIALIZED = true;
		ClientAdapter.setLocalPlayerIdSupplier(() -> player().map(LocalPlayer::getUUID));
		ClientMinecraftEvents.CLIENT_STARTED.register(this::setClient);
		ClientMinecraftEvents.CLIENT_STOPPING.register(client -> setClient(null));
	}

	private void setClient(@Nullable Minecraft client) {
		if (client == null) {
			this.client = null;
			ModdedCrowdControlPlugin.CLIENT_AVAILABLE = false;
		} else {
			this.client = client;
			ModdedCrowdControlPlugin.CLIENT_AVAILABLE = true;
		}
	}

	public @NotNull Optional<LocalPlayer> player() {
		return Optional.ofNullable(client).map(minecraft -> minecraft.player);
	}

	public static Screen createConfigScreen(Screen parent) {
		ModdedCrowdControlPlugin plugin = ModdedCrowdControlPlugin.getInstance();
		plugin.loadConfig();
		ConfigBuilder builder = ConfigBuilder.create()
			// I wish I could hide the search bar
			.setParentScreen(parent)
			.setTitle(Component.translatable("config.crowdcontrol.title"))
			.setSavingRunnable(plugin::saveConfig);
		ConfigCategory category = builder.getOrCreateCategory(Component.translatable("config.crowdcontrol.category.general"));
		ConfigEntryBuilder entryBuilder = builder.entryBuilder();
		category.addEntry(entryBuilder.startBooleanToggle(Component.translatable("config.crowdcontrol.announce.name"), plugin.announceEffects())
			.setDefaultValue(true)
			.setTooltip(Component.translatable("config.crowdcontrol.announce.description"))
			.setSaveConsumer(plugin::setAnnounceEffects)
			.build());
		category.addEntry(entryBuilder.startEnumSelector(Component.translatable("config.crowdcontrol.hide_names.name"), HideNames.class, plugin.getHideNames())
			.setDefaultValue(HideNames.NONE)
			.setTooltip(Component.translatable("config.crowdcontrol.hide_names.description"))
			.setSaveConsumer(plugin::setHideNames)
			.setEnumNameProvider(enumValue -> Component.translatable("config.crowdcontrol.hide_names.option." + ((HideNames) enumValue).getConfigCode()))
			.build());
		category.addEntry(entryBuilder.startTextDescription(Component.translatable("config.crowdcontrol.advanced_settings")).build());
		// TODO: add entity & item limits
		return builder.build();
	}

	public void handleRequestVersion(@NotNull RequestVersionS2C payload, @NotNull ClientPacketContext context) {
		logger.info("Received version request from server!");
		context.send(new ResponseVersionC2S(SemVer.MOD));
		context.send(new ExtraFeatureC2S(getExtraFeatures()));
	}

	public void handleSetShader(@NotNull SetShaderS2C payload, @NotNull ClientPacketContext context) {
		logger.debug("Received shader request from server!");
//		Identifier shader = withDefaultNamespace(payload.shader());
//
//		client.execute(() -> {
//			client.gameRenderer.setPostEffect(shader);
//			SHADER_ACTIVE = true;
//		});
//		executor.schedule(() -> client.execute(() -> {
//			SHADER_ACTIVE = false;
//			client.gameRenderer.checkEntityPostEffect(client.cameraEntity);
//		}), payload.duration().toMillis(), TimeUnit.MILLISECONDS);
	}

	public void handleMovementStatus(@NotNull MovementStatusS2C payload, @NotNull ClientPacketContext context) {
		if (payload.statusType() == null || payload.statusValue() == null) return;
		context.player().cc$setMovementStatus(payload.statusType(), payload.statusValue());
	}

	public abstract void sendToServer(@NotNull CustomPacketPayload payload);

	public static void sendToServerStatic(@NotNull CustomPacketPayload payload) {
		if (INSTANCE == null) return;
		INSTANCE.sendToServer(payload);
	}
}
