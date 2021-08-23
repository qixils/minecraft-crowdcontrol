package dev.qixils.crowdcontrol.plugin.commands;

import dev.qixils.crowdcontrol.plugin.Command;
import dev.qixils.crowdcontrol.plugin.CrowdControlPlugin;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;

@Getter
public class BucketClutchCommand extends Command {
	private static final int OFFSET = 30;
	public BucketClutchCommand(CrowdControlPlugin plugin) {
		super(plugin);
	}

	private final String effectName = "bucket-clutch";
	private final String displayName = "Water Bucket Clutch";

	@Override
	public Response.@NotNull Result execute(@NotNull Request request) {
		Response.Result result = new Response.Result(Response.ResultType.FAILURE, "No players are on the surface");
		for (Player player : CrowdControlPlugin.getPlayers()) {
			Location curr = player.getLocation();
			boolean obstruction = false;
			for (int y = 1; y <= OFFSET; y++) {
				Block block = curr.clone().add(0, y, 0).getBlock();
				if (block.isEmpty() || block.getType() == Material.WATER)
					continue;
				obstruction = true;
				break;
			}
			if (!obstruction) {
				result = Response.Result.SUCCESS;
				Bukkit.getScheduler().runTask(plugin, () -> player.teleportAsync(curr.clone().add(0, OFFSET, 0)).thenRun(() -> {
					PlayerInventory inv = player.getInventory();
					ItemStack hand = inv.getItemInMainHand();
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
					inv.setItemInMainHand(new ItemStack(Material.WATER_BUCKET));
				}));
			}
		}
		return result;
	}
}
