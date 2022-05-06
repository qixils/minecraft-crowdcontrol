package dev.qixils.crowdcontrol.plugin.mojmap.commands;

import dev.qixils.crowdcontrol.plugin.mojmap.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.mojmap.MojmapPlugin;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import net.minecraft.core.Registry;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Blocking;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Getter
public class GiveItemCommand extends ImmediateCommand {
	private final Item item;
	private final String effectName;
	private final String displayName;

	public GiveItemCommand(MojmapPlugin<?> plugin, Item item) {
		super(plugin);
		this.item = item;
		this.effectName = "give_" + Registry.ITEM.getKey(item).getPath();
		this.displayName = "Give " + plugin.getTextUtil().asPlain(item.getName(new ItemStack(item)));
	}

	@Blocking
	public static void giveItemTo(MojmapPlugin<?> plugin, Player player, ItemStack itemStack) {
		ItemEntity entity = player.spawnAtLocation(itemStack);
		if (entity == null)
			throw new IllegalStateException("Could not spawn item entity");
		entity.setOwner(player.getUUID());
		entity.setThrower(player.getUUID());
		entity.setPickUpDelay(0);
	}

	private void giveItemTo(Player player, ItemStack itemStack) {
		giveItemTo(plugin, player, itemStack);
	}

	@NotNull
	@Override
	public Response.Builder executeImmediately(@NotNull List<@NotNull ServerPlayer> players, @NotNull Request request) {
		ItemStack itemStack = new ItemStack(item);
		sync(() -> {
			for (ServerPlayer player : players) {
				giveItemTo(player, itemStack);
				// workaround to limit the circulation of end portal frames in the economy
				if (item.equals(Items.END_PORTAL_FRAME))
					break;
			}
		});
		return request.buildResponse().type(Response.ResultType.SUCCESS);
	}
}
