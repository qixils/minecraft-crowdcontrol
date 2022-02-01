package dev.qixils.crowdcontrol.plugin.sponge8.commands;

import dev.qixils.crowdcontrol.common.CommandConstants;
import dev.qixils.crowdcontrol.plugin.sponge8.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.sponge8.SpongeCrowdControlPlugin;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import dev.qixils.crowdcontrol.socket.Response.ResultType;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.type.HandType;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.registry.RegistryTypes;

import java.util.List;

@Getter
public abstract class ItemDurabilityCommand extends ImmediateCommand {
	private final String effectName;
	private final String displayName;

	protected ItemDurabilityCommand(SpongeCrowdControlPlugin plugin, String displayName) {
		super(plugin);
		this.displayName = displayName;
		this.effectName = displayName.replace(' ', '_');
	}

	protected abstract void modifyDurability(Value.Mutable<Integer> data, int maxDurability);

	@Override
	public Response.@NotNull Builder executeImmediately(@NotNull List<@NotNull ServerPlayer> players, @NotNull Request request) {
		Response.Builder result = request.buildResponse()
				.type(ResultType.RETRY)
				.message("Targets not holding a durable item");

		for (ServerPlayer player : players) {
			for (HandType hand : plugin.registryIterable(RegistryTypes.HAND_TYPE)) {
				ItemStack item = player.itemInHand(hand);
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
