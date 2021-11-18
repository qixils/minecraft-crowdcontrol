package dev.qixils.crowdcontrol.plugin.commands;

import dev.qixils.crowdcontrol.plugin.CrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.ImmediateCommand;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

@Getter
public class ToastCommand extends ImmediateCommand {
    private final String effectName = "toast";
    private final String displayName = "Render Toasts";
    public ToastCommand(CrowdControlPlugin plugin) {
        super(plugin);
    }

    @Override
    public Response.@NotNull Builder executeImmediately(@NotNull Request request) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            for (Player player : CrowdControlPlugin.getPlayers()) {
                Collection<NamespacedKey> recipes = player.getDiscoveredRecipes();
                player.undiscoverRecipes(recipes);
                player.discoverRecipes(recipes);
            }
        });
        return request.buildResponse().type(Response.ResultType.SUCCESS);
    }
}
