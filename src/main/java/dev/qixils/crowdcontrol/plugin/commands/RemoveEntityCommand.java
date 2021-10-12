package dev.qixils.crowdcontrol.plugin.commands;

import dev.qixils.crowdcontrol.plugin.Command;
import dev.qixils.crowdcontrol.plugin.CrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.utils.TextUtil;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

@Getter
public final class RemoveEntityCommand extends Command {
    private static final int SEARCH_RADIUS = 35;
    private final EntityType entityType;
    private final String effectName;
    private final String displayName;

    public RemoveEntityCommand(CrowdControlPlugin plugin, EntityType entityType) {
        super(plugin);
        this.entityType = entityType;
        this.effectName = "remove_entity_" + entityType.name();
        this.displayName = "Remove " + TextUtil.translate(entityType);
    }

    @Override
    public @NotNull CompletableFuture<Response.@NotNull Builder> execute(@NotNull Request request) {
        CompletableFuture<Response.Builder> future = new CompletableFuture<>();
        Bukkit.getScheduler().runTask(plugin, () -> {
            Response.Builder result = Response.builder().type(Response.ResultType.FAILURE)
                    .message("No " + TextUtil.translate(entityType) + "s found nearby to remove");

            for (Player player : CrowdControlPlugin.getPlayers()) {
                for (Entity entity : player.getLocation().getNearbyEntitiesByType(entityType.getEntityClass(), SEARCH_RADIUS)) {
                    result.type(Response.ResultType.SUCCESS).message("SUCCESS");
                    entity.remove();
                    break;
                }
            }
            future.complete(result);
        });
        return future;
    }
}
