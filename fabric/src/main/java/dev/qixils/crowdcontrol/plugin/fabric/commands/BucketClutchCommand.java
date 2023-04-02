package dev.qixils.crowdcontrol.plugin.fabric.commands;

import dev.qixils.crowdcontrol.plugin.fabric.FabricCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.fabric.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.fabric.utils.BlockFinder;
import dev.qixils.crowdcontrol.plugin.fabric.utils.Location;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Getter
public class BucketClutchCommand extends ImmediateCommand {
	private static final int OFFSET = 30;

	public BucketClutchCommand(FabricCrowdControlPlugin plugin) {
		super(plugin);
	}

	private final String effectName = "bucket_clutch";

	@Override
	public Response.@NotNull Builder executeImmediately(@NotNull List<@NotNull ServerPlayerEntity> players, @NotNull Request request) {
		Response.Builder result = request.buildResponse()
				.type(Response.ResultType.RETRY)
				.message("No players are on the surface");
		for (ServerPlayerEntity player : players) {
			if (player.getWorld().getDimension().ultrawarm())
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
					player.requestTeleport(dest.x(), dest.y(), dest.z());
					ItemStack hand = player.getMainHandStack();
					if (!hand.isEmpty() && hand.getItem() != Items.WATER_BUCKET) {
						ItemStack offhand = player.getOffHandStack();
						if (offhand.isEmpty()) {
							player.setStackInHand(Hand.OFF_HAND, hand);
						} else {
							boolean slotFound = false;
							for (int i = 0; i <= 36; i++) {
								List<ItemStack> items = player.getInventory().main;
								ItemStack item = items.get(i);
								if (item.isEmpty()) {
									slotFound = true;
									items.set(i, hand);
									break;
								}
							}
							if (!slotFound)
								player.dropSelectedItem(true);
						}
					}
					player.setStackInHand(Hand.MAIN_HAND, new ItemStack(Items.WATER_BUCKET));
				});
			}
		}
		return result;
	}
}
