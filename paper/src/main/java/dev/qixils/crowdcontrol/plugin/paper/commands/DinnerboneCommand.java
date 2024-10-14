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
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;

import static dev.qixils.crowdcontrol.common.command.CommandConstants.DINNERBONE_COMPONENT;
import static dev.qixils.crowdcontrol.common.command.CommandConstants.ENTITY_SEARCH_RADIUS;
import static dev.qixils.crowdcontrol.plugin.paper.PaperCrowdControlPlugin.COMPONENT_TYPE;

@Getter
public class DinnerboneCommand extends RegionalCommandSync {
	private final NamespacedKey key;
	private final String effectName = "dinnerbone";

	public DinnerboneCommand(PaperCrowdControlPlugin plugin) {
		super(plugin);
		this.key = new NamespacedKey(plugin.getPaperPlugin(), "original_name");
	}

	@Override
	protected @NotNull CCEffectResponse buildFailure(@NotNull PublicEffectPayload request, @NotNull CCPlayer ccPlayer) {
		return new CCInstantEffectResponse(request.getRequestId(), ResponseStatus.FAIL_TEMPORARY, "No nearby entities");
	}

	@Override
	protected boolean executeRegionallySync(@NotNull Player player, @NotNull PublicEffectPayload request, @NotNull CCPlayer ccPlayer) {
		boolean success = false;
		for (Entity entity : player.getLocation().getNearbyLivingEntities(ENTITY_SEARCH_RADIUS)) {
			PersistentDataContainer data = entity.getPersistentDataContainer();
			Component currentName = entity.customName();
			if (DINNERBONE_COMPONENT.equals(currentName)) {
				Component savedName = data.get(key, COMPONENT_TYPE);
				entity.customName(savedName);
				if (savedName != null)
					entity.setCustomNameVisible(true);
				data.remove(key);
			} else {
				if (currentName != null)
					data.set(key, COMPONENT_TYPE, currentName);
				entity.customName(DINNERBONE_COMPONENT);
				entity.setCustomNameVisible(false);
			}
			success = true;
		}
		return success;
	}
}
