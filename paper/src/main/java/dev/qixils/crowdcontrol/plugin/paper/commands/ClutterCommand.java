package dev.qixils.crowdcontrol.plugin.paper.commands;

import dev.qixils.crowdcontrol.plugin.paper.PaperCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.paper.RegionalCommand;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Getter
public class ClutterCommand extends RegionalCommand {
	private final String effectName = "clutter";

	public ClutterCommand(PaperCrowdControlPlugin plugin) {
		super(plugin);
	}

	@Override
	protected boolean executeRegionally(Player player, Request request) {
		PlayerInventory inventory = player.getInventory();
		ItemStack[] items = inventory.getContents();
		List<ItemStack> list = new ArrayList<>(Arrays.asList(items));
		Collections.shuffle(list);
		ItemStack[] shuffled = list.toArray(new ItemStack[0]);

		if (Arrays.equals(items, shuffled)) return false;

		inventory.setContents(shuffled);
		player.updateInventory();
		return true;
	}

	@Override
	protected Response.@NotNull Builder buildFailure(Request request) {
		return request.buildResponse().type(Response.ResultType.RETRY).message("Could not find items to swap");
	}
}
