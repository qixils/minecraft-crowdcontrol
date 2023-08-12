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
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.slot.EquipmentSlot;
import org.spongepowered.api.world.DimensionTypes;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.List;
import java.util.Optional;

import static dev.qixils.crowdcontrol.common.command.CommandConstants.BUCKET_CLUTCH_MAX;
import static dev.qixils.crowdcontrol.common.command.CommandConstants.BUCKET_CLUTCH_MIN;

@Getter
public class BucketClutchCommand extends ImmediateCommand {
	public BucketClutchCommand(SpongeCrowdControlPlugin plugin) {
		super(plugin);
	}

	private final String effectName = "bucket_clutch";

	@Override
	public Response.@NotNull Builder executeImmediately(@NotNull List<@NotNull Player> players, @NotNull Request request) {
		Response.Builder result = request.buildResponse()
				.type(Response.ResultType.RETRY)
				.message("No players are on the surface");
		for (Player player : players) {
			ItemType material = player.getWorld().getDimension().getType().equals(DimensionTypes.NETHER) ? ItemTypes.WEB : ItemTypes.WATER_BUCKET;

			Location<World> curr = player.getLocation();
			int offset = BUCKET_CLUTCH_MAX - 1;
			for (int y = 1; y <= BUCKET_CLUTCH_MAX; y++) {
				BlockState block = curr.add(0, y, 0).getBlock();
				if (BlockFinder.isPassable(block))
					continue;
				offset = y - 2;
				break;
			}
			if (offset < BUCKET_CLUTCH_MIN)
				continue;
			Location<World> dest = curr.add(0, offset, 0);

            result.type(Response.ResultType.SUCCESS).message("SUCCESS");

            sync(() -> {
                player.setLocation(dest);
                ItemStack hand = player.getItemInHand(HandTypes.MAIN_HAND).orElse(null);
                if (hand != null && !hand.isEmpty() && !hand.getType().equals(material)) {
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
                player.setItemInHand(HandTypes.MAIN_HAND, ItemStack.of(material));
            });
        }
		return result;
	}
}
