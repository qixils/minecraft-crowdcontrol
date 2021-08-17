package dev.qixils.crowdcontrol.plugin.commands;

import dev.qixils.crowdcontrol.plugin.ChatCommand;
import dev.qixils.crowdcontrol.plugin.CrowdControlPlugin;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

@Getter
public class NameCommand extends ChatCommand {
    public NameCommand(CrowdControlPlugin plugin) {
        super(plugin);
    }

    private final String effectName = "name-item";
    private final String displayName = "Name Item";

    @Override
    public Response.Result execute(Request request) {
        String text = request.getViewer();
        CrowdControlPlugin.getPlayers().forEach(player -> {
            ItemStack item = player.getInventory().getItemInMainHand();
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(text);
            item.setItemMeta(meta);
        });
        return Response.Result.SUCCESS;
    }
}
