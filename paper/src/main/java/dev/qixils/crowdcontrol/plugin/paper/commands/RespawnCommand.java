package dev.qixils.crowdcontrol.plugin.paper.commands;

import dev.qixils.crowdcontrol.plugin.paper.PaperCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.paper.RegionalCommand;
import live.crowdcontrol.cc4j.CCPlayer;
import live.crowdcontrol.cc4j.websocket.data.CCEffectResponse;
import live.crowdcontrol.cc4j.websocket.data.CCInstantEffectResponse;
import live.crowdcontrol.cc4j.websocket.data.ResponseStatus;
import live.crowdcontrol.cc4j.websocket.payload.PublicEffectPayload;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

@Getter
public class RespawnCommand extends RegionalCommand {
	private final String effectName = "respawn";

	public RespawnCommand(PaperCrowdControlPlugin plugin) {
		super(plugin);
	}

	@NotNull
	private World getDefaultWorld() {
		World world = Bukkit.getWorld("world");
		if (world == null) {
			for (World iworld : Bukkit.getWorlds()) {
				if (iworld.getEnvironment() == Environment.NORMAL) {
					world = iworld;
					break;
				}
			}
		}
		if (world == null) {
			throw new IllegalStateException("Couldn't find an overworld world");
		}
		return world;
	}

	@Override
	protected @Nullable CCEffectResponse precheck(@NotNull List<@NotNull Player> players, @NotNull PublicEffectPayload request, @NotNull CCPlayer ccPlayer) {
		if (isActive(ccPlayer, "walk", "look"))
			return new CCInstantEffectResponse(request.getRequestId(), ResponseStatus.FAIL_TEMPORARY, "Cannot fling while frozen");
		return null;
	}

	@Override
	protected @NotNull CCEffectResponse buildFailure(@NotNull PublicEffectPayload request, @NotNull CCPlayer ccPlayer) {
		return new CCInstantEffectResponse(request.getRequestId(), ResponseStatus.FAIL_TEMPORARY, "Unable to teleport players");
	}

	@Override
	protected CompletableFuture<Boolean> executeRegionallyAsync(@NotNull Player player, @NotNull PublicEffectPayload request, @NotNull CCPlayer ccPlayer) {
		Location destination = Objects.requireNonNullElseGet(
			player.getRespawnLocation(),
			() -> getDefaultWorld().getSpawnLocation()
		);
		return player.teleportAsync(destination);
	}
}
