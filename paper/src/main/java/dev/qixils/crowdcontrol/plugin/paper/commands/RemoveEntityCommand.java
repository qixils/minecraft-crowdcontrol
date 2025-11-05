package dev.qixils.crowdcontrol.plugin.paper.commands;

import dev.qixils.crowdcontrol.plugin.paper.PaperCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.paper.RegionalCommandSync;
import live.crowdcontrol.cc4j.CCPlayer;
import live.crowdcontrol.cc4j.websocket.data.CCEffectResponse;
import live.crowdcontrol.cc4j.websocket.data.CCInstantEffectResponse;
import live.crowdcontrol.cc4j.websocket.data.ResponseStatus;
import live.crowdcontrol.cc4j.websocket.payload.PublicEffectPayload;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

import static dev.qixils.crowdcontrol.common.command.CommandConstants.REMOVE_ENTITY_RADIUS;
import static dev.qixils.crowdcontrol.common.command.CommandConstants.csIdOf;

@Getter
public final class RemoveEntityCommand extends RegionalCommandSync implements EntityCommand {
	private final EntityType entityType;
	private final String effectName;
	private final Component displayName;
	private final String image = "remove_entity_creeper";
	private final int price = 250;
	private final byte priority = -5;
	private final List<String> categories = Collections.singletonList("Remove Entity");

	public RemoveEntityCommand(PaperCrowdControlPlugin plugin, EntityType entityType) {
		super(plugin);
		this.entityType = entityType;
		this.effectName = "remove_entity_" + csIdOf(entityType);
		this.displayName = Component.translatable("cc.effect.remove_entity.name", Component.translatable(entityType));
	}

	@Override
	public boolean isMonster() {
		if (entityType == EntityType.ENDER_DRAGON)
			return false; // ender dragon is persistent regardless of difficulty so allow it to be removed
		return EntityCommand.super.isMonster();
	}

	@Override
	protected @Nullable CCEffectResponse precheck(@NotNull List<@NotNull Player> players, @NotNull PublicEffectPayload request, @NotNull CCPlayer ccPlayer) {
		return tryExecute(players, request, ccPlayer);
	}

	@Override
	protected @NotNull CCEffectResponse buildFailure(@NotNull PublicEffectPayload request, @NotNull CCPlayer ccPlayer) {
		return new CCInstantEffectResponse(request.getRequestId(), ResponseStatus.FAIL_TEMPORARY, "No " + plugin.getTextUtil().translate(entityType) + "s found nearby to remove");
	}

	@Override
	protected int getPlayerLimit() {
		return plugin.getLimitConfig().getEntityLimit(entityType.getKey().getKey());
	}

	@Override
	protected boolean executeRegionallySync(@NotNull Player player, @NotNull PublicEffectPayload request, @NotNull CCPlayer ccPlayer) {
		if (entityType == EntityType.ENDER_DRAGON && player.getWorld().getEnvironment() == World.Environment.THE_END) return false;
		for (Entity entity : player.getLocation().getNearbyEntitiesByType(entityType.getEntityClass(), REMOVE_ENTITY_RADIUS)) {
			entity.remove();
			return true;
		}
		return false;
	}
}
