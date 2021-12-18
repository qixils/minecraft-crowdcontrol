package dev.qixils.crowdcontrol.plugin.commands;

import dev.qixils.crowdcontrol.plugin.Command;
import dev.qixils.crowdcontrol.plugin.SpongeCrowdControlPlugin;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import dev.qixils.crowdcontrol.socket.Response.Builder;
import dev.qixils.crowdcontrol.socket.Response.ResultType;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.living.player.Player;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static dev.qixils.crowdcontrol.common.CommandConstants.REMOVE_ENTITY_RADIUS;

@Getter
public class RemoveEntityCommand extends Command {
	protected final EntityType entityType;
	private final String effectName;
	private final String displayName;
	// TODO: mob key

	public RemoveEntityCommand(SpongeCrowdControlPlugin plugin, EntityType entityType) {
		super(plugin);
		this.entityType = entityType;
		this.effectName = "remove_entity_" + entityType.getId(); // TODO: ensure this is right!
		this.displayName = "Remove " + entityType.getTranslation().get();
	}

	@Override
	public @NotNull CompletableFuture<Builder> execute(@NotNull List<@NotNull Player> players, @NotNull Request request) {
		CompletableFuture<Builder> future = new CompletableFuture<>();
		plugin.getSyncExecutor().execute(() -> {
			Builder result = request.buildResponse().type(ResultType.FAILURE)
					.message("No " + entityType.getTranslation().get() + "s found nearby to remove");

			for (Player player : players) {
				for (Entity entity : player.getWorld().getNearbyEntities(player.getPosition(), REMOVE_ENTITY_RADIUS)) {
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
