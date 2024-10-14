package dev.qixils.crowdcontrol.plugin.sponge7.commands;

import dev.qixils.crowdcontrol.common.command.CommandConstants;
import dev.qixils.crowdcontrol.plugin.sponge7.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.sponge7.SpongeCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.sponge7.utils.Slot;
import dev.qixils.crowdcontrol.socket.Response;
import dev.qixils.crowdcontrol.socket.Response.ResultType;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.mutable.item.DurabilityData;
import org.spongepowered.api.data.property.item.UseLimitProperty;
import org.spongepowered.api.data.value.mutable.MutableBoundedValue;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.ItemStack;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Getter
public abstract class ItemDurabilityCommand extends ImmediateCommand {
	private final String effectName;

	protected ItemDurabilityCommand(SpongeCrowdControlPlugin plugin, String effectName) {
		super(plugin);
		this.effectName = effectName;
	}

	protected abstract void modifyDurability(MutableBoundedValue<Integer> data, int maxDurability);

	@Override
	public Response.@NotNull Builder executeImmediately(@NotNull List<@NotNull Player> players, @NotNull Request request) {
		Response.Builder result = request.buildResponse()
				.type(ResultType.RETRY)
				.message("Targets not holding a durable item");

		// create list of random equipment slots
		List<Slot> slots = Arrays.asList(Slot.values());
		Collections.shuffle(slots);

		// loop through all players and all slots, and apply the durability change
		for (Player player : players) {
			for (Slot slot : slots) {
				Optional<ItemStack> optionalItem = slot.getItem(player);
				if (!optionalItem.isPresent())
					continue;
				ItemStack item = optionalItem.get();
				if (item.isEmpty())
					continue;
				if (item.getOrElse(Keys.UNBREAKABLE, false))
					continue;
				if (!item.supports(DurabilityData.class))
					continue;
				Optional<UseLimitProperty> useLimitProperty = item.getProperty(UseLimitProperty.class);
				if (!useLimitProperty.isPresent())
					continue;
				Integer _max = useLimitProperty.get().getValue();
				if (_max == null)
					continue;

				MutableBoundedValue<Integer> durabilityData = item.require(DurabilityData.class).durability();
				int current = durabilityData.get();
				int max = _max;

				// attempt to modify durability and ensure it was updated
				modifyDurability(durabilityData, max);
				if (!CommandConstants.canApplyDurability(current, durabilityData.get(), max))
					continue;

				result.type(ResultType.SUCCESS).message("SUCCESS");
				item.offer(durabilityData);
				break;
			}
		}

		return result;
	}
}
