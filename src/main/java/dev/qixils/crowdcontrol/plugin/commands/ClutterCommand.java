package dev.qixils.crowdcontrol.plugin.commands;

import dev.qixils.crowdcontrol.plugin.CrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.ImmediateCommand;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

@Getter
public class ClutterCommand extends ImmediateCommand {
    public ClutterCommand(CrowdControlPlugin plugin) {
        super(plugin);
    }

    private final String effectName = "clutter";
    private final String displayName = "Clutter Inventories";

    @Override
    public Response.@NotNull Builder executeImmediately(@NotNull Request request) {
        // swaps two random items in player's inventory
        for (Player player : CrowdControlPlugin.getPlayers()) {
            PlayerInventory inventory = player.getInventory();
            int maxSlots = inventory.getSize();
            Set<Integer> slots = Set.of(inventory.getHeldItemSlot(), rand.nextInt(maxSlots), rand.nextInt(maxSlots));
            for (int slot : slots) {
                ItemStack hand = inventory.getItem(slot);
                // lazy workaround to get a unique slot lmfao
                int destSlot = slot;
                while (destSlot == slot) {
                    destSlot = rand.nextInt(maxSlots);
                }
                ItemStack swap = inventory.getItem(destSlot);
                inventory.setItem(slot, swap);
                inventory.setItem(destSlot, hand);
            }
            player.updateInventory();
        }
        return Response.builder().type(Response.ResultType.SUCCESS);
    }
}
