package dev.qixils.crowdcontrol.plugin.fabric.commands;

import dev.qixils.crowdcontrol.TriState;
import dev.qixils.crowdcontrol.plugin.fabric.FeatureElementCommand;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.Difficulty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.StreamSupport;

public interface EntityCommand<E extends Entity> extends FeatureElementCommand {
	@NotNull EntityType<E> getEntityType();

	@Override
	default @NotNull FeatureSet getRequiredFeatures() {
		return getEntityType().getRequiredFeatures();
	}

	default boolean isMonster() {
		return getEntityType().getSpawnGroup() == SpawnGroup.MONSTER;
	}

	default boolean levelIsPeaceful(@NotNull ServerWorld level) {
		return level.getDifficulty() == Difficulty.PEACEFUL;
	}

	default boolean serverIsPeaceful() {
		return StreamSupport.stream(getPlugin().server().getWorlds().spliterator(), false).allMatch(this::levelIsPeaceful);
	}

	@Override
	default TriState isSelectable() {
		if (!isMonster())
			return TriState.UNKNOWN;
		if (!serverIsPeaceful())
			return TriState.TRUE;
		return TriState.FALSE;
	}

	default Response.@Nullable Builder tryExecute(@NotNull List<@NotNull ServerPlayerEntity> players, @NotNull Request request) {
		for (ServerPlayerEntity player : players) {
			if (isMonster() && levelIsPeaceful(player.getWorld())) {
				return request.buildResponse()
						.type(Response.ResultType.FAILURE)
						.message("Hostile mobs cannot be spawned while on Peaceful difficulty");
			}
			if (!isEnabled(player.getWorld().getEnabledFeatures())) {
				return request.buildResponse()
						.type(Response.ResultType.UNAVAILABLE)
						.message("Mob is not available in this version of Minecraft");
			}
		}
		return null;
	}
}
