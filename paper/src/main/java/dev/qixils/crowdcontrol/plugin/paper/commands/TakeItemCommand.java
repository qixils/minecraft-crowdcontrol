package dev.qixils.crowdcontrol.plugin.paper.commands;

import dev.qixils.crowdcontrol.common.LimitConfig;
import dev.qixils.crowdcontrol.plugin.paper.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.paper.PaperCrowdControlPlugin;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import dev.qixils.crowdcontrol.socket.Response.ResultType;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Getter
public class TakeItemCommand extends ImmediateCommand {
	private final Material item;
	private final String effectName;
	private final String displayName;

	public TakeItemCommand(PaperCrowdControlPlugin plugin, Material item) {
		super(plugin);
		this.item = item;
		this.effectName = "take_" + item.name();
		this.displayName = "Take " + plugin.getTextUtil().translate(item);
	}

	private boolean takeItemFrom(Player player) {
		for (ItemStack itemStack : player.getInventory()) {
			if (itemStack == null) {
				continue;
			}
			if (itemStack.getType() == item) {
				itemStack.setAmount(itemStack.getAmount() - 1);
				return true;
			}
		}
		return false;
	}

	@Override
	public Response.@NotNull Builder executeImmediately(@NotNull List<@NotNull Player> players, @NotNull Request request) {
		Response.Builder response = request.buildResponse()
				.type(ResultType.RETRY)
				.message("Item could not be found in target inventories");

		LimitConfig config = getPlugin().getLimitConfig();
		int victims = 0;
		int maxVictims = config.getItemLimit(item.getKey().getKey());

		// first pass (hosts)
		for (Player player : players) {
			if (!config.hostsBypass() && maxVictims > 0 && victims >= maxVictims)
				break;
			if (!isHost(player))
				continue;
			if (takeItemFrom(player))
				victims++;
		}

		// second pass (guests)
		for (Player player : players) {
			if (maxVictims > 0 && victims >= maxVictims)
				break;
			if (isHost(player))
				continue;
			if (takeItemFrom(player))
				victims++;
		}

		if (victims > 0)
			response.type(ResultType.SUCCESS).message("SUCCESS");

		return response;
	}
}
