package dev.qixils.crowdcontrol.plugin.sponge8.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.api.entity.attribute.Attribute;
import org.spongepowered.api.entity.attribute.AttributeHolder;
import org.spongepowered.api.entity.attribute.AttributeModifier;
import org.spongepowered.api.entity.attribute.AttributeOperation;
import org.spongepowered.api.entity.attribute.type.AttributeType;
import org.spongepowered.api.registry.RegistryTypes;

import java.util.Optional;
import java.util.UUID;

public class AttributeUtil {
	private static final Logger logger = LoggerFactory.getLogger("CrowdControl/AttributeUtil");

	public static Optional<AttributeModifier> getModifier(@Nullable Attribute attr, UUID uuid) {
		if (attr == null) return Optional.empty();
		return attr.modifier(uuid);
	}

	public static Optional<AttributeModifier> getModifier(AttributeHolder player, AttributeType attribute, UUID uuid) {
		return getModifier(player.attribute(attribute).orElse(null), uuid);
	}

	public static void removeModifier(@Nullable Attribute attr, @NotNull UUID uuid) {
		if (attr == null) return;
		attr.removeModifier(uuid);
	}

	public static void removeModifier(AttributeHolder player, AttributeType attribute, UUID uuid) {
		removeModifier(player.attribute(attribute).orElse(null), uuid);
	}

	public static void addModifier(Attribute attr, UUID uuid, String name, double level, AttributeOperation op) {
		removeModifier(attr, uuid);
		if (level == 0) return;

		attr.addModifier(AttributeModifier.builder()
			.id(uuid)
			.name(name)
			.amount(level)
			.operation(op)
			.build());
	}

	public static void addModifier(AttributeHolder player, AttributeType attribute, UUID uuid, String name, double level, AttributeOperation op) {
		Optional<Attribute> attr = player.attribute(attribute);
		if (!attr.isPresent()) {
			logger.warn("Player missing {} attribute", attribute.findKey(RegistryTypes.ATTRIBUTE_TYPE).orElse(null));
			return;
		}

		addModifier(attr.get(), uuid, name, level, op);
	}
}
