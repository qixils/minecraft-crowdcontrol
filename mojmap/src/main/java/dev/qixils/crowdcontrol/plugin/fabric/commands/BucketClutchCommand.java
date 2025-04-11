package dev.qixils.crowdcontrol.plugin.fabric.commands;

import dev.qixils.crowdcontrol.common.util.ThreadUtil;
import dev.qixils.crowdcontrol.plugin.fabric.ModdedCommand;
import dev.qixils.crowdcontrol.plugin.fabric.ModdedCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.fabric.utils.BlockFinder;
import dev.qixils.crowdcontrol.plugin.fabric.utils.Location;
import live.crowdcontrol.cc4j.CCPlayer;
import live.crowdcontrol.cc4j.websocket.data.CCInstantEffectResponse;
import live.crowdcontrol.cc4j.websocket.data.ResponseStatus;
import live.crowdcontrol.cc4j.websocket.payload.PublicEffectPayload;
import lombok.Getter;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Supplier;

import static dev.qixils.crowdcontrol.common.command.CommandConstants.BUCKET_CLUTCH_MAX;
import static dev.qixils.crowdcontrol.common.command.CommandConstants.BUCKET_CLUTCH_MIN;

@Getter
public class BucketClutchCommand extends ModdedCommand {
	public BucketClutchCommand(ModdedCrowdControlPlugin plugin) {
		super(plugin);
	}

	private final String effectName = "bucket_clutch";

	@Override
	public void execute(@NotNull Supplier<@NotNull List<@NotNull ServerPlayer>> playerSupplier, @NotNull PublicEffectPayload request, @NotNull CCPlayer ccPlayer) {
		ccPlayer.sendResponse(ThreadUtil.waitForSuccess(() -> {
			boolean success = false;
			for (ServerPlayer player : playerSupplier.get()) {
				Item material = player.serverLevel().dimensionType().ultraWarm() ? Items.COBWEB : Items.WATER_BUCKET;
				ItemStack giveItem = new ItemStack(material);
				player.registryAccess().lookup(Registries.ENCHANTMENT)
					.flatMap(registry -> registry.get(Enchantments.VANISHING_CURSE))
					.ifPresent(enchantment -> giveItem.enchant(enchantment, 1));

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

				success = true;

				sync(() -> {
					player.teleportTo(dest.x(), dest.y(), dest.z());
					ItemStack hand = player.getMainHandItem();
					if (!hand.isEmpty() && hand.getItem() != material) {
						ItemStack offhand = player.getOffhandItem();
						if (offhand.isEmpty()) {
							player.setItemInHand(InteractionHand.OFF_HAND, hand);
						} else {
							boolean slotFound = false;
							Inventory inv = player.getInventory();
							for (int i = 0; i < 36; i++) {
								ItemStack item = inv.getItem(i);
								if (item.isEmpty()) {
									slotFound = true;
									inv.setItem(i, hand);
									break;
								}
							}
							if (!slotFound)
								player.drop(true);
						}
					}
					player.setItemInHand(InteractionHand.MAIN_HAND, giveItem.copy());
				});
			}
			return success
				? new CCInstantEffectResponse(request.getRequestId(), ResponseStatus.SUCCESS)
				: new CCInstantEffectResponse(request.getRequestId(), ResponseStatus.FAIL_TEMPORARY, "No players are on the surface");
		}));
	}
}
