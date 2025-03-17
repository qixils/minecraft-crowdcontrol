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
import org.bukkit.GameRule;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static dev.qixils.crowdcontrol.plugin.paper.commands.KeepInventoryCommand.globalKeepInventory;
import static dev.qixils.crowdcontrol.plugin.paper.commands.KeepInventoryCommand.isKeepingInventory;

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
	public TriState isVisible(@NotNull IUserRecord user, @NotNull List<Player> potentialPlayers) {
		// Cannot use inventory effects while /gamerule keepInventory true
		return potentialPlayers.stream()
			.anyMatch(player -> player.getWorld().getGameRuleValue(GameRule.KEEP_INVENTORY) == Boolean.TRUE)
			? TriState.FALSE
			: TriState.TRUE;
	}

	@Override
	public TriState isSelectable(@NotNull IUserRecord user, @NotNull List<Player> potentialPlayers) {
		if (plugin.isGlobal())
			return globalKeepInventory ? TriState.FALSE : TriState.TRUE;

		TriState keepingInventory = potentialPlayers.stream()
			.map(player -> TriState.fromBoolean(isKeepingInventory(player.getUniqueId())))
			.reduce((prev, next) -> {
				if (prev != next) return TriState.UNKNOWN;
				return prev;
			})
			.orElse(TriState.UNKNOWN);

		// if everyone is keeping inventory then no inventories can be cleared
		if (keepingInventory == TriState.TRUE) return TriState.FALSE;
		// some inventories can be cleared
		return TriState.TRUE;
	}
}
