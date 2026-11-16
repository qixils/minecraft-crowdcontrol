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
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

@Getter
public class SwapInventoryCommand extends ModdedCommand {
	private final String effectName = "swap_inventory";

	public SwapInventoryCommand(ModdedCrowdControlPlugin plugin) {
		super(plugin);
	}

	@Override
	public void execute(@NotNull Supplier<@NotNull List<@NotNull ServerPlayer>> playerSupplier, @NotNull PublicEffectPayload request, @NotNull CCPlayer ccPlayer) {
		ccPlayer.sendResponse(ThreadUtil.waitForSuccess(request, () -> {
			List<ServerPlayer> players = playerSupplier.get();
			if (players.size() < 2)
				return new CCInstantEffectResponse(request.getRequestId(), ResponseStatus.FAIL_TEMPORARY, "Not enough participating players online");

			// create a list offset by one
			List<ServerPlayer> offset = new ArrayList<>(players.size());
			offset.addAll(players.subList(1, players.size()));
			offset.add(players.getFirst());
			// get inventories
			Map<ServerPlayer, List<ItemStack>> destinations = new HashMap<>(players.size());
			for (int i = 0; i < players.size(); i++)
				destinations.put(players.get(i), InventoryUtil.viewAllItems(offset.get(i).inventory).stream().map(item -> item == null ? ItemStack.EMPTY : item.copy()).toList());
			// set inventories
			sync(() -> destinations.forEach((player, items) -> {
				var inv = player.inventory;
				for (int i = 0; i < items.size(); i++) {
					inv.setItem(i, items.get(i));
				}
			}));
			return new CCInstantEffectResponse(request.getRequestId(), ResponseStatus.SUCCESS);
		}));
	}

	@Override
	public TriState isSelectable(@NotNull IUserRecord user, @NotNull List<ServerPlayer> potentialPlayers) {
		return potentialPlayers.size() <= 1 ? TriState.FALSE : TriState.TRUE;
	}
}
