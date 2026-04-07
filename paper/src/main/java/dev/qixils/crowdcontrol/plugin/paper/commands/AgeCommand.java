package dev.qixils.crowdcontrol.plugin.paper.commands;

import dev.qixils.crowdcontrol.plugin.paper.PaperCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.paper.RegionalCommandSync;
import live.crowdcontrol.cc4j.CCPlayer;
import live.crowdcontrol.cc4j.websocket.data.CCEffectResponse;
import live.crowdcontrol.cc4j.websocket.data.CCInstantEffectResponse;
import live.crowdcontrol.cc4j.websocket.data.ResponseStatus;
import live.crowdcontrol.cc4j.websocket.payload.PublicEffectPayload;
import lombok.Getter;
import org.bukkit.Tag;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static dev.qixils.crowdcontrol.common.command.CommandConstants.ENTITY_SEARCH_RADIUS;

@Getter
public class AgeCommand extends RegionalCommandSync {
	private final boolean baby;
	private final String effectName;

	public AgeCommand(PaperCrowdControlPlugin plugin, boolean baby) {
		super(plugin);
		this.baby = baby;
		this.effectName = "age_" + (baby ? "baby" : "adult");
	}

	@Override
	protected @NotNull CCEffectResponse buildFailure(@NotNull PublicEffectPayload request, @NotNull CCPlayer ccPlayer) {
		return new CCInstantEffectResponse(request.getRequestId(), ResponseStatus.FAIL_TEMPORARY, "No nearby entities");
	}

	@Override
	protected boolean executeRegionallySync(@NotNull Player player, @NotNull PublicEffectPayload request, @NotNull CCPlayer ccPlayer) {
		boolean success = false;
		for (Ageable entity : player.getLocation().getNearbyEntitiesByType(Ageable.class, ENTITY_SEARCH_RADIUS)) {
			if (Tag.ENTITY_TYPES_CANNOT_BE_AGE_LOCKED.isTagged(entity.getType())) continue;
			if (entity.getAgeLock() == baby) continue;

			entity.setAgeLock(baby);
			if (baby) entity.setBaby();
			else entity.setAdult();

			success = true;
		}
		return success;
	}
}
