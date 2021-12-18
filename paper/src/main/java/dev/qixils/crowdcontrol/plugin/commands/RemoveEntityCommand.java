package dev.qixils.crowdcontrol.plugin.commands;

import dev.qixils.crowdcontrol.plugin.BukkitCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.Command;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static dev.qixils.crowdcontrol.common.CommandConstants.REMOVE_ENTITY_RADIUS;

@Getter
public final class RemoveEntityCommand extends Command {
	private final EntityType entityType;
	private final String effectName;
	private final String displayName;

	public RemoveEntityCommand(BukkitCrowdControlPlugin plugin, EntityType entityType) {
		super(plugin);
		this.entityType = entityType;
		this.effectName = "remove_entity_" + entityType.name();
		this.displayName = "Remove " + plugin.getTextUtil().translate(entityType);
	}

	@Override
	public @NotNull CompletableFuture<Response.@NotNull Builder> execute(@NotNull List<@NotNull Player> players, @NotNull Request request) {
		CompletableFuture<Response.Builder> future = new CompletableFuture<>();
		Bukkit.getScheduler().runTask(plugin, () -> {
			Response.Builder result = request.buildResponse().type(Response.ResultType.FAILURE)
					.message("No " + plugin.getTextUtil().translate(entityType) + "s found nearby to remove");

			for (Player player : players) {
				for (Entity entity : player.getLocation().getNearbyEntitiesByType(entityType.getEntityClass(), REMOVE_ENTITY_RADIUS)) {
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
