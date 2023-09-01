package dev.qixils.crowdcontrol.plugin.paper.commands;

import dev.qixils.crowdcontrol.plugin.paper.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.paper.PaperCrowdControlPlugin;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static dev.qixils.crowdcontrol.common.command.CommandConstants.BUCKET_CLUTCH_MAX;
import static dev.qixils.crowdcontrol.common.command.CommandConstants.BUCKET_CLUTCH_MIN;

@Getter
public class BucketClutchCommand extends ImmediateCommand {
	public BucketClutchCommand(PaperCrowdControlPlugin plugin) {
		super(plugin);
	}

	private final String effectName = "bucket_clutch";

	@Override
	public Response.@NotNull Builder executeImmediately(@NotNull List<@NotNull Player> players, @NotNull Request request) {
		Response.Builder result = request.buildResponse()
				.type(Response.ResultType.RETRY)
				.message("No players are on the surface");
		for (Player player : players) {
			Material material = player.getWorld().isUltraWarm() ? Material.COBWEB : Material.WATER_BUCKET;

			Location curr = player.getLocation();
			int offset = BUCKET_CLUTCH_MAX - 1;
			for (int y = 1; y <= BUCKET_CLUTCH_MAX; y++) {
				Block block = curr.clone().add(0, y, 0).getBlock();
				if (block.isPassable())
					continue;
				offset = y - 2;
				break;
			}
			if (offset < BUCKET_CLUTCH_MIN)
                continue;
			Location dest = curr.clone().add(0, offset, 0);

            result.type(Response.ResultType.SUCCESS).message("SUCCESS");

            sync(() -> player.teleportAsync(dest).thenRun(() -> {
                PlayerInventory inv = player.getInventory();
                ItemStack hand = inv.getItemInMainHand();
                Material handType = hand.getType();
                if (!handType.isEmpty() && handType != material && hand.getAmount() > 0) {
                    ItemStack offhand = inv.getItemInOffHand();
                    if (offhand.getAmount() == 0 || offhand.getType().isEmpty()) {
                        inv.setItemInOffHand(hand);
                    } else {
                        boolean slotFound = false;
                        for (int i = 0; i < 36; i++) {
                            ItemStack item = inv.getItem(i);
                            if (item == null || item.getAmount() == 0 || item.getType().isEmpty()) {
                                slotFound = true;
                                inv.setItem(i, hand);
                                break;
                            }
                        }
                        if (!slotFound)
                            player.dropItem(true);
                    }
                }
                inv.setItemInMainHand(new ItemStack(material));
            }));
        }
		return result;
	}
}
