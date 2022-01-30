package dev.qixils.crowdcontrol.plugin.paper.commands;

import dev.qixils.crowdcontrol.plugin.paper.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.paper.PaperCrowdControlPlugin;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Getter
public class BucketClutchCommand extends ImmediateCommand {
	private static final int OFFSET = 30;

	public BucketClutchCommand(PaperCrowdControlPlugin plugin) {
		super(plugin);
	}

	private final String effectName = "bucket_clutch";
	private final String displayName = "Water Bucket Clutch";

	@Override
	public Response.@NotNull Builder executeImmediately(@NotNull List<@NotNull Player> players, @NotNull Request request) {
		Response.Builder result = request.buildResponse()
				.type(Response.ResultType.RETRY)
				.message("No players are on the surface");
		for (Player player : players) {
			if (player.getWorld().getEnvironment().equals(Environment.NETHER))
				continue;
			Location curr = player.getLocation();
			boolean obstruction = false;
			for (int y = 1; y <= OFFSET; y++) {
				Block block = curr.clone().add(0, y, 0).getBlock();
				if (block.isPassable())
					continue;
				obstruction = true;
				break;
			}
			if (!obstruction) {
				result.type(Response.ResultType.SUCCESS).message("SUCCESS");
				sync(() -> player.teleportAsync(curr.clone().add(0, OFFSET, 0)).thenRun(() -> {
					PlayerInventory inv = player.getInventory();
					ItemStack hand = inv.getItemInMainHand();
					Material handType = hand.getType();
					if (!handType.isEmpty() && handType != Material.WATER_BUCKET && hand.getAmount() > 0) {
						ItemStack offhand = inv.getItemInOffHand();
						if (offhand.getAmount() == 0 || offhand.getType().isEmpty()) {
							inv.setItemInOffHand(hand);
						} else {
							boolean slotFound = false;
							for (int i = 0; i <= 36; i++) {
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
					inv.setItemInMainHand(new ItemStack(Material.WATER_BUCKET));
				}));
			}
		}
		return result;
	}
}
