package dev.qixils.crowdcontrol.plugin.fabric.client;

import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.*;
import dev.qixils.crowdcontrol.common.HideNames;
import dev.qixils.crowdcontrol.common.SoftLockConfig;
import dev.qixils.crowdcontrol.plugin.fabric.ModdedCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.fabric.client.option.MapOption;
import net.kyori.adventure.platform.modcommon.MinecraftAudiences;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ConfigScreenBuilder {
	public static Screen buildConfigScreen(ModdedCrowdControlPlugin plugin, Screen parent) {
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
						false,
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
					.initial(() -> Minecraft.getInstance().getGameProfile().getName())
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
						() -> displayedLimits(BuiltInRegistries.ITEM, plugin.getLimitConfig().itemLimits()),
						value -> plugin.getLimitConfig().itemLimits(mergeLimits(BuiltInRegistries.ITEM, plugin.getLimitConfig().itemLimits(), value))
					)
					.initialKey("diamond_sword")
					.initialValue(0)
					.keyController(opt -> DropdownStringControllerBuilder.create(opt).values(BuiltInRegistries.ITEM.entrySet().stream().map(item -> MinecraftAudiences.key(item.getKey()).asMinimalString()).toList()))
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
						() -> displayedLimits(BuiltInRegistries.ENTITY_TYPE, plugin.getLimitConfig().entityLimits()),
						value -> plugin.getLimitConfig().entityLimits(mergeLimits(BuiltInRegistries.ENTITY_TYPE, plugin.getLimitConfig().entityLimits(), value))
					)
					.initialKey("creeper")
					.initialValue(0)
					.keyController(opt -> DropdownStringControllerBuilder.create(opt).values(BuiltInRegistries.ENTITY_TYPE.entrySet().stream().map(item -> MinecraftAudiences.key(item.getKey()).asMinimalString()).toList()))
					.valueController(opt -> IntegerFieldControllerBuilder.create(opt).min(0))
					.build())
				.build())
			.build()
			.generateScreen(parent);
	}

	/**
	 * Gets the limits that can be shown in the config screen,
	 * skipping the default limit (which has its own option)
	 * and any IDs that aren't registered.
	 */
	private static <T> Map<String, Integer> displayedLimits(Registry<T> registry, Map<String, Integer> limits) {
		Map<String, Integer> displayed = new HashMap<>();
		limits.forEach((id, limit) -> displayedId(registry, id).ifPresent(key -> displayed.put(key, limit)));
		return displayed;
	}

	/**
	 * Merges the limits edited in the config screen into the existing limits,
	 * keeping the ones that the config screen couldn't show.
	 */
	private static <T> Map<String, Integer> mergeLimits(Registry<T> registry, Map<String, Integer> existing, Map<String, Integer> edited) {
		Map<String, Integer> merged = new HashMap<>();
		existing.forEach((id, limit) -> {
			if (displayedId(registry, id).isEmpty()) merged.put(id, limit);
		});
		merged.putAll(displayedLimits(registry, edited));
		return merged;
	}

	/**
	 * Gets the ID that the config screen uses for a limit, if it can be shown there.
	 */
	private static <T> Optional<String> displayedId(Registry<T> registry, String id) {
		if (id.equals("default")) return Optional.empty();
		ResourceLocation location = ResourceLocation.tryParse(id);
		if (location == null) return Optional.empty();
		return registry.get(location).map(holder -> MinecraftAudiences.key(holder.key()).asMinimalString());
	}
}
