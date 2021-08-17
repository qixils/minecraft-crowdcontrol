package dev.qixils.crowdcontrol.plugin.commands;

import dev.qixils.crowdcontrol.plugin.ChatCommand;
import dev.qixils.crowdcontrol.plugin.CrowdControlPlugin;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;

import java.util.Collection;

@Getter
public class ToastCommand extends ChatCommand {
    private final String effectName = "toast";
    private final String displayName = "Render Toasts";
    public ToastCommand(CrowdControlPlugin plugin) {
        super(plugin);
    }

    @Override
    public Response.Result execute(Request request) {
        for (Player player : CrowdControlPlugin.getPlayers()) {
            Collection<NamespacedKey> recipes = player.getDiscoveredRecipes();
            player.undiscoverRecipes(recipes);
            player.discoverRecipes(recipes);
        }
        return Response.Result.SUCCESS;
    }
}
