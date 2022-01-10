package dev.qixils.crowdcontrol.plugin.commands;

import dev.qixils.crowdcontrol.plugin.SpongeCrowdControlPlugin;
import org.spongepowered.api.data.value.mutable.MutableBoundedValue;

public final class ItemDamageCommand extends ItemDurabilityCommand {
	public ItemDamageCommand(SpongeCrowdControlPlugin plugin) {
		super(plugin, "Damage Item");
	}

	@Override
	protected void modifyDurability(MutableBoundedValue<Integer> data, int maxDurability) {
		data.set(data.get() / 2);
	}
}
