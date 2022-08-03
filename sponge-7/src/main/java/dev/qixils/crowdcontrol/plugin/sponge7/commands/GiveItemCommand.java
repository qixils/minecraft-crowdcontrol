package dev.qixils.crowdcontrol.plugin.sponge7.commands;

import dev.qixils.crowdcontrol.common.LimitConfig;
import dev.qixils.crowdcontrol.plugin.sponge7.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.sponge7.SpongeCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.sponge7.utils.SpongeTextUtil;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import org.jetbrains.annotations.Blocking;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.Item;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.CauseStackManager.StackFrame;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.cause.entity.spawn.SpawnTypes;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;

import java.util.List;

@Getter
public class GiveItemCommand extends ImmediateCommand {
	private final ItemStackSnapshot item;
	private final String effectName;
	private final String displayName;

	public GiveItemCommand(SpongeCrowdControlPlugin plugin, ItemType item) {
		super(plugin);
		this.item = ItemStack.of(item).createSnapshot();
		this.effectName = "give_" + SpongeTextUtil.valueOf(item);
		this.displayName = "Give " + item.getTranslation().get();
	}

	public GiveItemCommand(SpongeCrowdControlPlugin plugin, ItemStack item, String effectName, String displayName) {
		super(plugin);
		this.item = item.createSnapshot();
		this.effectName = effectName;
		this.displayName = displayName;
	}

	@Blocking
	public static void giveItemTo(SpongeCrowdControlPlugin plugin, Entity player, ItemStackSnapshot item) {
		Item entity = (Item) player.getLocation().createEntity(EntityTypes.ITEM);
		entity.offer(Keys.REPRESENTED_ITEM, item);
		entity.offer(Keys.PICKUP_DELAY, 0);

		// give entity a cause & spawn it
		try (StackFrame frame = plugin.getGame().getCauseStackManager().pushCauseFrame()) {
			frame.addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.PLUGIN);
			player.getWorld().spawnEntity(entity);
		}
	}

	private void giveItemTo(Entity player, ItemStackSnapshot item) {
		giveItemTo(plugin, player, item);
	}

	@NotNull
	@Override
	public Response.Builder executeImmediately(@NotNull List<@NotNull Player> players, @NotNull Request request) {
		sync(() -> {
			LimitConfig config = getPlugin().getLimitConfig();
			int recipients = 0;
			int maxRecipients = config.getItemLimit(SpongeTextUtil.valueOf(item.getType()));

			// first pass (hosts)
			for (Player player : players) {
				if (!config.hostsBypass() && maxRecipients > -1 && recipients >= maxRecipients)
					break;
				if (!isHost(player))
					continue;

				giveItemTo(player, item);
				recipients++;
			}

			// second pass (guests)
			for (Player player : players) {
				if (maxRecipients > -1 && recipients >= maxRecipients)
					break;
				if (isHost(player))
					continue;

				giveItemTo(player, item);
				recipients++;
			}
		});
		return request.buildResponse().type(Response.ResultType.SUCCESS);
	}
}
