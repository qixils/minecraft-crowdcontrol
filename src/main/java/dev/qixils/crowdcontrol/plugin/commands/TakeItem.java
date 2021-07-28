package dev.qixils.crowdcontrol.plugin.commands;

import dev.qixils.crowdcontrol.plugin.ChatCommand;
import dev.qixils.crowdcontrol.plugin.CrowdControlPlugin;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class TakeItem extends ChatCommand {
    public TakeItem(CrowdControlPlugin plugin) {
        super(plugin);
    }

    @Override
    public int getCooldownSeconds() {
        return 60*5;
    }

    @Override
    public @NotNull String getCommand() {
        return "take";
    }

    @Override
    public boolean execute(String authorName, List<Player> players, String... args) {
        if (args.length == 0) {
            return false;
        }
        Material item = GiveItem.getItemByEvent(args);
        if (item == null) {
            return false;
        }
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : players) {
                    for (ItemStack itemStack : player.getInventory()) {
                        if (itemStack == null) {
                            continue;
                        }
                        if (itemStack.getType() == item) {
                            itemStack.setAmount(itemStack.getAmount()-1);
                            break;
                        }
                    }
                    player.updateInventory();
                }
            }
        }.runTask(plugin);
        return true;
    }
}
