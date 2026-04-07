package dev.qixils.crowdcontrol.plugin.fabric.client;

import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.*;
import dev.qixils.crowdcontrol.common.HideNames;
import dev.qixils.crowdcontrol.common.SoftLockConfig;
import dev.qixils.crowdcontrol.common.packets.util.ExtraFeature;
import dev.qixils.crowdcontrol.common.packets.util.LanguageState;
import dev.qixils.crowdcontrol.common.util.SemVer;
import dev.qixils.crowdcontrol.plugin.fabric.ModdedCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.fabric.client.option.MapOption;
import dev.qixils.crowdcontrol.plugin.fabric.interfaces.MovementStatus;
import dev.qixils.crowdcontrol.plugin.fabric.packets.*;
import dev.qixils.crowdcontrol.plugin.fabric.utils.ClientAdapter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;
import java.util.stream.Stream;

//@Environment(EnvType.CLIENT)
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
		try {
			return YetAnotherConfigLib.createBuilder()
				.title(Component.translatable("config.crowdcontrol.title"))
				.save(plugin::saveConfig)
				.category(ConfigCategory.createBuilder()
					.name(Component.translatable("config.crowdcontrol.category.general"))
					// TODO: custom effects gui one day
					.option(LabelOption.create(Component.translatable("config.crowdcontrol.advanced_settings")))
					.option(Option.<Boolean>createBuilder()
						.name(Component.translatable("config.crowdcontrol.announce.name"))
						.description(OptionDescription.of(Component.translatable("config.crowdcontrol.announce.description")))
						.binding(
							true,
							plugin::announceEffects,
							plugin::setAnnounceEffects
						)
						.controller(TickBoxControllerBuilder::create)
						.build()
					)
					.option(Option.<HideNames>createBuilder()
						.name(Component.translatable("config.crowdcontrol.hide_names.name"))
						.description(OptionDescription.of(Component.translatable("config.crowdcontrol.hide_names.description")))
						.binding(
							HideNames.NONE,
							plugin::getHideNames,
							plugin::setHideNames
						)
						.controller(opt -> EnumControllerBuilder.create(opt)
							.enumClass(HideNames.class)
							.formatValue(enumValue -> Component.translatable("config.crowdcontrol.hide_names.option." + enumValue.getConfigCode()))
						)
						.build()
					)
					.option(Option.<Boolean>createBuilder()
						.name(Component.translatable("config.crowdcontrol.global.name"))
						.description(OptionDescription.of(Component.translatable("config.crowdcontrol.global.description")))
						.binding(
							true,
							plugin::isGlobal,
							plugin::setIsGlobal
						)
						.controller(TickBoxControllerBuilder::create)
						.build()
					)
					.group(OptionGroup.createBuilder()
						.name(Component.translatable("config.crowdcontrol.soft_lock.name"))
						.description(OptionDescription.of(Component.translatable("config.crowdcontrol.soft_lock.description")))
						.option(Option.<Integer>createBuilder()
							.name(Component.translatable("config.crowdcontrol.soft_lock.period.name"))
							.description(OptionDescription.of(Component.translatable("config.crowdcontrol.soft_lock.period.description")))
							.binding(
								SoftLockConfig.DEF_PERIOD,
								() -> Math.toIntExact(plugin.getSoftLockConfig().getPeriod()),
								value -> plugin.getSoftLockConfig().setPeriod(value)
							)
							.controller(opt -> IntegerFieldControllerBuilder.create(opt).min(0))
							.build())
						.option(Option.<Integer>createBuilder()
							.name(Component.translatable("config.crowdcontrol.soft_lock.deaths.name"))
							.description(OptionDescription.of(Component.translatable("config.crowdcontrol.soft_lock.deaths.description")))
							.binding(
								SoftLockConfig.DEF_DEATHS,
								() -> Math.toIntExact(plugin.getSoftLockConfig().getDeaths()),
								value -> plugin.getSoftLockConfig().setDeaths(value)
							)
							.controller(opt -> IntegerFieldControllerBuilder.create(opt).min(0))
							.build())
						.option(Option.<Integer>createBuilder()
							.name(Component.translatable("config.crowdcontrol.soft_lock.horiz_radius.name"))
							.description(OptionDescription.of(Component.translatable("config.crowdcontrol.soft_lock.radius.description")))
							.binding(
								SoftLockConfig.DEF_SEARCH_HORIZ,
								() -> Math.toIntExact(plugin.getSoftLockConfig().getSearchH()),
								value -> plugin.getSoftLockConfig().setSearchH(value)
							)
							.controller(opt -> IntegerFieldControllerBuilder.create(opt).min(0))
							.build())
						.option(Option.<Integer>createBuilder()
							.name(Component.translatable("config.crowdcontrol.soft_lock.vert_radius.name"))
							.description(OptionDescription.of(Component.translatable("config.crowdcontrol.soft_lock.radius.description")))
							.binding(
								SoftLockConfig.DEF_SEARCH_VERT,
								() -> Math.toIntExact(plugin.getSoftLockConfig().getSearchV()),
								value -> plugin.getSoftLockConfig().setSearchV(value)
							)
							.controller(opt -> IntegerFieldControllerBuilder.create(opt).min(0))
							.build())
						.build())
					.group(ListOption.<String>createBuilder()
						.name(Component.translatable("config.crowdcontrol.hosts.name"))
						.description(OptionDescription.of(Component.translatable("config.crowdcontrol.hosts.description")))
						.binding(
							new ArrayList<>(),
							() -> new ArrayList<>(plugin.getRawHosts()),
							plugin::setRawHosts
						)
						.controller(StringControllerBuilder::create)
						.initial(() -> Minecraft.getInstance().getGameProfile().name())
						.build())
					.build()
				)
				.category(ConfigCategory.createBuilder()
					.name(Component.translatable("config.crowdcontrol.category.limits"))
					.option(Option.<Boolean>createBuilder()
						.name(Component.translatable("config.crowdcontrol.hosts_bypass.name"))
						.description(OptionDescription.of(Component.translatable("config.crowdcontrol.hosts_bypass.description")))
						.binding(
							true,
							() -> plugin.getLimitConfig().hostsBypass(),
							value -> plugin.getLimitConfig().hostsBypass(value)
						)
						.controller(TickBoxControllerBuilder::create)
						.build()
					)
					.option(Option.<Integer>createBuilder()
						.name(Component.translatable("config.crowdcontrol.default_item_limit.name"))
						.description(OptionDescription.of(Component.translatable("config.crowdcontrol.default_item_limit.description")))
						.binding(
							0,
							() -> plugin.getLimitConfig().defaultItemLimit(),
							value -> plugin.getLimitConfig().defaultItemLimit(value)
						)
						.controller(opt -> IntegerFieldControllerBuilder.create(opt).min(0))
						.build())
					.group(MapOption.<String, Integer>createBuilder()
						.name(Component.translatable("config.crowdcontrol.item_limit.name"))
						.description(OptionDescription.of(Component.translatable("config.crowdcontrol.item_limit.description")))
						.binding(
							new HashMap<>(),
							() -> plugin.getLimitConfig().itemLimits().entrySet().stream().flatMap(entry -> {
								if (entry.getKey().equals("default")) return Stream.empty();
								try {
									var item = BuiltInRegistries.ITEM.get(Identifier.parse(entry.getKey()));
									assert item.isPresent() && item.get().isBound();
									return Stream.of(Map.entry(item.get().key().identifier().toShortString(), entry.getValue()));
								} catch (Exception e) {
									plugin.getSLF4JLogger().atDebug().setCause(e).log("Reading unknown limit item");
								}
								return Stream.empty();
							}).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> b)),
							value -> plugin.getLimitConfig().itemLimits(value.entrySet().stream().flatMap(entry -> {
								if (entry.getKey().equals("default")) return Stream.empty();
								try {
									var item = BuiltInRegistries.ITEM.get(Identifier.parse(entry.getKey()));
									assert item.isPresent() && item.get().isBound();
									return Stream.of(Map.entry(item.get().key().identifier().toShortString(), entry.getValue()));
								} catch (Exception e) {
									plugin.getSLF4JLogger().atDebug().setCause(e).log("Setting unknown limit item");
								}
								return Stream.empty();
							}).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> b)))
						)
						.initialKey("diamond_sword")
						.initialValue(0)
						.keyController(opt -> DropdownStringControllerBuilder.create(opt).values(BuiltInRegistries.ITEM.entrySet().stream().map(item -> item.getKey().identifier().toShortString()).toList()))
						.valueController(opt -> IntegerFieldControllerBuilder.create(opt).min(0))
						.build())
					.option(Option.<Integer>createBuilder()
						.name(Component.translatable("config.crowdcontrol.default_entity_limit.name"))
						.description(OptionDescription.of(Component.translatable("config.crowdcontrol.default_entity_limit.description")))
						.binding(
							0,
							() -> plugin.getLimitConfig().defaultEntityLimit(),
							value -> plugin.getLimitConfig().defaultEntityLimit(value)
						)
						.controller(opt -> IntegerFieldControllerBuilder.create(opt).min(0))
						.build())
					.group(MapOption.<String, Integer>createBuilder()
						.name(Component.translatable("config.crowdcontrol.entity_limit.name"))
						.description(OptionDescription.of(Component.translatable("config.crowdcontrol.entity_limit.description")))
						.binding(
							new HashMap<>(),
							() -> plugin.getLimitConfig().entityLimits().entrySet().stream().flatMap(entry -> {
								if (entry.getKey().equals("default")) return Stream.empty();
								try {
									var entity = BuiltInRegistries.ENTITY_TYPE.get(Identifier.parse(entry.getKey()));
									assert entity.isPresent() && entity.get().isBound();
									return Stream.of(Map.entry(entity.get().key().identifier().toShortString(), entry.getValue()));
								} catch (Exception e) {
									plugin.getSLF4JLogger().atDebug().setCause(e).log("Reading unknown limit entity");
								}
								return Stream.empty();
							}).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> b)),
							value -> plugin.getLimitConfig().entityLimits(value.entrySet().stream().flatMap(entry -> {
								if (entry.getKey().equals("default")) return Stream.empty();
								try {
									var entity = BuiltInRegistries.ENTITY_TYPE.get(Identifier.parse(entry.getKey()));
									assert entity.isPresent() && entity.get().isBound();
									return Stream.of(Map.entry(entity.get().key().identifier().toShortString(), entry.getValue()));
								} catch (Exception e) {
									plugin.getSLF4JLogger().atDebug().setCause(e).log("Setting unknown limit item");
								}
								return Stream.empty();
							}).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> b)))
						)
						.initialKey("creeper")
						.initialValue(0)
						.keyController(opt -> DropdownStringControllerBuilder.create(opt).values(BuiltInRegistries.ENTITY_TYPE.entrySet().stream().map(item -> item.getKey().identifier().toShortString()).toList()))
						.valueController(opt -> IntegerFieldControllerBuilder.create(opt).min(0))
						.build())
					.build())
				.build()
				.generateScreen(parent);
		} catch (Exception e) {
			plugin.getSLF4JLogger().atWarn().setCause(e).log("Failed to create yacl screen");
			return new ConfigMissingScreen(parent);
		}
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
		((MovementStatus) context.player()).cc$setMovementStatus(payload.statusType(), payload.statusValue());
	}

	public abstract void sendToServer(@NotNull CustomPacketPayload payload);

	public static void sendToServerStatic(@NotNull CustomPacketPayload payload) {
		if (INSTANCE == null) return;
		INSTANCE.sendToServer(payload);
	}
}
