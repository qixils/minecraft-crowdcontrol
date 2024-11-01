package dev.qixils.crowdcontrol.plugin.fabric.commands;

import dev.qixils.crowdcontrol.TriState;
import dev.qixils.crowdcontrol.plugin.fabric.FeatureElementCommand;
import live.crowdcontrol.cc4j.CCPlayer;
import live.crowdcontrol.cc4j.IUserRecord;
import live.crowdcontrol.cc4j.websocket.data.CCEffectResponse;
import live.crowdcontrol.cc4j.websocket.data.CCInstantEffectResponse;
import live.crowdcontrol.cc4j.websocket.data.ResponseStatus;
import live.crowdcontrol.cc4j.websocket.payload.PublicEffectPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.flag.FeatureFlagSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.StreamSupport;

public interface EntityCommand<E extends Entity> extends FeatureElementCommand {
	@NotNull EntityType<E> getEntityType();

	@Override
	default @NotNull FeatureFlagSet requiredFeatures() {
		return getEntityType().requiredFeatures();
	}

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
	default TriState isSelectable(@NotNull IUserRecord user, @NotNull List<ServerPlayer> potentialPlayers) {
		if (!isMonster())
			return TriState.UNKNOWN;
		if (!serverIsPeaceful())
			return TriState.TRUE;
		return TriState.FALSE;
	}

	default @Nullable CCEffectResponse tryExecute(@NotNull List<@NotNull ServerPlayer> players, @NotNull PublicEffectPayload request, @NotNull CCPlayer ccPlayer) {
		for (ServerPlayer player : players) {
			if (isMonster() && levelIsPeaceful(player.serverLevel())) {
				return new CCInstantEffectResponse(request.getRequestId(), ResponseStatus.FAIL_TEMPORARY, "Hostile mobs cannot be spawned while on Peaceful difficulty");
			}
			if (!isEnabled(player.serverLevel().enabledFeatures())) {
				return new CCInstantEffectResponse(request.getRequestId(), ResponseStatus.FAIL_TEMPORARY, "Mob is not available in this version of Minecraft");
			}
		}
		return null;
	}
}
