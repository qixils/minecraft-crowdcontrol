package dev.qixils.crowdcontrol.plugin.fabric.commands;

import dev.qixils.crowdcontrol.TriState;
import dev.qixils.crowdcontrol.plugin.fabric.FabricCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.fabric.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.fabric.utils.InventoryUtil;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import dev.qixils.crowdcontrol.socket.Response.ResultType;
import lombok.Getter;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static dev.qixils.crowdcontrol.plugin.fabric.commands.KeepInventoryCommand.globalKeepInventory;

@Getter
public class ClearInventoryCommand extends ImmediateCommand {
	private final String effectName = "clear_inventory";

	public ClearInventoryCommand(FabricCrowdControlPlugin plugin) {
		super(plugin);
	}

	@Override
	@NotNull
	public Response.Builder executeImmediately(@NotNull List<@NotNull ServerPlayer> players, @NotNull Request request) {
		Response.Builder resp = request.buildResponse()
				.type(ResultType.RETRY)
				.message("All inventories are already empty or protected");
		for (ServerPlayer player : players) {
			// ensure keep inventory is not enabled
			if (KeepInventoryCommand.isKeepingInventory(player)) {
				resp.type(ResultType.FAILURE);
				continue;
			}
			Inventory inv = player.getInventory();
			// ensure inventory is not empty
			boolean hasItems = false;
			for (ItemStack item : InventoryUtil.viewAllItems(inv)) {
				if (!item.isEmpty()) {
					hasItems = true;
					break;
				}
			}
			if (!hasItems) continue;
			// clear inventory
			resp.type(ResultType.SUCCESS).message("SUCCESS");
			sync(inv::clearContent);
		}
		return resp;
	}

	@Override
	public TriState isSelectable() {
		if (!plugin.isGlobal())
			return TriState.TRUE;
		return globalKeepInventory ? TriState.FALSE : TriState.TRUE;
	}
}
