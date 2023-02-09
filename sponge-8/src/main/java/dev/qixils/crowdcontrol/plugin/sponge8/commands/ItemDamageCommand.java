package dev.qixils.crowdcontrol.plugin.sponge8.commands;

import dev.qixils.crowdcontrol.plugin.sponge8.SpongeCrowdControlPlugin;
import org.spongepowered.api.data.value.Value;

public final class ItemDamageCommand extends ItemDurabilityCommand {
	public ItemDamageCommand(SpongeCrowdControlPlugin plugin) {
		super(plugin, "damage_item");
	}

	@Override
	protected void modifyDurability(Value.Mutable<Integer> data, int maxDurability) {
		data.set(data.get() / 2);
	}
}
