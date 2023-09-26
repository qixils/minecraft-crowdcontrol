package dev.qixils.crowdcontrol.plugin.paper.commands;

import dev.qixils.crowdcontrol.TriState;
import dev.qixils.crowdcontrol.exceptions.ExceptionUtil;
import dev.qixils.crowdcontrol.plugin.paper.FeatureElementCommand;
import dev.qixils.crowdcontrol.plugin.paper.utils.ReflectionUtil;
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
import java.util.Optional;

import static dev.qixils.crowdcontrol.plugin.paper.utils.ReflectionUtil.cbClass;

public interface EntityCommand extends FeatureElementCommand {
	@NotNull EntityType getEntityType();

	default @NotNull Class<? extends Entity> getEntityClass() {
		return ExceptionUtil.validateNotNull(getEntityType().getEntityClass(), "entityClass");
	}

	@Override
	default @NotNull Optional<Object> requiredFeatures() {
		//return CraftMagicNumbers.getEntityTypes(getEntityType()).requiredFeatures();
		return ReflectionUtil.getClazz(cbClass("util.CraftMagicNumbers")).flatMap(clazz -> ReflectionUtil.invokeMethod(
				(Object) null,
				clazz,
				"getEntityTypes",
				new Class<?>[]{EntityType.class},
				getEntityType()
		));
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
		if (!isMonster())
			return TriState.UNKNOWN;
		if (!serverIsPeaceful())
			return TriState.TRUE;
		return TriState.FALSE;
	}

	default Response.@Nullable Builder tryExecute(@NotNull List<@NotNull Player> players, @NotNull Request request) {
		for (Player player : players) {
			if (isMonster() && levelIsPeaceful(player.getWorld())) {
				return request.buildResponse()
						.type(Response.ResultType.FAILURE)
						.message("Hostile mobs cannot be spawned while on Peaceful difficulty");
			}
//			if (!isEnabled(((CraftWorld) player.getWorld()).getHandle().enabledFeatures())) {
//				return request.buildResponse()
//						.type(Response.ResultType.UNAVAILABLE)
//						.message("Mob is not available in this version of Minecraft");
//			}
		}
		return null;
	}
}
