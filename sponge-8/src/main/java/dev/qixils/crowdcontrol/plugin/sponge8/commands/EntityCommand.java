package dev.qixils.crowdcontrol.plugin.sponge8.commands;

import dev.qixils.crowdcontrol.TriState;
import dev.qixils.crowdcontrol.common.command.Command;
import dev.qixils.crowdcontrol.plugin.sponge8.SpongeCrowdControlPlugin;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityCategories;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.world.difficulty.Difficulties;
import org.spongepowered.api.world.server.ServerWorld;

import java.util.List;

public interface EntityCommand<E extends Entity> extends Command<ServerPlayer> {
	// TODO: API 10 feature set

	@Override
	@NotNull SpongeCrowdControlPlugin getPlugin();

	@NotNull EntityType<E> getEntityType();

	default boolean isMonster() {
		return EntityCategories.MONSTER.get().equals(getEntityType().category());
	}

	default boolean levelIsPeaceful(@NotNull ServerWorld level) {
		return Difficulties.PEACEFUL.get().equals(level.difficulty());
	}

	default boolean serverIsPeaceful() {
		return getPlugin().getGame().server().worldManager().worlds().stream().allMatch(this::levelIsPeaceful);
	}

	@Override
	default TriState isSelectable() {
		if (!isMonster()) {
			return TriState.UNKNOWN;
		}
		if (!serverIsPeaceful()) {
			return TriState.TRUE;
		}
		return TriState.FALSE;
	}

	default Response.@Nullable Builder tryExecute(@NotNull List<@NotNull ServerPlayer> players, @NotNull Request request) {
		if (isMonster()) {
			for (ServerPlayer player : players) {
				if (levelIsPeaceful(player.world())) {
					return request.buildResponse()
						.type(Response.ResultType.FAILURE)
						.message("Hostile mobs cannot be spawned while on Peaceful difficulty");
				}
			}
		}
		return null;
	}
}
