package dev.qixils.crowdcontrol.plugin.fabric.commands;

import dev.qixils.crowdcontrol.TriState;
import dev.qixils.crowdcontrol.common.command.Command;
import dev.qixils.crowdcontrol.plugin.fabric.FabricCrowdControlPlugin;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.StreamSupport;

public interface EntityCommand<E extends Entity> extends Command<ServerPlayer> {

	@Override
	@NotNull FabricCrowdControlPlugin getPlugin();

	@NotNull EntityType<E> getEntityType();

	default boolean isMonster() {
		return getEntityType().getCategory() == MobCategory.MONSTER;
	}

	default boolean levelIsPeaceful(@NotNull ServerLevel level) {
		return level.getDifficulty() == Difficulty.PEACEFUL;
	}

	default boolean serverIsPeaceful() {
		return StreamSupport.stream(getPlugin().server().getAllLevels().spliterator(), false).allMatch(this::levelIsPeaceful);
	}

	@Override
	default TriState isSelectable() {
		if (!isMonster())
			return TriState.UNKNOWN;
		if (!serverIsPeaceful())
			return TriState.TRUE;
		return TriState.FALSE;
	}

	default Response.@Nullable Builder tryExecute(@NotNull List<@NotNull ServerPlayer> players, @NotNull Request request) {
		for (ServerPlayer player : players) {
			if (isMonster() && levelIsPeaceful(player.getLevel())) {
				return request.buildResponse()
						.type(Response.ResultType.FAILURE)
						.message("Hostile mobs cannot be spawned while on Peaceful difficulty");
			}
		}
		return null;
	}
}
