package dev.qixils.crowdcontrol.plugin.paper.commands;

import dev.qixils.crowdcontrol.common.LimitConfig;
import dev.qixils.crowdcontrol.plugin.paper.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.paper.PaperCrowdControlPlugin;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import dev.qixils.crowdcontrol.socket.Response.ResultType;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@Getter
public class TakeItemCommand extends ImmediateCommand {
	private final Material item;
	private final String effectName;
	private final TranslatableComponent defaultDisplayName;

	public TakeItemCommand(PaperCrowdControlPlugin plugin, Material item) {
		super(plugin);
		this.item = item;
		this.effectName = "take_" + item.name();
		this.defaultDisplayName = Component.translatable("cc.effect.take_item.name", Component.translatable(new ItemStack(item)));
	}

	@Override
	public @NotNull Component getProcessedDisplayName(@NotNull Request request) {
		if (request.getParameters() == null)
			return getDefaultDisplayName();
		int amount = (int) (double) request.getParameters()[0];
		TranslatableComponent displayName = getDefaultDisplayName().key("cc.effect.take_item_x.name");
		List<Component> args = new ArrayList<>(displayName.args());
		args.add(Component.text(amount));
		return displayName.args(args);
	}

	private boolean takeItemFrom(Player player, int amount) {
		PlayerInventory inventory = player.getInventory();
		// simulate
		int toTake = 0;
		for (ItemStack itemStack : inventory) {
			if (itemStack == null) continue;
			if (itemStack.getType() != this.item) continue;
			toTake += itemStack.getAmount();
			if (toTake >= amount) break;
		}
		// do
		if (toTake < amount) return false;
		toTake = amount;
		for (int i = 0; i < inventory.getSize(); i++) {
			ItemStack itemStack = inventory.getItem(i);
			if (itemStack == null) continue;
			if (itemStack.getType() != this.item) continue;
			int take = Math.min(itemStack.getAmount(), toTake);
			itemStack.setAmount(itemStack.getAmount() - take);
			toTake -= take;
			if (toTake == 0) break;
		}
		return true;
	}

	@Override
	public Response.@NotNull Builder executeImmediately(@NotNull List<@NotNull Player> players, @NotNull Request request) {
		if (request.getParameters() == null)
			return request.buildResponse().type(Response.ResultType.UNAVAILABLE).message("CC is improperly configured and failing to send parameters");

		int amount = (int) (double) request.getParameters()[0];

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
			if (takeItemFrom(player, amount))
				victims++;
		}

		// second pass (guests)
		for (Player player : players) {
			if (maxVictims > 0 && victims >= maxVictims)
				break;
			if (isHost(player))
				continue;
			if (takeItemFrom(player, amount))
				victims++;
		}

		if (victims > 0)
			response.type(ResultType.SUCCESS).message("SUCCESS");

		return response;
	}
}
