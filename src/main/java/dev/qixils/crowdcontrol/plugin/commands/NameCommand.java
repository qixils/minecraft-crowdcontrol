package dev.qixils.crowdcontrol.plugin.commands;

import dev.qixils.crowdcontrol.plugin.ChatCommand;
import dev.qixils.crowdcontrol.plugin.CrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.utils.RandomUtil;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class NameCommand extends ChatCommand {
    public NameCommand(CrowdControlPlugin plugin) {
        super(plugin);
    }

    @Override
    public int getCooldownSeconds() {
        return 10;
    }

    @Override
    public @NotNull String getCommand() {
        return "name";
    }

    @Override
    public boolean execute(String authorName, List<Player> players, String... args) {
        String text = String.join(" ", args);
        Player player = (Player) RandomUtil.randomElementFrom(players);
        ItemStack item = player.getInventory().getItemInMainHand();
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(text);
        new BukkitRunnable(){
            @Override
            public void run() {
                item.setItemMeta(meta);
                player.updateInventory();
            }
        }.runTask(plugin);
        return true;
    }
}
