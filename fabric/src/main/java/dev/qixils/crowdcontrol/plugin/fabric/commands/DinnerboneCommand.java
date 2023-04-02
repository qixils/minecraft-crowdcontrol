package dev.qixils.crowdcontrol.plugin.fabric.commands;

import dev.qixils.crowdcontrol.plugin.fabric.Command;
import dev.qixils.crowdcontrol.plugin.fabric.FabricCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.fabric.interfaces.Components;
import dev.qixils.crowdcontrol.plugin.fabric.interfaces.OriginalDisplayName;
import dev.qixils.crowdcontrol.plugin.fabric.interfaces.ViewerMob;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response.Builder;
import dev.qixils.crowdcontrol.socket.Response.ResultType;
import lombok.Getter;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.StreamSupport;

import static dev.qixils.crowdcontrol.common.command.CommandConstants.DINNERBONE_NAME;
import static dev.qixils.crowdcontrol.common.command.CommandConstants.DINNERBONE_RADIUS;

@Getter
public class DinnerboneCommand extends Command {
	private static final Text DINNERBONE_COMPONENT = Text.literal(DINNERBONE_NAME);
	private final String effectName = "dinnerbone";

	public DinnerboneCommand(FabricCrowdControlPlugin plugin) {
		super(plugin);
	}

	@Override
	public @NotNull CompletableFuture<Builder> execute(@NotNull List<@NotNull ServerPlayerEntity> players, @NotNull Request request) {
		CompletableFuture<Boolean> successFuture = new CompletableFuture<>();
		sync(() -> {
			Set<LivingEntity> entities = new HashSet<>();
			for (ServerPlayerEntity player : players) {
				entities.addAll(StreamSupport.stream(player.getWorld().iterateEntities().spliterator(), false)
						.filter(entity -> entity instanceof LivingEntity
								&& entity.getType() != EntityType.PLAYER
								&& entity.getPos().squaredDistanceTo(player.getPos()) <= (DINNERBONE_RADIUS * DINNERBONE_RADIUS))
						.map(entity -> (LivingEntity) entity)
						.toList());
			}
			successFuture.complete(!entities.isEmpty());
			entities.forEach(entity -> {
				OriginalDisplayName nameData = Components.ORIGINAL_DISPLAY_NAME.get(entity);
				ViewerMob viewerData = Components.VIEWER_MOB.get(entity);
				final @Nullable Text oldName = nameData.getValue();
				final @Nullable Text currentName = entity.getCustomName();
				if (Objects.equals(currentName, DINNERBONE_COMPONENT)) {
					entity.setCustomName(oldName);
					nameData.setValue(null);
					if (viewerData.isViewerSpawned())
						entity.setCustomNameVisible(true);
				} else {
					nameData.setValue(currentName);
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
