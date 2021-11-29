package dev.qixils.crowdcontrol.plugin.commands;

import dev.qixils.crowdcontrol.plugin.CrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.ImmediateCommand;
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
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Getter
public class GiveItemCommand extends ImmediateCommand {
    private final Material item;
    private final String effectName;
    private final String displayName;
    public GiveItemCommand(CrowdControlPlugin plugin, Material item) {
        super(plugin);
        this.item = item;
        this.effectName = "give_" + item.name();
        this.displayName = "Give " + TextUtil.translate(item);
    }

    @Override
    public Response.@NotNull Builder executeImmediately(@NotNull List<@NotNull Player> players, @NotNull Request request) {
        ItemStack itemStack = new ItemStack(item);
        for (Player player : players) {
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
        return request.buildResponse().type(Response.ResultType.SUCCESS);
    }
}
