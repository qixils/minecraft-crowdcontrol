package io.github.lexikiq.crowdcontrol.commands;

import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import io.github.lexikiq.crowdcontrol.ChatCommand;
import io.github.lexikiq.crowdcontrol.CrowdControl;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ClutterCommand extends ChatCommand {
    public ClutterCommand(CrowdControl plugin) {
        super(plugin);
    }

    @Override
    public int getCooldownSeconds() {
        return 60;
    }

    @Override
    public @NotNull String getCommand() {
        return "clutter";
    }

    @Override
    public boolean execute(ChannelMessageEvent event, List<Player> players, String... args) {
        // swaps two random items in player's inventory
        for (Player player : players) {
            PlayerInventory inventory = player.getInventory();
            ItemStack hand = inventory.getItemInMainHand();
            // lazy workaround to get a unique slot lmfao
            int destSlot = inventory.getHeldItemSlot();
            while (destSlot == inventory.getHeldItemSlot()) {
                destSlot = rand.nextInt(9*4 + 5);
            }
            ItemStack swap = inventory.getItem(destSlot);
            inventory.setItem(inventory.getHeldItemSlot(), swap);
            inventory.setItem(destSlot, hand);
            player.updateInventory();
        }
        return true;
    }
}
