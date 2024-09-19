package dev.qixils.crowdcontrol.plugin.sponge8.commands;

import dev.qixils.crowdcontrol.common.LimitConfig;
import dev.qixils.crowdcontrol.plugin.sponge8.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.sponge8.SpongeCrowdControlPlugin;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import dev.qixils.crowdcontrol.socket.Response.Builder;
import dev.qixils.crowdcontrol.socket.Response.ResultType;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.world.WorldTypes;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.math.vector.Vector3d;

import java.util.ArrayList;
import java.util.List;

import static dev.qixils.crowdcontrol.common.command.CommandConstants.REMOVE_ENTITY_RADIUS;
import static dev.qixils.crowdcontrol.common.command.CommandConstants.csIdOf;

@Getter
public class RemoveEntityCommand<E extends Entity> extends ImmediateCommand implements EntityCommand<E> {
	protected final EntityType<E> entityType;
	private final String effectName;
	private final Component displayName;

	public RemoveEntityCommand(SpongeCrowdControlPlugin plugin, EntityType<E> entityType) {
		super(plugin);
		this.entityType = entityType;
		this.effectName = "remove_entity_" + csIdOf(entityType.key(RegistryTypes.ENTITY_TYPE));
		this.displayName = Component.translatable("cc.effect.remove_entity.name", entityType);
	}

	@Override
	public boolean isMonster() {
		if (EntityTypes.ENDER_DRAGON.get().equals(entityType))
			return false; // ender dragon is persistent regardless of difficulty so allow it to be removed
		return EntityCommand.super.isMonster();
	}

	private boolean removeEntityFrom(ServerPlayer player) {
		ServerWorld world = player.world();
		if (entityType == EntityTypes.ENDER_DRAGON && world.worldType() == WorldTypes.THE_END.get()) return false;

		Vector3d playerPosition = player.position();
		List<Entity> entities = new ArrayList<>(world.nearbyEntities(player.position(), REMOVE_ENTITY_RADIUS));
		entities.removeIf(entity -> !entity.type().equals(entityType));

		if (entities.isEmpty())
			return false;

		if (entities.size() > 1) {
			entities.sort((o1, o2) ->
					(int) (o1.position().distanceSquared(playerPosition)
							- o2.position().distanceSquared(playerPosition)));
		}

		entities.get(0).remove();
		return true;
	}

	@NotNull
	@Override
	public Builder executeImmediately(@NotNull List<@NotNull ServerPlayer> players, @NotNull Request request) {
		Response.Builder tryExecute = tryExecute(players, request);
		if (tryExecute != null) return tryExecute;

		Builder result = request.buildResponse().type(ResultType.RETRY)
				.message("No " + plugin.getTextUtil().asPlain(entityType) + "s found nearby to remove");

		LimitConfig config = getPlugin().getLimitConfig();
		int maxVictims = config.getItemLimit(entityType.key(RegistryTypes.ENTITY_TYPE).value());
		int victims = 0;

		// first pass (hosts)
		for (ServerPlayer player : players) {
			if (!config.hostsBypass() && maxVictims > 0 && victims >= maxVictims)
				break;
			if (!isHost(player))
				continue;
			if (removeEntityFrom(player))
				victims++;
		}

		// second pass (guests)
		for (ServerPlayer player : players) {
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
