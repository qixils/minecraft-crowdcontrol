package dev.qixils.crowdcontrol.plugin.fabric.commands;

import dev.qixils.crowdcontrol.plugin.fabric.Command;
import dev.qixils.crowdcontrol.plugin.fabric.FabricCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.fabric.interfaces.LivingEntityData;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response.Builder;
import dev.qixils.crowdcontrol.socket.Response.ResultType;
import lombok.Getter;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.StreamSupport;

import static dev.qixils.crowdcontrol.common.command.CommandConstants.DINNERBONE_NAME;
import static dev.qixils.crowdcontrol.common.command.CommandConstants.DINNERBONE_RADIUS;

@Getter
public class DinnerboneCommand extends Command {
	private static final Component DINNERBONE_COMPONENT = Component.literal(DINNERBONE_NAME);
	private final String effectName = "dinnerbone";

	public DinnerboneCommand(FabricCrowdControlPlugin plugin) {
		super(plugin);
	}

	@Override
	public @NotNull CompletableFuture<Builder> execute(@NotNull List<@NotNull ServerPlayer> players, @NotNull Request request) {
		CompletableFuture<Boolean> successFuture = new CompletableFuture<>();
		sync(() -> {
			Set<LivingEntity> entities = new HashSet<>();
			for (ServerPlayer player : players) {
				entities.addAll(StreamSupport.stream(player.getLevel().getAllEntities().spliterator(), false)
						.filter(entity -> entity instanceof LivingEntity
								&& entity.getType() != EntityType.PLAYER
								&& entity.position().distanceToSqr(player.position()) <= (DINNERBONE_RADIUS * DINNERBONE_RADIUS))
						.map(entity -> (LivingEntity) entity)
						.toList());
			}
			successFuture.complete(!entities.isEmpty());
			entities.forEach(entity -> {
				LivingEntityData data = (LivingEntityData) entity;
				final @Nullable Component oldName = data.originalDisplayName().orElse(null);
				final @Nullable Component currentName = entity.getCustomName();
				if (Objects.equals(currentName, DINNERBONE_COMPONENT)) {
					entity.setCustomName(oldName);
					data.originalDisplayName(Optional.empty());
					if (data.viewerSpawned())
						entity.setCustomNameVisible(true);
				} else {
					data.originalDisplayName(Optional.ofNullable(currentName));
					entity.setCustomName(DINNERBONE_COMPONENT.copy());
					entity.setCustomNameVisible(false);
				}
			});
		});
		return successFuture.thenApply(success -> success
				? request.buildResponse().type(ResultType.SUCCESS)
				: request.buildResponse().type(ResultType.RETRY).message("No nearby entities"));
	}
}
