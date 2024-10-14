package dev.qixils.crowdcontrol.plugin.paper.commands;

import dev.qixils.crowdcontrol.plugin.paper.PaperCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.paper.RegionalCommandSync;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static dev.qixils.crowdcontrol.common.command.CommandConstants.REMOVE_ENTITY_RADIUS;
import static dev.qixils.crowdcontrol.common.command.CommandConstants.csIdOf;

@Getter
public final class RemoveEntityCommand extends RegionalCommandSync implements EntityCommand {
	private final EntityType entityType;
	private final String effectName;
	private final Component displayName;

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
	protected Response.@Nullable Builder precheck(@NotNull List<@NotNull Player> players, @NotNull Request request) {
		return tryExecute(players, request);
	}

	@Override
	protected Response.@NotNull Builder buildFailure(@NotNull Request request) {
		return request.buildResponse()
			.type(Response.ResultType.RETRY)
			.message("No " + plugin.getTextUtil().translate(entityType) + "s found nearby to remove");
	}

	@Override
	protected int getPlayerLimit() {
		return plugin.getLimitConfig().getEntityLimit(entityType.getKey().getKey());
	}

	@Override
	protected boolean executeRegionallySync(@NotNull Player player, @NotNull Request request) {
		for (Entity entity : player.getLocation().getNearbyEntitiesByType(entityType.getEntityClass(), REMOVE_ENTITY_RADIUS)) {
			entity.remove();
			return true;
		}
		return false;
	}
}
