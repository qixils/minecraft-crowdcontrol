package dev.qixils.crowdcontrol.plugin.sponge8.commands;

import dev.qixils.crowdcontrol.common.LimitConfig;
import dev.qixils.crowdcontrol.plugin.sponge8.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.sponge8.SpongeCrowdControlPlugin;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import org.jetbrains.annotations.Blocking;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.Item;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.CauseStackManager.StackFrame;
import org.spongepowered.api.event.EventContextKeys;
import org.spongepowered.api.event.cause.entity.SpawnTypes;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.util.Ticks;

import java.util.List;

@Getter
public class GiveItemCommand extends ImmediateCommand {
	private final ItemType item;
	private final String effectName;
	private final String displayName;

	public GiveItemCommand(SpongeCrowdControlPlugin plugin, ItemType item) {
		super(plugin);
		this.item = item;
		this.effectName = "give_" + item.key(RegistryTypes.ITEM_TYPE).value();
		this.displayName = "Give " + plugin.getTextUtil().asPlain(item);
	}

	@Blocking
	public static void giveItemTo(SpongeCrowdControlPlugin plugin, Entity player, ItemStack itemStack) {
		Item entity = player.world().createEntity(EntityTypes.ITEM, player.position());
		entity.offer(Keys.ITEM_STACK_SNAPSHOT, itemStack.createSnapshot());
		entity.offer(Keys.CREATOR, player.uniqueId());
		entity.offer(Keys.PICKUP_DELAY, Ticks.of(0));

		// give entity a cause & spawn it
		try (StackFrame frame = plugin.getGame().server().causeStackManager().pushCauseFrame()) {
			frame.addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.PLUGIN);
			player.world().spawnEntity(entity);
		}
	}

	private void giveItemTo(Entity player, ItemStack itemStack) {
		giveItemTo(plugin, player, itemStack);
	}

	@NotNull
	@Override
	public Response.Builder executeImmediately(@NotNull List<@NotNull ServerPlayer> players, @NotNull Request request) {
		ItemStack itemStack = ItemStack.of(item);

		LimitConfig config = getPlugin().getLimitConfig();
		int maxRecipients = config.getItemLimit(item.key(RegistryTypes.ITEM_TYPE).value());

		sync(() -> {
			int recipients = 0;

			// first pass (hosts)
			for (ServerPlayer player : players) {
				if (!config.hostsBypass() && maxRecipients > -1 && recipients >= maxRecipients)
					break;
				if (!isHost(player))
					continue;

				giveItemTo(player, itemStack);
				recipients++;
			}

			// second pass (guests)
			for (ServerPlayer player : players) {
				if (maxRecipients > -1 && recipients >= maxRecipients)
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
