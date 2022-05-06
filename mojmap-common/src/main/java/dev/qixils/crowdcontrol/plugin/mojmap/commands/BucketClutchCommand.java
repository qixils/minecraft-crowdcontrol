package dev.qixils.crowdcontrol.plugin.mojmap.commands;

import dev.qixils.crowdcontrol.plugin.mojmap.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.mojmap.MojmapPlugin;
import dev.qixils.crowdcontrol.plugin.mojmap.utils.BlockFinder;
import dev.qixils.crowdcontrol.plugin.mojmap.utils.Location;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Getter
public class BucketClutchCommand extends ImmediateCommand {
	private static final int OFFSET = 30;

	public BucketClutchCommand(MojmapPlugin<?> plugin) {
		super(plugin);
	}

	private final String effectName = "bucket_clutch";
	private final String displayName = "Water Bucket Clutch";

	@Override
	public Response.@NotNull Builder executeImmediately(@NotNull List<@NotNull ServerPlayer> players, @NotNull Request request) {
		Response.Builder result = request.buildResponse()
				.type(Response.ResultType.RETRY)
				.message("No players are on the surface");
		for (ServerPlayer player : players) {
			if (player.getLevel().dimensionType().ultraWarm())
				continue;
			Location curr = new Location(player);
			boolean obstruction = false;
			for (int y = 1; y <= OFFSET; y++) {
				if (BlockFinder.isPassable(curr.add(0, y, 0)))
					continue;
				obstruction = true;
				break;
			}
			if (!obstruction) {
				result.type(Response.ResultType.SUCCESS).message("SUCCESS");
				Location dest = curr.add(0, OFFSET, 0);
				sync(() -> {
					player.teleportTo(dest.x(), dest.y(), dest.z());
					ItemStack hand = player.getMainHandItem();
					if (!hand.isEmpty() && hand.getItem() != Items.WATER_BUCKET) {
						ItemStack offhand = player.getOffhandItem();
						if (offhand.isEmpty()) {
							player.setItemInHand(InteractionHand.OFF_HAND, hand);
						} else {
							boolean slotFound = false;
							for (int i = 0; i <= 36; i++) {
								List<ItemStack> items = player.getInventory().items;
								ItemStack item = items.get(i);
								if (item.isEmpty()) {
									slotFound = true;
									items.set(i, hand);
									break;
								}
							}
							if (!slotFound)
								player.drop(true);
						}
					}
					player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.WATER_BUCKET));
				});
			}
		}
		return result;
	}
}
