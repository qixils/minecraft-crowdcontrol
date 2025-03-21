package dev.qixils.crowdcontrol.plugin.paper.commands;

import dev.qixils.crowdcontrol.plugin.paper.PaperCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.paper.RegionalCommand;
import live.crowdcontrol.cc4j.CCPlayer;
import live.crowdcontrol.cc4j.websocket.data.CCEffectResponse;
import live.crowdcontrol.cc4j.websocket.data.CCInstantEffectResponse;
import live.crowdcontrol.cc4j.websocket.data.ResponseStatus;
import live.crowdcontrol.cc4j.websocket.payload.PublicEffectPayload;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

import static dev.qixils.crowdcontrol.common.command.CommandConstants.BUCKET_CLUTCH_MAX;
import static dev.qixils.crowdcontrol.common.command.CommandConstants.BUCKET_CLUTCH_MIN;

@Getter
public class BucketClutchCommand extends RegionalCommand {
	public BucketClutchCommand(PaperCrowdControlPlugin plugin) {
		super(plugin);
	}

	private final String effectName = "bucket_clutch";

	@Override
	protected @NotNull CCEffectResponse buildFailure(@NotNull PublicEffectPayload request, @NotNull CCPlayer ccPlayer) {
		return new CCInstantEffectResponse(request.getRequestId(), ResponseStatus.FAIL_TEMPORARY, "No players are on the surface");
	}

	@Override
	protected CompletableFuture<Boolean> executeRegionallyAsync(@NotNull Player player, @NotNull PublicEffectPayload request, @NotNull CCPlayer ccPlayer) {
		Material material = player.getWorld().isUltraWarm() ? Material.COBWEB : Material.WATER_BUCKET;
		ItemStack giveItem = new ItemStack(material);
		giveItem.addEnchantment(Enchantment.VANISHING_CURSE, 1);

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
			return CompletableFuture.completedFuture(false);

		Location dest = curr.clone().add(0, offset, 0);
		return player.teleportAsync(dest).thenApply(success -> {
			if (!success)
				return false;

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
			inv.setItemInMainHand(giveItem);
			return true;
		});
	}
}
