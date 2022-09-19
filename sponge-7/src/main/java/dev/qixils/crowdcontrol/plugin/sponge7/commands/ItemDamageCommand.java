package dev.qixils.crowdcontrol.plugin.sponge7.commands;

import dev.qixils.crowdcontrol.plugin.sponge7.SpongeCrowdControlPlugin;
import org.spongepowered.api.data.value.mutable.MutableBoundedValue;

public final class ItemDamageCommand extends ItemDurabilityCommand {
	public ItemDamageCommand(SpongeCrowdControlPlugin plugin) {
		super(plugin, "damage_item");
	}

	@Override
	protected void modifyDurability(MutableBoundedValue<Integer> data, int maxDurability) {
		data.set(data.get() / 2);
	}
}
