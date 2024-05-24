package dev.qixils.crowdcontrol.plugin.paper.utils;

import org.bukkit.attribute.Attributable;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

public class AttributeUtil {
	private static final Logger logger = LoggerFactory.getLogger("CrowdControl/AttributeUtil");

	public static Optional<AttributeModifier> getModifier(@Nullable AttributeInstance attr, UUID uuid) {
		if (attr == null) return Optional.empty();
		return Optional.ofNullable(attr.getModifier(uuid));
	}

	public static Optional<AttributeModifier> getModifier(Attributable player, Attribute attribute, UUID uuid) {
		return getModifier(player.getAttribute(attribute), uuid);
	}

	public static void removeModifier(@Nullable AttributeInstance attr, @NotNull UUID uuid) {
		if (attr == null) return;
		attr.removeModifier(uuid);
	}

	public static void removeModifier(Attributable player, Attribute attribute, UUID uuid) {
		removeModifier(player.getAttribute(attribute), uuid);
	}

	public static void addModifier(AttributeInstance attr, UUID uuid, String name, double level, AttributeModifier.Operation op, boolean permanent) {
		removeModifier(attr, uuid);
		if (level == 0) return;

		Consumer<AttributeModifier> func = permanent
			? attr::addTransientModifier
			: attr::addModifier;

		func.accept(new AttributeModifier(
			uuid,
			name,
			level,
			op
		));
	}

	public static void addModifier(Attributable player, Attribute attribute, UUID uuid, String name, double level, AttributeModifier.Operation op, boolean permanent) {
		AttributeInstance attr = player.getAttribute(attribute);
		if (attr == null) {
			logger.warn("Player missing {} attribute", attribute.getKey());
			return;
		}

		addModifier(attr, uuid, name, level, op, permanent);
	}
}
