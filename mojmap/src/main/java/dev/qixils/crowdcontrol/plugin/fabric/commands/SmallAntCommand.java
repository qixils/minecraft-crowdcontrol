package dev.qixils.crowdcontrol.plugin.fabric.commands;

import dev.qixils.crowdcontrol.plugin.fabric.ModdedCrowdControlPlugin;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.spider.Spider;
import org.jetbrains.annotations.Blocking;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static dev.qixils.crowdcontrol.common.command.CommandConstants.*;
import static dev.qixils.crowdcontrol.plugin.fabric.utils.AttributeUtil.addModifier;

@Getter
public class SmallAntCommand extends SummonEntityCommand<Spider> {
	public SmallAntCommand(ModdedCrowdControlPlugin plugin) {
		super(plugin, "entity_small_ant", null, EntityType.SPIDER);
	}

	@Override
	@Blocking
	protected Spider spawnEntity(@Nullable Component viewer, @NotNull ServerPlayer player) {
		Spider spider = super.spawnEntity(viewer, player);
		if (spider == null) return null;

		addModifier(spider, Attributes.SCALE, ANT_SCALE_MODIFIER_UUID, -0.6, AttributeModifier.Operation.ADD_MULTIPLIED_BASE, true);
		addModifier(spider, Attributes.MOVEMENT_SPEED, ANT_SPEED_MODIFIER_UUID, 0.4, AttributeModifier.Operation.ADD_MULTIPLIED_BASE, true);
		addModifier(spider, Attributes.STEP_HEIGHT, ANT_STEP_MODIFIER_UUID, 1, AttributeModifier.Operation.ADD_MULTIPLIED_BASE, true);
		addModifier(spider, Attributes.ENTITY_INTERACTION_RANGE, ANT_RANGE_MODIFIER_UUID, -0.5, AttributeModifier.Operation.ADD_MULTIPLIED_BASE, true);

		return spider;
	}
}
