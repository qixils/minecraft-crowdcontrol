package dev.qixils.crowdcontrol.plugin.sponge8.commands;

import dev.qixils.crowdcontrol.common.CommandConstants;
import dev.qixils.crowdcontrol.plugin.sponge8.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.sponge8.SpongeCrowdControlPlugin;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import dev.qixils.crowdcontrol.socket.Response.ResultType;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.data.manipulator.mutable.item.DurabilityData;
import org.spongepowered.api.data.property.item.UseLimitProperty;
import org.spongepowered.api.data.type.HandType;
import org.spongepowered.api.data.value.mutable.MutableBoundedValue;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.ItemStack;

import java.util.List;
import java.util.Optional;

@Getter
public abstract class ItemDurabilityCommand extends ImmediateCommand {
	private final String effectName;
	private final String displayName;

	protected ItemDurabilityCommand(SpongeCrowdControlPlugin plugin, String displayName) {
		super(plugin);
		this.displayName = displayName;
		this.effectName = displayName.replace(' ', '_');
	}

	protected abstract void modifyDurability(MutableBoundedValue<Integer> data, int maxDurability);

	@Override
	public Response.@NotNull Builder executeImmediately(@NotNull List<@NotNull Player> players, @NotNull Request request) {
		Response.Builder result = request.buildResponse()
				.type(ResultType.RETRY)
				.message("Targets not holding a durable item");

		for (Player player : players) {
			Optional<ItemStack> optionalItem = Optional.empty();
			for (HandType hand : plugin.getRegistry().getAllOf(HandType.class)) {
				Optional<ItemStack> heldItem = player.getItemInHand(hand);
				if (!heldItem.isPresent())
					continue;
				ItemStack heldItemStack = heldItem.get();
				if (!heldItemStack.isEmpty()
						&& heldItemStack.supports(DurabilityData.class)
						&& heldItemStack.getProperty(UseLimitProperty.class).isPresent()) {
					optionalItem = heldItem;
					break;
				}
			}

			if (!optionalItem.isPresent())
				continue;
			ItemStack item = optionalItem.get();

			//noinspection OptionalGetWithoutIsPresent - it is checked in the for loop
			MutableBoundedValue<Integer> data = item.get(DurabilityData.class).get().durability();
			//noinspection OptionalGetWithoutIsPresent - it is checked in the for loop
			Integer max = item.getProperty(UseLimitProperty.class).get().getValue();
			if (max == null)
				continue;

			// attempt to modify durability and ensure it was updated
			int current = data.get();
			modifyDurability(data, max);
			if (!CommandConstants.canApplyDurability(current, data.get(), max))
				continue;

			result.type(ResultType.SUCCESS).message("SUCCESS");
			item.offer(data);
		}

		return result;
	}
}
