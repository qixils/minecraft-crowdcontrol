package dev.qixils.crowdcontrol.plugin.sponge8.commands;

import dev.qixils.crowdcontrol.common.command.CommandConstants;
import dev.qixils.crowdcontrol.plugin.sponge8.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.sponge8.SpongeCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.sponge8.utils.Slot;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import dev.qixils.crowdcontrol.socket.Response.ResultType;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.item.inventory.ItemStack;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Getter
public abstract class ItemDurabilityCommand extends ImmediateCommand {
	private final String effectName;

	protected ItemDurabilityCommand(SpongeCrowdControlPlugin plugin, String effectName) {
		super(plugin);
		this.effectName = effectName;
	}

	protected abstract void modifyDurability(Value.Mutable<Integer> data, int maxDurability);

	@Override
	public Response.@NotNull Builder executeImmediately(@NotNull List<@NotNull ServerPlayer> players, @NotNull Request request) {
		Response.Builder result = request.buildResponse()
				.type(ResultType.RETRY)
				.message("Targets not holding a durable item");

		// create list of random equipment slots
		List<Slot> slots = Arrays.asList(Slot.values());
		Collections.shuffle(slots);

		// loop through all players and all slots, and apply the durability change
		for (ServerPlayer player : players) {
			for (Slot slot : slots) {
				ItemStack item = slot.getItem(player);
				if (item.isEmpty())
					continue;
				if (!item.supports(Keys.ITEM_DURABILITY))
					continue;
				if (!item.supports(Keys.MAX_DURABILITY))
					continue;

				Value.Mutable<Integer> durabilityData = item.requireValue(Keys.ITEM_DURABILITY).asMutable();
				int current = durabilityData.get();
				int max = item.requireValue(Keys.MAX_DURABILITY).get();

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
