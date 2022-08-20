package dev.qixils.crowdcontrol.plugin.paper.commands;

import dev.qixils.crowdcontrol.plugin.paper.Command;
import dev.qixils.crowdcontrol.plugin.paper.PaperCrowdControlPlugin;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response.Builder;
import dev.qixils.crowdcontrol.socket.Response.ResultType;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static dev.qixils.crowdcontrol.common.command.CommandConstants.DINNERBONE_COMPONENT;
import static dev.qixils.crowdcontrol.common.command.CommandConstants.DINNERBONE_RADIUS;
import static dev.qixils.crowdcontrol.plugin.paper.PaperCrowdControlPlugin.COMPONENT_TYPE;

@Getter
public class DinnerboneCommand extends Command {
	private final NamespacedKey key;
	private final String effectName = "dinnerbone";
	private final String displayName = "Flip Mobs Upside-Down";

	public DinnerboneCommand(PaperCrowdControlPlugin plugin) {
		super(plugin);
		this.key = new NamespacedKey(plugin, "original_name");
	}

	@Override
	public @NotNull CompletableFuture<Builder> execute(@NotNull List<@NotNull Player> players, @NotNull Request request) {
		Set<LivingEntity> entities = new HashSet<>();
		CompletableFuture<Boolean> successFuture = new CompletableFuture<>();
		sync(() -> {
			for (Player player : players) {
				entities.addAll(player.getLocation().getNearbyLivingEntities(DINNERBONE_RADIUS,
						x -> x.getType() != EntityType.PLAYER
				));
			}
			successFuture.complete(!entities.isEmpty());
			for (Entity entity : entities) {
				PersistentDataContainer data = entity.getPersistentDataContainer();
				Component currentName = entity.customName();
				if (DINNERBONE_COMPONENT.equals(currentName)) {
					entity.customName(data.get(key, COMPONENT_TYPE));
					entity.setCustomNameVisible(true);
					data.remove(key);
				} else {
					if (currentName != null)
						data.set(key, COMPONENT_TYPE, currentName);
					entity.customName(DINNERBONE_COMPONENT);
					entity.setCustomNameVisible(false);
				}
			}
		});
		return successFuture.thenApply(success -> success
				? request.buildResponse().type(ResultType.SUCCESS)
				: request.buildResponse().type(ResultType.RETRY).message("No nearby entities"));
	}
}
