package dev.qixils.crowdcontrol.plugin.paper.commands;

import dev.qixils.crowdcontrol.plugin.paper.PaperCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.paper.RegionalCommandSync;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Getter
public class DeleteRandomItemCommand extends RegionalCommandSync {
	private final String effectName = "delete_random_item";

	public DeleteRandomItemCommand(PaperCrowdControlPlugin plugin) {
		super(plugin);
	}

	@Override
	protected Response.@NotNull Builder buildFailure(@NotNull Request request) {
		return request.buildResponse()
			.type(Response.ResultType.RETRY)
			.message("No players have items");
	}

	@Override
	protected boolean executeRegionallySync(@NotNull Player player, @NotNull Request request) {
		PlayerInventory inv = player.getInventory();
		List<Integer> indices = IntStream.range(0, inv.getSize()).boxed().collect(Collectors.toList()); // TODO: this still sucks
		Collections.shuffle(indices);
		for (int i : indices) {
			ItemStack stack = inv.getItem(i);
			if (stack == null || stack.getType().isAir() || stack.getAmount() == 0)
				continue;
			inv.setItem(i, null);
			return true;
		}
		return false;
	}
}
