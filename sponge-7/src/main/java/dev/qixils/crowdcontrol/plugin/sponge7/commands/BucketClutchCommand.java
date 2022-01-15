package dev.qixils.crowdcontrol.plugin.sponge7.commands;

import dev.qixils.crowdcontrol.plugin.sponge7.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.sponge7.SpongeCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.sponge7.utils.BlockFinder;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.slot.EquipmentSlot;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.List;
import java.util.Optional;

@Getter
public class BucketClutchCommand extends ImmediateCommand {
	private static final int OFFSET = 30;
	private final String effectName = "bucket_clutch";
	private final String displayName = "Water Bucket Clutch";

	public BucketClutchCommand(SpongeCrowdControlPlugin plugin) {
		super(plugin);
	}

	@Override
	public Response.@NotNull Builder executeImmediately(@NotNull List<@NotNull Player> players, @NotNull Request request) {
		Response.Builder result = request.buildResponse()
				.type(Response.ResultType.RETRY)
				.message("No players are on the surface");
		for (Player player : players) {
			Location<World> curr = player.getLocation();
			boolean obstruction = false;
			for (int y = 1; y <= OFFSET; y++) {
				BlockState block = curr.add(0, y, 0).getBlock();
				if (BlockFinder.isPassable(block))
					continue;
				obstruction = true;
				break;
			}
			if (!obstruction) {
				result.type(Response.ResultType.SUCCESS).message("SUCCESS");
				sync(() -> {
					player.setLocation(curr.add(0, OFFSET, 0));
					ItemStack hand = player.getItemInHand(HandTypes.MAIN_HAND).orElse(null);
					if (hand != null && !hand.isEmpty() && !hand.getType().equals(ItemTypes.WATER_BUCKET)) {
						Optional<ItemStack> offhand = player.getItemInHand(HandTypes.OFF_HAND);
						if (!offhand.isPresent() || offhand.get().isEmpty()) {
							player.setItemInHand(HandTypes.OFF_HAND, hand);
						} else {
							boolean slotFound = false;
							for (Inventory slot : player.getInventory().slots()) {
								if (slot instanceof EquipmentSlot)
									continue;
								Optional<ItemStack> item = slot.peek();
								if (!item.isPresent() || item.get().isEmpty()) {
									slotFound = true;
									slot.set(hand);
									break;
								}
							}
							if (!slotFound)
								DropItemCommand.dropItem(plugin, player);
						}
					}
					player.setItemInHand(HandTypes.MAIN_HAND, ItemStack.of(ItemTypes.WATER_BUCKET));
				});
			}
		}
		return result;
	}
}
