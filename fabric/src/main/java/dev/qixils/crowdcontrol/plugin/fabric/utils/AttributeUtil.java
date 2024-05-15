package dev.qixils.crowdcontrol.plugin.fabric.utils;

import net.minecraft.core.Holder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;
import java.util.function.Consumer;

public class AttributeUtil {
	private static final Logger logger = LoggerFactory.getLogger("CrowdControl/AttributeUtil");

	public static void removeModifier(AttributeInstance attr, UUID uuid) {
		if (attr == null) return;
		for (AttributeModifier attributeModifier : attr.getModifiers()) {
			if (attributeModifier.id().equals(uuid)) {
				attr.removePermanentModifier(uuid);
				break; // avoid CME or whatever it's called
			}
		}
	}

	public static void removeModifier(LivingEntity player, Holder<Attribute> attribute, UUID uuid) {
		removeModifier(player.getAttribute(attribute), uuid);
	}

	public static void addModifier(AttributeInstance attr, UUID uuid, String name, double level, AttributeModifier.Operation op, boolean permanent) {
		removeModifier(attr, uuid);
		if (level == 0) return;

		Consumer<AttributeModifier> func = permanent
			? attr::addTransientModifier
			: attr::addPermanentModifier;

		func.accept(new AttributeModifier(
			uuid,
			name,
			level,
			op
		));
	}

	public static void addModifier(LivingEntity player, Holder<Attribute> attributeHolder, UUID uuid, String name, double level, AttributeModifier.Operation op, boolean permanent) {
		AttributeInstance attr = player.getAttribute(attributeHolder);
		if (attr == null) {
			logger.warn("Player missing {} attribute", attributeHolder.unwrapKey().orElse(null));
			return;
		}

		addModifier(attr, uuid, name, level, op, permanent);
	}
}
