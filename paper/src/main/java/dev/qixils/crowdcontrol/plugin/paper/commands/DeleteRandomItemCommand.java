package dev.qixils.crowdcontrol.plugin.paper.commands;

import dev.qixils.crowdcontrol.plugin.paper.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.paper.PaperCrowdControlPlugin;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

@Getter
public class DeleteRandomItemCommand extends ImmediateCommand {
	private final String effectName = "delete_random_item";

	public DeleteRandomItemCommand(PaperCrowdControlPlugin plugin) {
		super(plugin);
	}

	@Override
	public Response.@NotNull Builder executeImmediately(@NotNull List<@NotNull Player> players, @NotNull Request request) {
		Response.Builder result = request.buildResponse()
				.type(Response.ResultType.RETRY)
				.message("No players have items");
		for (Player player : players) {
			PlayerInventory inv = player.getInventory();
			List<Integer> indices = IntStream.range(0, inv.getSize()).collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
			Collections.shuffle(indices);
			for (int i : indices) {
				ItemStack stack = inv.getItem(i);
				if (stack == null || stack.getType().isAir() || stack.getAmount() == 0)
					continue;
				inv.setItem(i, null);
				result.type(Response.ResultType.SUCCESS).message("SUCCESS");
				break;
			}
		}
		return result;
	}
}
