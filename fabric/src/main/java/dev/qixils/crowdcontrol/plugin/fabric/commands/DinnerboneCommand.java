package dev.qixils.crowdcontrol.plugin.fabric.commands;

import dev.qixils.crowdcontrol.plugin.fabric.Command;
import dev.qixils.crowdcontrol.plugin.fabric.FabricCrowdControlPlugin;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response.Builder;
import dev.qixils.crowdcontrol.socket.Response.ResultType;
import lombok.Getter;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.StreamSupport;

import static dev.qixils.crowdcontrol.common.CommandConstants.DINNERBONE_NAME;
import static dev.qixils.crowdcontrol.common.CommandConstants.DINNERBONE_RADIUS;
import static dev.qixils.crowdcontrol.plugin.fabric.FabricCrowdControlPlugin.ORIGINAL_DISPLAY_NAME;
import static dev.qixils.crowdcontrol.plugin.fabric.FabricCrowdControlPlugin.VIEWER_SPAWNED;

@Getter
public class DinnerboneCommand extends Command {
	private final String effectName = "dinnerbone";
	private final String displayName = "Flip Mobs Upside-Down";

	public DinnerboneCommand(FabricCrowdControlPlugin plugin) {
		super(plugin);
	}

	@Override
	public @NotNull CompletableFuture<Builder> execute(@NotNull List<@NotNull ServerPlayer> players, @NotNull Request request) {
		CompletableFuture<Boolean> successFuture = new CompletableFuture<>();
		sync(() -> {
			Set<Entity> entities = new HashSet<>();
			for (ServerPlayer player : players) {
				entities.addAll(StreamSupport.stream(player.getLevel().getAllEntities().spliterator(), false)
						.filter(entity -> entity.getType() != EntityType.PLAYER
								&& entity.position().distanceToSqr(player.position()) <= (DINNERBONE_RADIUS * DINNERBONE_RADIUS))
						.toList());
			}
			successFuture.complete(!entities.isEmpty());
			entities.forEach(entity -> {
				Component oldName = entity.getEntityData().get(ORIGINAL_DISPLAY_NAME).orElse(TextComponent.EMPTY);
				Component currentName = entity.getCustomName();
				if (currentName == null)
					currentName = TextComponent.EMPTY;
				if (currentName.equals(new TextComponent(DINNERBONE_NAME))) {
					entity.setCustomName(oldName);
					entity.getEntityData().set(ORIGINAL_DISPLAY_NAME, Optional.empty());
					Boolean viewerSpawned = entity.getEntityData().get(VIEWER_SPAWNED);
					if (viewerSpawned != null && viewerSpawned)
						entity.setCustomNameVisible(true);
				} else {
					entity.getEntityData().set(ORIGINAL_DISPLAY_NAME, Optional.of(currentName));
					entity.setCustomName(new TextComponent(DINNERBONE_NAME));
					entity.setCustomNameVisible(false);
				}
			});
		});
		return successFuture.thenApply(success -> success
				? request.buildResponse().type(ResultType.SUCCESS)
				: request.buildResponse().type(ResultType.RETRY).message("No nearby entities"));
	}
}
