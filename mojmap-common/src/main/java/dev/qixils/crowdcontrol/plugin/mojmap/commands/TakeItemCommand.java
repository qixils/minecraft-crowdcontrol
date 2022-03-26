package dev.qixils.crowdcontrol.plugin.mojmap.commands;

import dev.qixils.crowdcontrol.plugin.mojmap.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.mojmap.MojmapPlugin;
import dev.qixils.crowdcontrol.plugin.mojmap.mixin.InventoryAccessor;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import dev.qixils.crowdcontrol.socket.Response.ResultType;
import lombok.Getter;
import net.minecraft.core.Registry;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Getter
public class TakeItemCommand extends ImmediateCommand {
	private final Item item;
	private final String effectName;
	private final String displayName;

	public TakeItemCommand(MojmapPlugin plugin, Item item) {
		super(plugin);
		this.item = item;
		this.effectName = "take_" + Registry.ITEM.getKey(item).getPath();
		this.displayName = "Take " + plugin.getTextUtil().asPlain(item.getName(new ItemStack(item)));
	}

	@NotNull
	@Override
	public Response.Builder executeImmediately(@NotNull List<@NotNull ServerPlayer> players, @NotNull Request request) {
		Response.Builder response = request.buildResponse()
				.type(ResultType.RETRY)
				.message("Item could not be found in target inventories");
		for (ServerPlayer player : players) {
			Inventory inventory = player.getInventory();
			loop: // TODO i hope i'm using this feature correctly
			for (List<ItemStack> items : ((InventoryAccessor) inventory).getCompartments()) {
				for (ItemStack itemStack : items) {
					if (itemStack.isEmpty()) continue;
					if (itemStack.getItem() != this.item) continue;
					response.type(ResultType.SUCCESS).message("SUCCESS");
					itemStack.setCount(itemStack.getCount() - 1);
					break loop;
				}
			}

			if (ResultType.SUCCESS == response.type() && item.equals(Items.END_PORTAL_FRAME))
				break;
		}
		return response;
	}
}
