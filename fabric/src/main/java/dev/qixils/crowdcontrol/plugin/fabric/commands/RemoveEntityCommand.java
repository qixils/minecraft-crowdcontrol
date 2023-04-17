package dev.qixils.crowdcontrol.plugin.fabric.commands;

import dev.qixils.crowdcontrol.common.LimitConfig;
import dev.qixils.crowdcontrol.plugin.fabric.FabricCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.fabric.ImmediateCommand;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import dev.qixils.crowdcontrol.socket.Response.Builder;
import dev.qixils.crowdcontrol.socket.Response.ResultType;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.minecraft.entity.Entity;
import net.minecraft.entity.Entity.RemovalReason;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.StreamSupport;

import static dev.qixils.crowdcontrol.common.command.CommandConstants.REMOVE_ENTITY_RADIUS;

@Getter
public class RemoveEntityCommand<E extends Entity> extends ImmediateCommand implements EntityCommand<E> {
	protected final EntityType<E> entityType;
	private final String effectName;
	private final Component displayName;

	public RemoveEntityCommand(FabricCrowdControlPlugin plugin, EntityType<E> entityType) {
		super(plugin);
		this.entityType = entityType;
		this.effectName = "remove_entity_" + csIdOf(Registries.ENTITY_TYPE.getId(entityType));
		this.displayName = Component.translatable("cc.effect.remove_entity.name", entityType.getName());
	}

	@Override
	public boolean isMonster() {
		if (entityType == EntityType.ENDER_DRAGON)
			return false; // ender dragon is persistent regardless of difficulty so allow it to be removed
		return EntityCommand.super.isMonster();
	}

	private boolean removeEntityFrom(ServerPlayerEntity player) {
		Vec3d playerPosition = player.getPos();
		List<Entity> entities = StreamSupport.stream(player.getWorld().iterateEntities().spliterator(), false)
				.filter(entity -> entity.getType() == entityType && entity.squaredDistanceTo(playerPosition) <= REMOVE_ENTITY_RADIUS * REMOVE_ENTITY_RADIUS)
				.sorted((entity1, entity2) -> (int) (entity1.squaredDistanceTo(playerPosition) - entity2.squaredDistanceTo(playerPosition))).toList();
		if (entities.isEmpty())
			return false;
		entities.get(0).remove(RemovalReason.KILLED);
		return true;
	}

	@NotNull
	@Override
	public Response.Builder executeImmediately(@NotNull List<@NotNull ServerPlayerEntity> players, @NotNull Request request) {
		Response.Builder tryExecute = tryExecute(players, request);
		if (tryExecute != null) return tryExecute;

		Builder result = request.buildResponse().type(ResultType.RETRY)
				.message("No " + plugin.getTextUtil().asPlain(entityType.getName()) + "s found nearby to remove");

		LimitConfig config = getPlugin().getLimitConfig();
		int maxVictims = config.getItemLimit(Registries.ENTITY_TYPE.getId(entityType).getPath());
		int victims = 0;

		// first pass (hosts)
		for (ServerPlayerEntity player : players) {
			if (!config.hostsBypass() && maxVictims > 0 && victims >= maxVictims)
				break;
			if (!isHost(player))
				continue;
			if (removeEntityFrom(player))
				victims++;
		}

		// second pass (guests)
		for (ServerPlayerEntity player : players) {
			if (maxVictims > 0 && victims >= maxVictims)
				break;
			if (isHost(player))
				continue;
			if (removeEntityFrom(player))
				victims++;
		}

		if (victims > 0)
			result.type(ResultType.SUCCESS).message("SUCCESS");

		return result;
	}
}
