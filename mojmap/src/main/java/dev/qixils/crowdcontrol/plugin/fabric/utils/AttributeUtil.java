package dev.qixils.crowdcontrol.plugin.fabric.utils;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
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

	public static Optional<AttributeModifier> getModifier(LivingEntity player, Attribute attribute, UUID uuid) {
		return getModifier(player.getAttribute(attribute), uuid);
	}

	public static void removeModifier(@Nullable AttributeInstance attr, UUID uuid) {
		if (attr == null) return;
		attr.removeModifier(uuid);
	}

	public static void removeModifier(LivingEntity player, Attribute attribute, UUID uuid) {
		removeModifier(player.getAttribute(attribute), uuid);
	}

	public static void addModifier(AttributeInstance attr, UUID uuid, double level, AttributeModifier.Operation op, boolean permanent) {
		removeModifier(attr, uuid);
		if (level == 0) return;

		Consumer<AttributeModifier> func = permanent
			? attr::addPermanentModifier
			: attr::addTransientModifier;

		func.accept(new AttributeModifier(
			uuid,
			"CrowdControl-" + uuid.toString(),
			level,
			op
		));
	}

	public static void addModifier(LivingEntity player, Attribute attribute, UUID uuid, double level, AttributeModifier.Operation op, boolean permanent) {
		AttributeInstance attr = player.getAttribute(attribute);
		if (attr == null) {
			// TODO: register

			logger.warn("Player missing {} attribute", attribute.getDescriptionId());
			return;
		}

		addModifier(attr, uuid, level, op, permanent);
	}
}
