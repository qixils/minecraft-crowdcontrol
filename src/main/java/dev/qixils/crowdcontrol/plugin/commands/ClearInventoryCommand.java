package dev.qixils.crowdcontrol.plugin.commands;

import dev.qixils.crowdcontrol.plugin.CrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.ImmediateCommand;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import dev.qixils.crowdcontrol.socket.Response.ResultType;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Getter
public class ClearInventoryCommand extends ImmediateCommand {
	private final String effectName = "clear_inventory";
	private final String displayName = "Clear Inventory";

	public ClearInventoryCommand(CrowdControlPlugin plugin) {
		super(plugin);
	}

	@Override
	protected @NotNull Response.Builder executeImmediately(@NotNull List<@NotNull Player> players, @NotNull Request request) {
		Response.Builder resp = request.buildResponse().type(ResultType.FAILURE).message("");
		for (Player player : players) {
			if (KeepInventoryCommand.isKeepingInventory(player)) continue;
			PlayerInventory inv = player.getInventory();
			if (inv.isEmpty()) continue;
			resp.type(ResultType.SUCCESS).message("SUCCESS");
			Bukkit.getScheduler().runTask(plugin, (@NotNull Runnable) inv::clear);
		}
		return resp;
	}
}
