package dev.qixils.crowdcontrol.plugin.paper.commands;

import dev.qixils.crowdcontrol.TriState;
import dev.qixils.crowdcontrol.exceptions.ExceptionUtil;
import dev.qixils.crowdcontrol.plugin.paper.FeatureElementCommand;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.World;
import org.bukkit.entity.Enemy;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface EntityCommand extends FeatureElementCommand {
	@NotNull EntityType getEntityType();

	default @NotNull Class<? extends Entity> getEntityClass() {
		return ExceptionUtil.validateNotNull(getEntityType().getEntityClass(), "entityClass");
	}

	@Override
	default boolean isFeatureEnabled(@NotNull World world) {
		return getEntityType().isEnabledByFeature(world);
	}

	default boolean isMonster() {
		return Enemy.class.isAssignableFrom(getEntityClass());
	}

	default boolean levelIsPeaceful(@NotNull World level) {
		return level.getDifficulty() == Difficulty.PEACEFUL;
	}

	default boolean serverIsPeaceful() {
		return Bukkit.getWorlds().stream().allMatch(this::levelIsPeaceful);
	}

	@Override
	default TriState isSelectable() {
		// TODO: dragon check?
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
			else if (getEntityType() == EntityType.ENDER_DRAGON && world.getEnvironment() == World.Environment.THE_END) {
				error = request.buildResponse()
					.type(Response.ResultType.FAILURE)
					.message("Ender Dragons are very sensitive cannot be spawned in or removed from The End, sorry!");
			}
			else if (!getEntityType().isEnabledByFeature(world)) {
				error = request.buildResponse()
						.type(Response.ResultType.UNAVAILABLE)
						.message("Mob is not available in this version of Minecraft");
			}
			else {
				return null;
			}
		}
		return error;
	}
}
