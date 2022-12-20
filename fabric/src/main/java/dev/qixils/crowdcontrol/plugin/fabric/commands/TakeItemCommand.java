package dev.qixils.crowdcontrol.plugin.fabric.commands;

import dev.qixils.crowdcontrol.common.LimitConfig;
import dev.qixils.crowdcontrol.plugin.fabric.FabricCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.fabric.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.fabric.utils.InventoryUtil;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import dev.qixils.crowdcontrol.socket.Response.ResultType;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Getter
public class TakeItemCommand extends ImmediateCommand {
	private final Item item;
	private final String effectName;
	private final Component displayName;

	public TakeItemCommand(FabricCrowdControlPlugin plugin, Item item) {
		super(plugin);
		this.item = item;
		this.effectName = "take_" + BuiltInRegistries.ITEM.getKey(item).getPath();
		this.displayName = Component.translatable("cc.effect.take_item.name", item.getName(new ItemStack(item)));
	}

	private boolean takeItemFrom(Player player) {
		Inventory inventory = player.getInventory();
		for (ItemStack itemStack : InventoryUtil.viewAllItems(inventory)) {
			if (itemStack.isEmpty()) continue;
			if (itemStack.getItem() != this.item) continue;
			itemStack.setCount(itemStack.getCount() - 1);
			return true;
		}
		return false;
	}

	@NotNull
	@Override
	public Response.Builder executeImmediately(@NotNull List<@NotNull ServerPlayer> players, @NotNull Request request) {
		Response.Builder response = request.buildResponse()
				.type(ResultType.RETRY)
				.message("Item could not be found in target inventories");

		LimitConfig config = getPlugin().getLimitConfig();
		int maxVictims = config.getItemLimit(BuiltInRegistries.ITEM.getKey(item).getPath());
		int victims = 0;

		// first pass (hosts)
		for (ServerPlayer player : players) {
			if (!config.hostsBypass() && maxVictims > 0 && victims >= maxVictims)
				break;
			if (!isHost(player))
				continue;
			if (takeItemFrom(player))
				victims++;
		}

		// second pass (guests)
		for (ServerPlayer player : players) {
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
