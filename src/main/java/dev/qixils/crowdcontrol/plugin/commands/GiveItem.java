package dev.qixils.crowdcontrol.plugin.commands;

import dev.qixils.crowdcontrol.plugin.ChatCommand;
import dev.qixils.crowdcontrol.plugin.CrowdControlPlugin;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;

public class GiveItem extends ChatCommand {
    public GiveItem(CrowdControlPlugin plugin) {
        super(plugin);
    }

    @Override
    public int getCooldownSeconds() {
        return (int) (60*2.5);
    }

    @Override
    public @NotNull String getCommand() {
        return "give";
    }

    @Override
    public boolean execute(String authorName, List<Player> players, String... args) {
        if (args.length == 0) {
            return false;
        }
        Material material = getItemByEvent(args);
        if (material == null) {
            return false;
        }
        ItemStack itemStack = new ItemStack(material);
//        itemStack.setAmount(itemStack.getMaxStackSize());
        for (Player player : players) {
            Location location = player.getLocation();
            new BukkitRunnable(){
                @Override
                public void run() {
                    Item item = (Item) player.getWorld().spawnEntity(location, EntityType.DROPPED_ITEM);
                    item.setItemStack(itemStack);
                    item.setOwner(player.getUniqueId());
                    item.setThrower(player.getUniqueId());
                    item.setCanMobPickup(false);
                    item.setCanPlayerPickup(true);
                    item.setPickupDelay(0);
                }
            }.runTask(plugin);
        }
        return true;
    }

    public static @Nullable Material getItemByEvent(String[] args) {
        String itemText = String.join("_", args).toUpperCase(Locale.ENGLISH);
        if (itemText.isEmpty()) {
            return null;
        }
        Material material = Material.getMaterial(itemText);
        if (material == null || !material.isItem()) {
            return null;
        }
        return material;
    }
}
