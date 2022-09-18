package dev.qixils.crowdcontrol.plugin.paper.commands;

import dev.qixils.crowdcontrol.common.LimitConfig;
import dev.qixils.crowdcontrol.plugin.paper.Command;
import dev.qixils.crowdcontrol.plugin.paper.PaperCrowdControlPlugin;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static dev.qixils.crowdcontrol.common.command.CommandConstants.REMOVE_ENTITY_RADIUS;

@Getter
public final class RemoveEntityCommand extends Command {
	private final EntityType entityType;
	private final String effectName;
	private final Component displayName;

	public RemoveEntityCommand(PaperCrowdControlPlugin plugin, EntityType entityType) {
		super(plugin);
		this.entityType = entityType;
		this.effectName = "remove_entity_" + entityType.name();
		this.displayName = Component.translatable("cc.effect.remove_entity.name", Component.translatable(entityType));
	}

	private boolean removeEntityFrom(Player player) {
		for (Entity entity : player.getLocation().getNearbyEntitiesByType(entityType.getEntityClass(), REMOVE_ENTITY_RADIUS)) {
			entity.remove();
			return true;
		}
		return false;
	}

	@Override
	public @NotNull CompletableFuture<Response.@NotNull Builder> execute(@NotNull List<@NotNull Player> players, @NotNull Request request) {
		CompletableFuture<Response.Builder> future = new CompletableFuture<>();
		sync(() -> {
			Response.Builder result = request.buildResponse().type(Response.ResultType.FAILURE)
					.message("No " + plugin.getTextUtil().translate(entityType) + "s found nearby to remove");

			LimitConfig config = plugin.getLimitConfig();
			int maxVictims = config.getEntityLimit(entityType.getKey().getKey());
			int victims = 0;

			// first pass (hosts)
			for (Player player : players) {
				if (!config.hostsBypass() && maxVictims > 0 && victims >= maxVictims)
					break;
				if (!isHost(player))
					continue;
				if (removeEntityFrom(player))
					victims++;
			}

			// second pass (guests)
			for (Player player : players) {
				if (maxVictims > 0 && victims >= maxVictims)
					break;
				if (isHost(player))
					continue;
				if (removeEntityFrom(player))
					victims++;
			}

			if (victims > 0)
				result.type(Response.ResultType.SUCCESS).message("SUCCESS");

			future.complete(result);
		});
		return future;
	}
}
