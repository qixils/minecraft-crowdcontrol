package dev.qixils.crowdcontrol.plugin.paper.commands;

import dev.qixils.crowdcontrol.common.LimitConfig;
import dev.qixils.crowdcontrol.plugin.paper.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.paper.PaperCrowdControlPlugin;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Blocking;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@Getter
public class GiveItemCommand extends ImmediateCommand {
	private final Material item;
	private final String effectName;
	private final TranslatableComponent defaultDisplayName;

	public GiveItemCommand(PaperCrowdControlPlugin plugin, Material item) {
		super(plugin);
		this.item = item;
		this.effectName = "give_" + item.name();
		this.defaultDisplayName = Component.translatable("cc.effect.give_item.name", Component.translatable(new ItemStack(item)));
	}

	@Override
	public @NotNull Component getProcessedDisplayName(@NotNull Request request) {
		if (request.getParameters() == null)
			return getDefaultDisplayName();
		int amount = (int) (double) request.getParameters()[0];
		TranslatableComponent displayName = getDefaultDisplayName().key("cc.effect.give_item_x.name");
		List<Component> args = new ArrayList<>(displayName.args());
		args.add(Component.text(amount));
		return displayName.args(args);
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

	@Override
	public Response.@NotNull Builder executeImmediately(@NotNull List<@NotNull Player> players, @NotNull Request request) {
		if (request.getParameters() == null)
			return request.buildResponse().type(Response.ResultType.UNAVAILABLE).message("CC is improperly configured and failing to send parameters");

		int amount = (int) (double) request.getParameters()[0];
		ItemStack itemStack = new ItemStack(item, amount);

		sync(() -> {
			LimitConfig config = getPlugin().getLimitConfig();
			int recipients = 0;
			int maxRecipients = config.getItemLimit(item.getKey().getKey());

			// first pass (hosts)
			for (Player player : players) {
				if (!config.hostsBypass() && maxRecipients > 0 && recipients >= maxRecipients)
					break;
				if (!isHost(player))
					continue;

				giveItemTo(player, itemStack);
				recipients++;
			}

			// second pass (guests)
			for (Player player : players) {
				if (maxRecipients > 0 && recipients >= maxRecipients)
					break;
				if (isHost(player))
					continue;

				giveItemTo(player, itemStack);
				recipients++;
			}
		});
		return request.buildResponse().type(Response.ResultType.SUCCESS);
	}
}
