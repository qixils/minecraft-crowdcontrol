package dev.qixils.crowdcontrol.plugin.paper.commands;

import dev.qixils.crowdcontrol.TriState;
import dev.qixils.crowdcontrol.common.util.ThreadUtil;
import dev.qixils.crowdcontrol.plugin.paper.PaperCommand;
import dev.qixils.crowdcontrol.plugin.paper.PaperCrowdControlPlugin;
import live.crowdcontrol.cc4j.CCPlayer;
import live.crowdcontrol.cc4j.IUserRecord;
import live.crowdcontrol.cc4j.websocket.data.CCInstantEffectResponse;
import live.crowdcontrol.cc4j.websocket.data.ResponseStatus;
import live.crowdcontrol.cc4j.websocket.payload.PublicEffectPayload;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Supplier;

@Getter
public class SwapInventoryCommand extends PaperCommand {
	private final String effectName = "swap_inventory";

	public SwapInventoryCommand(PaperCrowdControlPlugin plugin) {
		super(plugin);
	}

	@Override
	public void execute(@NotNull Supplier<@NotNull List<@NotNull Player>> playerSupplier, @NotNull PublicEffectPayload request, @NotNull CCPlayer ccPlayer) {
		ccPlayer.sendResponse(ThreadUtil.waitForSuccess(request, () -> {
			List<Player> players = playerSupplier.get();
			if (players.size() < 2)
				return new CCInstantEffectResponse(request.getRequestId(), ResponseStatus.FAIL_TEMPORARY, "Not enough participating players online");

			// get shuffled list of players
			Collections.shuffle(players, random);
			// create a list offset by one
			List<Player> offset = new ArrayList<>(players.size());
			offset.addAll(players.subList(1, players.size()));
			offset.add(players.getFirst());
			// get inventories
			Map<Player, List<ItemStack>> destinations = new HashMap<>(players.size());
			for (int i = 0; i < players.size(); ++i)
				destinations.put(players.get(i), Arrays.stream(offset.get(i).getInventory().getContents()).map(item -> item == null ? ItemStack.empty() : item.clone()).toList());
			// set inventories
			destinations.forEach((player, items) -> player.getScheduler().run(plugin.getPaperPlugin(), $ -> {
				var inv = player.getInventory();
				for (int i = 0; i < items.size(); i++) {
					inv.setItem(i, items.get(i));
				}
			}, null));
			return new CCInstantEffectResponse(request.getRequestId(), ResponseStatus.SUCCESS); // TODO: safer return / folia
		}));
	}

	@Override
	public TriState isSelectable(@NotNull IUserRecord user, @NotNull List<Player> potentialPlayers) {
		return plugin.getPlayerManager().getPotentialPlayers(user).count() <= 1L ? TriState.FALSE : TriState.TRUE;
	}
}
