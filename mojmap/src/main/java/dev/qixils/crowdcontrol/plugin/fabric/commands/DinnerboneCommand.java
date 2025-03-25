package dev.qixils.crowdcontrol.plugin.fabric.commands;

import dev.qixils.crowdcontrol.common.util.ThreadUtil;
import dev.qixils.crowdcontrol.plugin.fabric.ModdedCommand;
import dev.qixils.crowdcontrol.plugin.fabric.ModdedCrowdControlPlugin;
import live.crowdcontrol.cc4j.CCPlayer;
import live.crowdcontrol.cc4j.websocket.data.CCInstantEffectResponse;
import live.crowdcontrol.cc4j.websocket.data.ResponseStatus;
import live.crowdcontrol.cc4j.websocket.payload.PublicEffectPayload;
import lombok.Getter;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.StreamSupport;

import static dev.qixils.crowdcontrol.common.command.CommandConstants.DINNERBONE_NAME;
import static dev.qixils.crowdcontrol.common.command.CommandConstants.ENTITY_SEARCH_RADIUS;

@Getter
public class DinnerboneCommand extends ModdedCommand {
	private static final Component DINNERBONE_COMPONENT = Component.literal(DINNERBONE_NAME);
	private final String effectName = "dinnerbone";

	public DinnerboneCommand(ModdedCrowdControlPlugin plugin) {
		super(plugin);
	}

	@Override
	public void execute(@NotNull Supplier<@NotNull List<@NotNull ServerPlayer>> playerSupplier, @NotNull PublicEffectPayload request, @NotNull CCPlayer ccPlayer) {
		ccPlayer.sendResponse(ThreadUtil.waitForSuccess(() -> {
			Set<LivingEntity> entities = new HashSet<>();
			for (ServerPlayer player : playerSupplier.get()) {
				entities.addAll(StreamSupport.stream(player.serverLevel().getAllEntities().spliterator(), false)
					.filter(entity -> entity instanceof LivingEntity
						&& entity.getType() != EntityType.PLAYER
						&& entity.position().distanceToSqr(player.position()) <= (ENTITY_SEARCH_RADIUS * ENTITY_SEARCH_RADIUS))
					.map(entity -> (LivingEntity) entity)
					.toList());
			}
			if (entities.isEmpty())
				return new CCInstantEffectResponse(request.getRequestId(), ResponseStatus.FAIL_TEMPORARY, "No nearby entities");
			for (LivingEntity entity : entities) {
				final @Nullable Component oldName = entity.cc$getOriginalDisplayName();
				final @Nullable Component currentName = entity.getCustomName();
				if (Objects.equals(currentName, DINNERBONE_COMPONENT)) {
					entity.setCustomName(oldName);
					entity.cc$setOriginalDisplayName(null);
					if (entity.cc$isViewerSpawned())
						entity.setCustomNameVisible(true);
				} else {
					entity.cc$setOriginalDisplayName(currentName);
					entity.setCustomName(DINNERBONE_COMPONENT.copy());
					entity.setCustomNameVisible(false);
				}
			}
			return new CCInstantEffectResponse(request.getRequestId(), ResponseStatus.SUCCESS);
		}, plugin.getSyncExecutor()));
	}
}
