package dev.qixils.crowdcontrol.plugin.sponge7.commands;

import dev.qixils.crowdcontrol.common.LimitConfig;
import dev.qixils.crowdcontrol.plugin.sponge7.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.sponge7.SpongeCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.sponge7.utils.SpongeTextUtil;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
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

import java.util.ArrayList;
import java.util.List;

@Getter
public class GiveItemCommand extends ImmediateCommand {
	private final ItemStackSnapshot item;
	private final String effectName;
	private final TranslatableComponent defaultDisplayName;

	public GiveItemCommand(SpongeCrowdControlPlugin plugin, ItemType item) {
		super(plugin);
		this.item = ItemStack.of(item).createSnapshot();
		this.effectName = "give_" + SpongeTextUtil.valueOf(item);
		this.defaultDisplayName = Component.translatable("cc.effect.give_item.name", Component.translatable(item.getTranslation().getId()));
	}

	public GiveItemCommand(SpongeCrowdControlPlugin plugin, ItemStack item, String effectName, Component arg) {
		super(plugin);
		this.item = item.createSnapshot();
		this.effectName = effectName;
		this.defaultDisplayName = Component.translatable("cc.effect.give_item.name", arg);
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
		int amount = request.getParameters() == null ? 1 : (int) (double) request.getParameters()[0];
		ItemStack itemStack = this.item.createStack();
		itemStack.setQuantity(amount);
		ItemStackSnapshot item = itemStack.createSnapshot();

		sync(() -> {
			LimitConfig config = getPlugin().getLimitConfig();
			int recipients = 0;
			int maxRecipients = config.getItemLimit(SpongeTextUtil.valueOf(item.getType()));

			// first pass (hosts)
			for (Player player : players) {
				if (!config.hostsBypass() && maxRecipients > 0 && recipients >= maxRecipients)
					break;
				if (!isHost(player))
					continue;

				giveItemTo(player, item);
				recipients++;
			}

			// second pass (guests)
			for (Player player : players) {
				if (maxRecipients > 0 && recipients >= maxRecipients)
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
