package dev.qixils.crowdcontrol.plugin.sponge8.commands;

import dev.qixils.crowdcontrol.plugin.sponge8.SpongeCrowdControlPlugin;
import org.spongepowered.api.data.value.Value;

import static dev.qixils.crowdcontrol.common.command.CommandConstants.ITEM_DAMAGE_PERCENTAGE;

public final class ItemDamageCommand extends ItemDurabilityCommand {
	public ItemDamageCommand(SpongeCrowdControlPlugin plugin) {
		super(plugin, "damage_item");
	}

	@Override
	protected void modifyDurability(Value.Mutable<Integer> data, int maxDurability) {
		data.set(data.get() - (maxDurability / ITEM_DAMAGE_PERCENTAGE));
	}
}
