package dev.qixils.crowdcontrol.plugin.sponge7.commands;

import dev.qixils.crowdcontrol.TriState;
import dev.qixils.crowdcontrol.common.command.Command;
import dev.qixils.crowdcontrol.plugin.sponge7.SpongeCrowdControlPlugin;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.living.Hostile;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.world.DimensionTypes;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.difficulty.Difficulties;

import java.util.List;

public interface EntityCommand extends Command<Player> {
	@Override
	@NotNull SpongeCrowdControlPlugin getPlugin();

	@NotNull EntityType getEntityType();

	default boolean isMonster() {
		return Hostile.class.isAssignableFrom(getEntityType().getEntityClass());
	}

	default boolean levelIsPeaceful(@NotNull World level) {
		return level.getDifficulty() == Difficulties.PEACEFUL;
	}

	default boolean serverIsPeaceful() {
		return getPlugin().getGame().getServer().getWorlds().stream().allMatch(this::levelIsPeaceful);
	}

	@Override
	default TriState isSelectable() {
		if (!isMonster())
			return TriState.UNKNOWN;
		if (!serverIsPeaceful())
			return TriState.TRUE;
		return TriState.FALSE;
	}

	default Response.@Nullable Builder tryExecute(@NotNull List<@NotNull Player> players, @NotNull Request request) {
		Response.Builder error = null;
		for (Player player : players) {
			World world = player.getWorld();
			if (isMonster() && levelIsPeaceful(world)) {
				error = request.buildResponse()
						.type(Response.ResultType.FAILURE)
						.message("Hostile mobs cannot be spawned while on Peaceful difficulty");
			}
			else if (getEntityType() == EntityTypes.ENDER_DRAGON && world.getDimension().getType() == DimensionTypes.THE_END) {
				error = request.buildResponse()
					.type(Response.ResultType.FAILURE)
					.message("Ender Dragons are very sensitive cannot be spawned in or removed from The End, sorry!");
			}
			else {
				return null;
			}
		}
		return error;
	}
}
