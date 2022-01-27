package dev.qixils.crowdcontrol.plugin.paper.commands;

import dev.qixils.crowdcontrol.plugin.paper.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.paper.PaperCrowdControlPlugin;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Blocking;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Getter
public class GiveItemCommand extends ImmediateCommand {
	private final Material item;
	private final String effectName;
	private final String displayName;

	public GiveItemCommand(PaperCrowdControlPlugin plugin, Material item) {
		super(plugin);
		this.item = item;
		this.effectName = "give_" + item.name();
		this.displayName = "Give " + plugin.getTextUtil().translate(item);
	}

	@Override
	public Response.@NotNull Builder executeImmediately(@NotNull List<@NotNull Player> players, @NotNull Request request) {
		ItemStack itemStack = new ItemStack(item);
		for (Player player : players) {
			sync(() -> giveItemTo(player, itemStack));
			// workaround to limit the circulation of end portal frames in the economy
			if (item == Material.END_PORTAL_FRAME)
				break;
		}
		return request.buildResponse().type(Response.ResultType.SUCCESS);
	}

	@Blocking
	public static void giveItemTo(Entity player, ItemStack itemStack) {
		Location location = player.getLocation();
		Item item = (Item) player.getWorld().spawnEntity(location, EntityType.DROPPED_ITEM);
		item.setItemStack(itemStack);
		item.setOwner(player.getUniqueId());
		item.setThrower(player.getUniqueId());
		item.setCanMobPickup(false);
		item.setCanPlayerPickup(true);
		item.setPickupDelay(0);
	}
}
