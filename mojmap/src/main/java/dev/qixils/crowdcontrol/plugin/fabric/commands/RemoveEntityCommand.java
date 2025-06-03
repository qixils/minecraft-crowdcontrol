package dev.qixils.crowdcontrol.plugin.fabric.commands;

import dev.qixils.crowdcontrol.common.LimitConfig;
import dev.qixils.crowdcontrol.common.util.ThreadUtil;
import dev.qixils.crowdcontrol.plugin.fabric.ModdedCommand;
import dev.qixils.crowdcontrol.plugin.fabric.ModdedCrowdControlPlugin;
import live.crowdcontrol.cc4j.CCPlayer;
import live.crowdcontrol.cc4j.websocket.data.CCEffectResponse;
import live.crowdcontrol.cc4j.websocket.data.CCInstantEffectResponse;
import live.crowdcontrol.cc4j.websocket.data.ResponseStatus;
import live.crowdcontrol.cc4j.websocket.payload.PublicEffectPayload;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.StreamSupport;

import static dev.qixils.crowdcontrol.common.command.CommandConstants.REMOVE_ENTITY_RADIUS;
import static dev.qixils.crowdcontrol.common.command.CommandConstants.csIdOf;

@Getter
public class RemoveEntityCommand<E extends Entity> extends ModdedCommand implements EntityCommand<E> {
	protected final EntityType<? extends E> entityType;
	protected final List<EntityType<? extends E>> entityTypes;
	private final String effectName;
	private final Component displayName;

	public RemoveEntityCommand(ModdedCrowdControlPlugin plugin, EntityType<E> entityType) {
		this(
			plugin,
			"remove_entity_" + csIdOf(BuiltInRegistries.ENTITY_TYPE.getKey(entityType)),
			Component.translatable("cc.effect.remove_entity.name", plugin.toAdventure(entityType.getDescription())),
			entityType
		);
	}

	@SafeVarargs
	public RemoveEntityCommand(ModdedCrowdControlPlugin plugin, String effectName, @Nullable Component displayName, EntityType<? extends E> firstEntity, EntityType<? extends E>... otherEntities) {
		super(plugin);
		this.entityType = firstEntity;
		this.entityTypes = new ArrayList<>(1 + otherEntities.length);
		entityTypes.add(firstEntity);
		entityTypes.addAll(Arrays.asList(otherEntities));

		this.effectName = effectName;
		this.displayName = displayName;
	}

	public @NotNull Component getDisplayName() {
		if (displayName != null) return displayName;
		return getDefaultDisplayName();
	}

	@Override
	public boolean isMonster() {
		if (entityType == EntityType.ENDER_DRAGON)
			return false; // ender dragon is persistent regardless of difficulty so allow it to be removed
		return EntityCommand.super.isMonster();
	}

	private boolean removeEntityFrom(ServerPlayer player) {
		ServerLevel level = player.serverLevel();
		if (entityType == EntityType.ENDER_DRAGON && level.getDragonFight() != null) return false;

		Vec3 playerPosition = player.position();
		List<Entity> entities = StreamSupport.stream(level.getAllEntities().spliterator(), false)
				.filter(entity -> getEntityTypes().contains(entity.getType()) && entity.distanceToSqr(playerPosition) <= REMOVE_ENTITY_RADIUS * REMOVE_ENTITY_RADIUS)
				.sorted((entity1, entity2) -> (int) (entity1.distanceToSqr(playerPosition) - entity2.distanceToSqr(playerPosition))).toList();
		if (entities.isEmpty())
			return false;
		entities.getFirst().remove(RemovalReason.KILLED);
		return true;
	}

	@Override
	public void execute(@NotNull Supplier<@NotNull List<@NotNull ServerPlayer>> playerSupplier, @NotNull PublicEffectPayload request, @NotNull CCPlayer ccPlayer) {
		ccPlayer.sendResponse(ThreadUtil.waitForSuccess(() -> {
			List<ServerPlayer> players = playerSupplier.get();

			LimitConfig config = getPlugin().getLimitConfig();
			int playerLimit = config.getEntityLimit(BuiltInRegistries.ENTITY_TYPE.getKey(entityType).getPath());

			CCEffectResponse tryExecute = tryExecute(players, request, ccPlayer);
			if (tryExecute != null) return tryExecute;

			return executeLimit(players, playerLimit, player -> {
				boolean success = false;
				try {
					success = removeEntityFrom(player);
				} catch (Exception ignored) {
				}
				return success
					? new CCInstantEffectResponse(request.getRequestId(), ResponseStatus.SUCCESS)
					: new CCInstantEffectResponse(request.getRequestId(), ResponseStatus.FAIL_TEMPORARY, "No " + plugin.getTextUtil().asPlain(entityType.getDescription()) + "s found nearby to remove");
			});
		}));
	}
}
