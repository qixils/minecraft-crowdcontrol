package dev.qixils.crowdcontrol.plugin.sponge8.commands;

import dev.qixils.crowdcontrol.plugin.sponge8.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.sponge8.SpongeCrowdControlPlugin;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.entity.PlayerInventory;
import org.spongepowered.api.item.inventory.transaction.InventoryTransactionResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

@Getter
public class DeleteRandomItemCommand extends ImmediateCommand {
	private final String effectName = "delete_random_item";

	public DeleteRandomItemCommand(SpongeCrowdControlPlugin plugin) {
		super(plugin);
	}
	@NotNull
	@Override
	public Response.Builder executeImmediately(@NotNull List<@NotNull ServerPlayer> players, @NotNull Request request) {
		Response.Builder result = request.buildResponse()
				.type(Response.ResultType.RETRY)
				.message("No players have items");
		for (ServerPlayer player : players) {
			PlayerInventory inv = player.inventory();
			List<Integer> indices = IntStream.range(0, inv.capacity()).collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
			Collections.shuffle(indices);
			for (int i : indices) {
				InventoryTransactionResult.Poll invRes = inv.pollFrom(i);
				if (invRes.type() != InventoryTransactionResult.Type.SUCCESS)
					continue;
				ItemStackSnapshot stack = invRes.polledItem();
				if (stack == null || stack.isEmpty() || stack.type().isAnyOf(ItemTypes.AIR.get()))
					continue;
				result.type(Response.ResultType.SUCCESS).message("SUCCESS");
			}
		}
		return result;
	}
}
