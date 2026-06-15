package dev.qixils.crowdcontrol.plugin.paper.commands;

import dev.qixils.crowdcontrol.TriState;
import dev.qixils.crowdcontrol.exceptions.ExceptionUtil;
import dev.qixils.crowdcontrol.plugin.paper.FeatureElementCommand;
import live.crowdcontrol.cc4j.CCPlayer;
import live.crowdcontrol.cc4j.IUserRecord;
import live.crowdcontrol.cc4j.websocket.data.CCEffectResponse;
import live.crowdcontrol.cc4j.websocket.data.CCInstantEffectResponse;
import live.crowdcontrol.cc4j.websocket.data.ResponseStatus;
import live.crowdcontrol.cc4j.websocket.payload.PublicEffectPayload;
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
		return world.isEnabled(getEntityType());
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
	default TriState isSelectable(@NotNull IUserRecord user, @NotNull List<Player> potentialPlayers) {
		// TODO: dragon check?
		if (!isMonster())
			return TriState.UNKNOWN;
		if (!serverIsPeaceful())
			return TriState.TRUE;
		return TriState.FALSE;
	}

	default @Nullable CCEffectResponse tryExecute(@NotNull List<@NotNull Player> players, @NotNull PublicEffectPayload request, @NotNull CCPlayer ccPlayer) {
		// Block execution if impossible for all players
		CCEffectResponse error = null;
		for (Player player : players) {
			World world = player.getWorld();
			if (isMonster() && levelIsPeaceful(world))
				error = new CCInstantEffectResponse(request.getRequestId(), ResponseStatus.FAIL_TEMPORARY, "Hostile mobs cannot be spawned while on Peaceful difficulty");
			else if (getEntityType() == EntityType.ENDER_DRAGON && world.getEnvironment() == World.Environment.THE_END)
				error = new CCInstantEffectResponse(request.getRequestId(), ResponseStatus.FAIL_TEMPORARY, "Ender Dragons are very sensitive cannot be spawned in or removed from The End, sorry!");
			else if (!world.isEnabled(getEntityType()))
				error = new CCInstantEffectResponse(request.getRequestId(), ResponseStatus.FAIL_TEMPORARY, "Mob is not available in this version of Minecraft");
			else
				return null;
		}
		return error;
	}
}
