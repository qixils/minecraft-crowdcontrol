package dev.qixils.crowdcontrol.plugin.fabric.commands;

import dev.qixils.crowdcontrol.common.LimitConfig;
import dev.qixils.crowdcontrol.plugin.fabric.FabricCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.fabric.ImmediateCommand;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Blocking;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Getter
public class GiveItemCommand extends ImmediateCommand {
	private final Item item;
	private final String effectName;
	private final Component displayName;

	public GiveItemCommand(FabricCrowdControlPlugin plugin, Item item) {
		super(plugin);
		this.item = item;
		this.effectName = "give_" + BuiltInRegistries.ITEM.getKey(item).getPath();
		this.displayName = Component.translatable("cc.effect.give_item.name", item.getName(new ItemStack(item)));
	}

	@Blocking
	public static void giveItemTo(Player player, ItemStack itemStack) {
		ItemEntity entity = player.spawnAtLocation(itemStack);
		if (entity == null)
			throw new IllegalStateException("Could not spawn item entity");
		entity.setOwner(player.getUUID());
		entity.setThrower(player.getUUID());
		entity.setPickUpDelay(0);
	}

	@NotNull
	@Override
	public Response.Builder executeImmediately(@NotNull List<@NotNull ServerPlayer> players, @NotNull Request request) {
		ItemStack itemStack = new ItemStack(item);

		LimitConfig config = getPlugin().getLimitConfig();
		int maxRecipients = config.getItemLimit(BuiltInRegistries.ITEM.getKey(item).getPath());

		sync(() -> {
			int recipients = 0;

			// first pass (hosts)
			for (ServerPlayer player : players) {
				if (!config.hostsBypass() && maxRecipients > 0 && recipients >= maxRecipients)
					break;
				if (!isHost(player))
					continue;
				giveItemTo(player, itemStack);
				recipients++;
			}

			// second pass (guests)
			for (ServerPlayer player : players) {
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