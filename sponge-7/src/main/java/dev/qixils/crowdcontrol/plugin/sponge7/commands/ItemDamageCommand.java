package dev.qixils.crowdcontrol.plugin.sponge7.commands;

import dev.qixils.crowdcontrol.plugin.sponge7.SpongeCrowdControlPlugin;
import org.spongepowered.api.data.value.mutable.MutableBoundedValue;

import static dev.qixils.crowdcontrol.common.command.CommandConstants.ITEM_DAMAGE_PERCENTAGE;

public final class ItemDamageCommand extends ItemDurabilityCommand {
	public ItemDamageCommand(SpongeCrowdControlPlugin plugin) {
		super(plugin, "damage_item");
	}

	@Override
	protected void modifyDurability(MutableBoundedValue<Integer> data, int maxDurability) {
		data.set(data.get() - (maxDurability / ITEM_DAMAGE_PERCENTAGE));
	}
}
