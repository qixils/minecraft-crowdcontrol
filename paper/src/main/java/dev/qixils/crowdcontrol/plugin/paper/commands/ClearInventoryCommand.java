package dev.qixils.crowdcontrol.plugin.paper.commands;

import dev.qixils.crowdcontrol.TriState;
import dev.qixils.crowdcontrol.plugin.paper.PaperCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.paper.RegionalCommandSync;
import live.crowdcontrol.cc4j.CCPlayer;
import live.crowdcontrol.cc4j.IUserRecord;
import live.crowdcontrol.cc4j.websocket.data.CCEffectResponse;
import live.crowdcontrol.cc4j.websocket.data.CCInstantEffectResponse;
import live.crowdcontrol.cc4j.websocket.data.ResponseStatus;
import live.crowdcontrol.cc4j.websocket.payload.PublicEffectPayload;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static dev.qixils.crowdcontrol.plugin.paper.commands.KeepInventoryCommand.globalKeepInventory;

@Getter
public class ClearInventoryCommand extends RegionalCommandSync {
	private final String effectName = "clear_inventory";

	public ClearInventoryCommand(PaperCrowdControlPlugin plugin) {
		super(plugin);
	}

	@Override
	protected @NotNull CCEffectResponse buildFailure(@NotNull PublicEffectPayload request, @NotNull CCPlayer ccPlayer) {
		return new CCInstantEffectResponse(request.getRequestId(), ResponseStatus.FAIL_TEMPORARY, "All inventories are already empty or protected");
	}

	@Override
	protected boolean executeRegionallySync(@NotNull Player player, @NotNull PublicEffectPayload request, @NotNull CCPlayer ccPlayer) {
		if (KeepInventoryCommand.isKeepingInventory(player))
			return false;

		PlayerInventory inv = player.getInventory();
		if (inv.isEmpty())
			return false;

		inv.clear();
		return true;
	}

	@Override
	public TriState isSelectable(@NotNull IUserRecord user, @NotNull List<Player> potentialPlayers) {
		if (!plugin.isGlobal())
			return TriState.TRUE;
		return globalKeepInventory ? TriState.FALSE : TriState.TRUE;
	}
}
