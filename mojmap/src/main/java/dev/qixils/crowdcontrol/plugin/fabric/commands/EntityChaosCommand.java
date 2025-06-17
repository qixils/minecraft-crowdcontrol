package dev.qixils.crowdcontrol.plugin.fabric.commands;

import dev.qixils.crowdcontrol.common.util.ThreadUtil;
import dev.qixils.crowdcontrol.plugin.fabric.ModdedCommand;
import dev.qixils.crowdcontrol.plugin.fabric.ModdedCrowdControlPlugin;
import live.crowdcontrol.cc4j.CCPlayer;
import live.crowdcontrol.cc4j.websocket.data.CCInstantEffectResponse;
import live.crowdcontrol.cc4j.websocket.data.ResponseStatus;
import live.crowdcontrol.cc4j.websocket.payload.PublicEffectPayload;
import lombok.Getter;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import static dev.qixils.crowdcontrol.common.command.CommandConstants.CHAOS_LOCAL_RADIUS;
import static dev.qixils.crowdcontrol.common.command.CommandConstants.ENTITY_CHAOS_MIN;
import static java.util.Collections.emptySet;

@Getter
public class EntityChaosCommand extends ModdedCommand {
	private static final int R = CHAOS_LOCAL_RADIUS * CHAOS_LOCAL_RADIUS;
	private final String effectName = "entity_chaos";

	public EntityChaosCommand(ModdedCrowdControlPlugin plugin) {
		super(plugin);
	}

	@Override
	public void execute(@NotNull Supplier<@NotNull List<@NotNull ServerPlayer>> playerSupplier, @NotNull PublicEffectPayload request, @NotNull CCPlayer ccPlayer) {
		ccPlayer.sendResponse(ThreadUtil.waitForSuccess(request, () -> {
			List<ServerPlayer> players = playerSupplier.get();
			Set<Entity> entities = new HashSet<>(200);
			if (players.stream().anyMatch(plugin::globalEffectsUsableFor)) {
				for (ServerLevel level : plugin.theGame().getAllLevels()) {
					for (Entity entity : level.getAllEntities()) {
						if (entity.getType() == EntityType.PLAYER) continue;
						entities.add(entity);
					}
				}
			} else {
				for (ServerPlayer player : playerSupplier.get()) {
					Vec3 pp = player.position();
					for (Entity entity : ((ServerLevel) player.level()).getAllEntities()) {
						// TODO: I can probably reduce some loops here right?
						if (entity.getType() == EntityType.PLAYER) continue;
						Vec3 ep = entity.position();
						double x = pp.x - ep.x;
						double y = pp.y - ep.y;
						double z = pp.z - ep.z;
						if (x * x + y * y + z * z < R)
							entities.add(entity);
					}
				}
			}

			if (entities.size() < ENTITY_CHAOS_MIN)
				return new CCInstantEffectResponse(request.getRequestId(), ResponseStatus.FAIL_TEMPORARY, "Not enough entities found to teleport");

			int i = 0;
			boolean success = false;
			for (Entity entity : entities) {
				try {
					entity.ejectPassengers();
					ServerPlayer target = players.get((i++) % players.size());
					Vec3 dest = target.position();
					success |= entity.teleportTo(target.serverLevel(), dest.x, dest.y, dest.z, emptySet(), entity.getYRot(), entity.getXRot(), false); // boolean is unused?
				} catch (Exception ignored) {}
			}

			return success
				? new CCInstantEffectResponse(request.getRequestId(), ResponseStatus.SUCCESS)
				: new CCInstantEffectResponse(request.getRequestId(), ResponseStatus.FAIL_TEMPORARY, "Could not teleport entities");
		}, plugin.getSyncExecutor()));
	}
}
