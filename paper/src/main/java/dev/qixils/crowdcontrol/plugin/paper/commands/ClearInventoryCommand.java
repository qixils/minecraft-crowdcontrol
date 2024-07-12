package dev.qixils.crowdcontrol.plugin.paper.commands;

import dev.qixils.crowdcontrol.TriState;
import dev.qixils.crowdcontrol.plugin.paper.PaperCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.paper.RegionalCommandSync;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import dev.qixils.crowdcontrol.socket.Response.ResultType;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;

import static dev.qixils.crowdcontrol.plugin.paper.commands.KeepInventoryCommand.globalKeepInventory;

@Getter
public class ClearInventoryCommand extends RegionalCommandSync {
	private final String effectName = "clear_inventory";

	public ClearInventoryCommand(PaperCrowdControlPlugin plugin) {
		super(plugin);
	}

	@Override
	protected Response.@NotNull Builder buildFailure(@NotNull Request request) {
		return request.buildResponse()
			.type(ResultType.RETRY)
			.message("All inventories are already empty or protected");
	}

	@Override
	protected boolean executeRegionallySync(@NotNull Player player, @NotNull Request request) {
		if (KeepInventoryCommand.isKeepingInventory(player))
			return false;

		PlayerInventory inv = player.getInventory();
		if (inv.isEmpty())
			return false;

		inv.clear();
		return true;
	}

	@Override
	public TriState isSelectable() {
		if (!plugin.isGlobal())
			return TriState.TRUE;
		return globalKeepInventory ? TriState.FALSE : TriState.TRUE;
	}
}
