package io.github.lexikiq.crowdcontrol.commands;

import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import io.github.lexikiq.crowdcontrol.ChatCommand;
import io.github.lexikiq.crowdcontrol.CrowdControl;
import io.github.lexikiq.crowdcontrol.utils.BlockUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class LootboxCommand extends ChatCommand {
    protected static List<Material> ITEMS = List.copyOf(BlockUtil.MATERIAL_SET);

    public LootboxCommand(CrowdControl plugin) {
        super(plugin);
    }

    @Override
    public int getCooldownSeconds() {
        return 60;
    }

    @Override
    public @NotNull String getCommand() {
        return "lootbox";
    }

    @Override
    public boolean execute(ChannelMessageEvent event, List<Player> players, String... args) {
        for (Player player : players) {
            Inventory lootbox = Bukkit.createInventory(null, 27, event.getUser().getName()+" has gifted you...");
            Collections.shuffle(ITEMS, rand);
            Material item = null;
            for (Material i : ITEMS) {
                if (i.isItem()) {
                    item = i;
                    break;
                }
            }
            assert item != null;
            ItemStack itemStack = new ItemStack(item, 1+rand.nextInt(item.getMaxStackSize()));
            lootbox.setItem(13, itemStack);
            new BukkitRunnable(){
                @Override
                public void run() {
                    player.openInventory(lootbox);
                }
            }.runTask(plugin);
        }
        return true;
    }
}
