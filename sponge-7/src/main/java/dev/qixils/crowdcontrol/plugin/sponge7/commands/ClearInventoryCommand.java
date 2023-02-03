package dev.qixils.crowdcontrol.plugin.sponge7.commands;

import dev.qixils.crowdcontrol.TriState;
import dev.qixils.crowdcontrol.plugin.sponge7.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.sponge7.SpongeCrowdControlPlugin;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import dev.qixils.crowdcontrol.socket.Response.ResultType;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.type.CarriedInventory;

import java.util.List;

import static dev.qixils.crowdcontrol.plugin.sponge7.commands.KeepInventoryCommand.globalKeepInventory;

@Getter
public class ClearInventoryCommand extends ImmediateCommand {
	private final String effectName = "clear_inventory";

	public ClearInventoryCommand(SpongeCrowdControlPlugin plugin) {
		super(plugin);
	}

	@Override
	@NotNull
	public Response.Builder executeImmediately(@NotNull List<@NotNull Player> players, @NotNull Request request) {
		Response.Builder resp = request.buildResponse()
				.type(ResultType.RETRY)
				.message("All inventories are already empty or protected");
		for (Player player : players) {
			if (KeepInventoryCommand.isKeepingInventory(player)) continue;
			CarriedInventory<?> inv = player.getInventory();
			if (inv.size() == 0) continue;
			resp.type(ResultType.SUCCESS).message("SUCCESS");
			sync(inv::clear);
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
