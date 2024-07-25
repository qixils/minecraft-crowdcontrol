package dev.qixils.crowdcontrol.plugin.paper.commands;

import dev.qixils.crowdcontrol.plugin.paper.PaperCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.paper.RegionalCommand;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
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

import static dev.qixils.crowdcontrol.TimedEffect.isActive;

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
	protected Response.@Nullable Builder precheck(@NotNull List<@NotNull Player> players, @NotNull Request request) {
		if (isActive("walk", request) || isActive("look", request))
			return request.buildResponse().type(Response.ResultType.RETRY).message("Cannot fling while frozen");
		return null;
	}

	@Override
	protected Response.@NotNull Builder buildFailure(@NotNull Request request) {
		return request.buildResponse()
			.type(Response.ResultType.RETRY)
			.message("Unable to teleport players");
	}

	@Override
	protected CompletableFuture<Boolean> executeRegionallyAsync(@NotNull Player player, @NotNull Request request) {
		Location destination = Objects.requireNonNullElseGet(
			player.getRespawnLocation(),
			() -> getDefaultWorld().getSpawnLocation()
		);
		return player.teleportAsync(destination);
	}
}
