package dev.qixils.crowdcontrol.plugin.fabric.commands;

import dev.qixils.crowdcontrol.plugin.fabric.FabricCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.fabric.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.fabric.utils.BlockFinder;
import dev.qixils.crowdcontrol.plugin.fabric.utils.Location;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static dev.qixils.crowdcontrol.common.command.CommandConstants.BUCKET_CLUTCH_MAX;
import static dev.qixils.crowdcontrol.common.command.CommandConstants.BUCKET_CLUTCH_MIN;

@Getter
public class BucketClutchCommand extends ImmediateCommand {
	public BucketClutchCommand(FabricCrowdControlPlugin plugin) {
		super(plugin);
	}

	private final String effectName = "bucket_clutch";

	@Override
	public Response.@NotNull Builder executeImmediately(@NotNull List<@NotNull ServerPlayer> players, @NotNull Request request) {
		Response.Builder result = request.buildResponse()
				.type(Response.ResultType.RETRY)
				.message("No players are on the surface");
		for (ServerPlayer player : players) {
			Item material = player.getLevel().dimensionType().ultraWarm() ? Items.COBWEB : Items.WATER_BUCKET;

			Location curr = new Location(player);
			int offset = BUCKET_CLUTCH_MAX - 1;
			for (int y = 1; y <= BUCKET_CLUTCH_MAX; y++) {
				if (BlockFinder.isPassable(curr.add(0, y, 0)))
					continue;
				offset = y - 2;
				break;
			}
			if (offset < BUCKET_CLUTCH_MIN)
				continue;
			Location dest = curr.add(0, offset, 0);

            result.type(Response.ResultType.SUCCESS).message("SUCCESS");

            sync(() -> {
                player.teleportTo(dest.x(), dest.y(), dest.z());
                ItemStack hand = player.getMainHandItem();
                if (!hand.isEmpty() && hand.getItem() != material) {
                    ItemStack offhand = player.getOffhandItem();
                    if (offhand.isEmpty()) {
                        player.setItemInHand(InteractionHand.OFF_HAND, hand);
                    } else {
                        boolean slotFound = false;
                        for (int i = 0; i < 36; i++) {
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
                player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(material));
            });
        }
		return result;
	}
}
