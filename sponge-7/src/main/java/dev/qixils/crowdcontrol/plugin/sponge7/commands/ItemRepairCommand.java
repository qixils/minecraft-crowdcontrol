package dev.qixils.crowdcontrol.plugin.sponge7.commands;

import dev.qixils.crowdcontrol.plugin.sponge7.SpongeCrowdControlPlugin;
import org.spongepowered.api.data.value.mutable.MutableBoundedValue;

public final class ItemRepairCommand extends ItemDurabilityCommand {
	public ItemRepairCommand(SpongeCrowdControlPlugin plugin) {
		super(plugin, "repair_item");
	}

	@Override
	protected void modifyDurability(MutableBoundedValue<Integer> data, int maxDurability) {
		data.set(maxDurability);
	}
}
