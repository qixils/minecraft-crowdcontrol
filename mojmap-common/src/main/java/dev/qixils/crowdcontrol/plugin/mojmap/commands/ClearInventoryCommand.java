package dev.qixils.crowdcontrol.plugin.mojmap.commands;

import dev.qixils.crowdcontrol.plugin.mojmap.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.mojmap.MojmapPlugin;
import dev.qixils.crowdcontrol.plugin.mojmap.mixin.InventoryAccessor;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import dev.qixils.crowdcontrol.socket.Response.ResultType;
import lombok.Getter;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@SuppressWarnings("ReferenceToMixin")
@Getter
public class ClearInventoryCommand extends ImmediateCommand {
	private final String effectName = "clear_inventory";
	private final String displayName = "Clear Inventory";

	public ClearInventoryCommand(MojmapPlugin<?> plugin) {
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
			if (KeepInventoryCommand.isKeepingInventory(player)) continue;
			Inventory inv = player.getInventory();
			// ensure inventory is not empty
			boolean hasItems = false;
			for (ItemStack item : ((InventoryAccessor) inv).viewAllItems()) {
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
}