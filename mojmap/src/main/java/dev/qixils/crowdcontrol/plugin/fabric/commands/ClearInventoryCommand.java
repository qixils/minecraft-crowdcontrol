package dev.qixils.crowdcontrol.plugin.fabric.commands;

import dev.qixils.crowdcontrol.TriState;
import dev.qixils.crowdcontrol.common.util.ThreadUtil;
import dev.qixils.crowdcontrol.plugin.fabric.ModdedCommand;
import dev.qixils.crowdcontrol.plugin.fabric.ModdedCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.fabric.utils.InventoryUtil;
import live.crowdcontrol.cc4j.CCPlayer;
import live.crowdcontrol.cc4j.IUserRecord;
import live.crowdcontrol.cc4j.websocket.data.CCInstantEffectResponse;
import live.crowdcontrol.cc4j.websocket.data.ResponseStatus;
import live.crowdcontrol.cc4j.websocket.payload.PublicEffectPayload;
import lombok.Getter;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Supplier;

import static dev.qixils.crowdcontrol.plugin.fabric.commands.KeepInventoryCommand.globalKeepInventory;
import static dev.qixils.crowdcontrol.plugin.fabric.commands.KeepInventoryCommand.isKeepingInventory;

@Getter
public class ClearInventoryCommand extends ModdedCommand {
	private final String effectName = "clear_inventory";

	public ClearInventoryCommand(ModdedCrowdControlPlugin plugin) {
		super(plugin);
	}

	@Override
	public void execute(@NotNull Supplier<@NotNull List<@NotNull ServerPlayer>> playerSupplier, @NotNull PublicEffectPayload request, @NotNull CCPlayer ccPlayer) {
		ccPlayer.sendResponse(ThreadUtil.waitForSuccess(request, () -> {
			boolean success = false;
			for (ServerPlayer player : playerSupplier.get()) {
				// ensure keep inventory is not enabled
				if (isKeepingInventory(player))
					continue;

				// ensure inventory is not empty
				Inventory inv = player.getInventory();
				boolean hasItems = false;
				for (ItemStack item : InventoryUtil.viewAllItems(inv)) {
					if (!item.isEmpty()) {
						hasItems = true;
						break;
					}
				}
				if (!hasItems)
					continue;

				// clear inventory
				success = true;
				sync(inv::clearContent);
			}
			return success
				? new CCInstantEffectResponse(request.getRequestId(), ResponseStatus.SUCCESS)
				: new CCInstantEffectResponse(request.getRequestId(), ResponseStatus.FAIL_TEMPORARY, "All inventories are already empty or protected");
		}));
	}

	@Override
	public TriState isVisible(@NotNull IUserRecord user, @NotNull List<ServerPlayer> potentialPlayers) {
		// Cannot use inventory effects while /gamerule keepInventory true
		return potentialPlayers.stream()
			.anyMatch(player -> player.level().getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY))
			? TriState.FALSE
			: TriState.TRUE;
	}

	@Override
	public TriState isSelectable(@NotNull IUserRecord user, @NotNull List<ServerPlayer> potentialPlayers) {
		if (plugin.isGlobal())
			return globalKeepInventory ? TriState.FALSE : TriState.TRUE;

		TriState keepingInventory = potentialPlayers.stream()
			.map(player -> TriState.fromBoolean(isKeepingInventory(player.getUUID())))
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
