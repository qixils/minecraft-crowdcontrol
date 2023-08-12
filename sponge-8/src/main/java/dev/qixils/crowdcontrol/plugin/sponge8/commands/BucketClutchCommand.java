package dev.qixils.crowdcontrol.plugin.sponge8.commands;

import dev.qixils.crowdcontrol.plugin.sponge8.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.sponge8.SpongeCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.sponge8.utils.BlockFinder;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.api.item.inventory.slot.EquipmentSlot;
import org.spongepowered.api.world.WorldTypes;
import org.spongepowered.api.world.server.ServerLocation;

import java.util.List;

import static dev.qixils.crowdcontrol.common.command.CommandConstants.BUCKET_CLUTCH_MAX;
import static dev.qixils.crowdcontrol.common.command.CommandConstants.BUCKET_CLUTCH_MIN;

@Getter
public class BucketClutchCommand extends ImmediateCommand {
	public BucketClutchCommand(SpongeCrowdControlPlugin plugin) {
		super(plugin);
	}

	private final String effectName = "bucket_clutch";

	@Override
	public Response.@NotNull Builder executeImmediately(@NotNull List<@NotNull ServerPlayer> players, @NotNull Request request) {
		Response.Builder result = request.buildResponse()
				.type(Response.ResultType.RETRY)
				.message("No players are on the surface");
		for (ServerPlayer player : players) {
			ItemType material = (player.world().worldType().equals(WorldTypes.THE_NETHER.get()) ? ItemTypes.COBWEB : ItemTypes.WATER_BUCKET).get(); // TODO API?: ultrawarm

			ServerLocation curr = player.serverLocation();
			int offset = BUCKET_CLUTCH_MAX - 1;
			for (int y = 1; y <= BUCKET_CLUTCH_MAX; y++) {
				BlockState block = curr.add(0, y, 0).block();
				if (BlockFinder.isPassable(block))
					continue;
				offset = y - 2;
				break;
			}
			if (offset < BUCKET_CLUTCH_MIN)
				continue;
			ServerLocation dest = curr.add(0, offset, 0);

            result.type(Response.ResultType.SUCCESS).message("SUCCESS");

            sync(() -> {
                player.setLocation(dest);
                ItemStack hand = player.itemInHand(HandTypes.MAIN_HAND);
                if (hand != null && !hand.isEmpty() && !hand.type().equals(material)) {
                    ItemStack offhand = player.itemInHand(HandTypes.OFF_HAND);
                    if (offhand.isEmpty()) {
                        player.setItemInHand(HandTypes.OFF_HAND, hand);
                    } else {
                        boolean slotFound = false;
                        for (Slot slot : player.inventory().slots()) {
                            if (slot instanceof EquipmentSlot)
                                continue;
                            ItemStack item = slot.peek();
                            if (item.isEmpty()) {
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
