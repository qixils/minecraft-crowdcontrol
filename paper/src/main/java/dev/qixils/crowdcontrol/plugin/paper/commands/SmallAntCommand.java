package dev.qixils.crowdcontrol.plugin.paper.commands;

import dev.qixils.crowdcontrol.plugin.paper.PaperCrowdControlPlugin;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Spider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static dev.qixils.crowdcontrol.common.command.CommandConstants.*;
import static dev.qixils.crowdcontrol.plugin.paper.utils.AttributeUtil.addModifier;

@Getter
public class SmallAntCommand extends SummonEntityCommand {
	private final String effectName = "entity_small_ant";
	private final Component displayName = getDefaultDisplayName();

	public SmallAntCommand(PaperCrowdControlPlugin plugin) {
		super(plugin, EntityType.SPIDER);
	}

	@Override
	protected Entity spawnEntity(@Nullable Component viewer, @NotNull Player player) {
		Spider spider = (Spider) super.spawnEntity(viewer, player);
		if (spider == null) return null;

		addModifier(spider, Attribute.SCALE, ANT_SCALE_MODIFIER_UUID, ANT_SCALE_MODIFIER_NAME, -0.6, AttributeModifier.Operation.ADD_SCALAR, true);
		addModifier(spider, Attribute.MOVEMENT_SPEED, ANT_SPEED_MODIFIER_UUID, ANT_SPEED_MODIFIER_NAME, 0.4, AttributeModifier.Operation.ADD_SCALAR, true);
		addModifier(spider, Attribute.STEP_HEIGHT, ANT_STEP_MODIFIER_UUID, ANT_STEP_MODIFIER_NAME, 1, AttributeModifier.Operation.ADD_SCALAR, true);
		addModifier(spider, Attribute.ENTITY_INTERACTION_RANGE, ANT_RANGE_MODIFIER_UUID, ANT_RANGE_MODIFIER_NAME, -0.5, AttributeModifier.Operation.ADD_SCALAR, true);

		return spider;
	}
}
