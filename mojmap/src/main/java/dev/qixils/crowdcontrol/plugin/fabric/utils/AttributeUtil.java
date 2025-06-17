package dev.qixils.crowdcontrol.plugin.fabric.utils;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

public class AttributeUtil {
	private static final Logger logger = LoggerFactory.getLogger("CrowdControl/AttributeUtil");

	public static ResourceLocation migrateId(UUID uuid) {
		return ResourceLocation.withDefaultNamespace(uuid.toString().toLowerCase(Locale.US));
	}

	public static Optional<AttributeModifier> getModifier(@Nullable AttributeInstance attr, UUID uuid) {
		if (attr == null) return Optional.empty();
		return Optional.ofNullable(attr.getModifier(migrateId(uuid)));
	}

	public static Optional<AttributeModifier> getModifier(LivingEntity player, Holder<Attribute> attribute, UUID uuid) {
		return getModifier(player.getAttribute(attribute), uuid);
	}

	public static void removeModifier(@Nullable AttributeInstance attr, UUID uuid) {
		if (attr == null) return;
		attr.removeModifier(migrateId(uuid));
	}

	public static void removeModifier(LivingEntity player, Holder<Attribute> attribute, UUID uuid) {
		removeModifier(player.getAttribute(attribute), uuid);
	}

	public static void addModifier(AttributeInstance attr, UUID uuid, double level, AttributeModifier.Operation op, boolean permanent) {
		removeModifier(attr, uuid);
		if (level == 0) return;

		Consumer<AttributeModifier> func = permanent
			? attr::addPermanentModifier
			: attr::addTransientModifier;

		func.accept(new AttributeModifier(
			migrateId(uuid),
			level,
			op
		));
	}

	public static void addModifier(LivingEntity player, Holder<Attribute> attributeHolder, UUID uuid, double level, AttributeModifier.Operation op, boolean permanent) {
		AttributeInstance attr = player.getAttribute(attributeHolder);
		if (attr == null) {
			// TODO: register

			logger.warn("Player missing {} attribute", attributeHolder.unwrapKey().orElse(null));
			return;
		}

		addModifier(attr, uuid, level, op, permanent);
	}
}
