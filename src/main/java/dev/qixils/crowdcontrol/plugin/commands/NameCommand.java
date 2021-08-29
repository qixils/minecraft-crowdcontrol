package dev.qixils.crowdcontrol.plugin.commands;

import dev.qixils.crowdcontrol.plugin.Command;
import dev.qixils.crowdcontrol.plugin.CrowdControlPlugin;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

@Getter
public class NameCommand extends Command {
    public NameCommand(CrowdControlPlugin plugin) {
        super(plugin);
    }

    private final String effectName = "name_item";
    private final String displayName = "Name Item";

    @Override
    public Response.@NotNull Result execute(@NotNull Request request) {
        String text = request.getViewer();
        CrowdControlPlugin.getPlayers().forEach(player -> {
            ItemStack item = player.getInventory().getItemInMainHand();
            ItemMeta meta = item.getItemMeta();
            meta.displayName(Component.text(text));
            item.setItemMeta(meta);
        });
        return Response.Result.SUCCESS;
    }
}
