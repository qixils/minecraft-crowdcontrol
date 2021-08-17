package dev.qixils.crowdcontrol.plugin.commands;

import dev.qixils.crowdcontrol.plugin.ChatCommand;
import dev.qixils.crowdcontrol.plugin.CrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.utils.TextUtil;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@Getter
public class GiveItemCommand extends ChatCommand {
    private final Material item;
    private final String effectName;
    private final String displayName;
    public GiveItemCommand(CrowdControlPlugin plugin, Material item) {
        super(plugin);
        this.item = item;
        this.effectName = "give-" + item.name();
        this.displayName = "Give " + TextUtil.translate(item);
    }

    @Override
    public Response.Result execute(Request request) {
        ItemStack itemStack = new ItemStack(item);
        itemStack.setAmount(itemStack.getMaxStackSize());
        for (Player player : CrowdControlPlugin.getPlayers()) {
            Location location = player.getLocation();
            Bukkit.getScheduler().runTask(plugin, () -> {
                Item item = (Item) player.getWorld().spawnEntity(location, EntityType.DROPPED_ITEM);
                item.setItemStack(itemStack);
                item.setOwner(player.getUniqueId());
                item.setThrower(player.getUniqueId());
                item.setCanMobPickup(false);
                item.setCanPlayerPickup(true);
                item.setPickupDelay(0);
            });
        }
        return Response.Result.SUCCESS;
    }
}
