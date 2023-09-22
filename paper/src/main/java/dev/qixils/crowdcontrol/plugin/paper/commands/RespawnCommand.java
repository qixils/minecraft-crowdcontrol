package dev.qixils.crowdcontrol.plugin.paper.commands;

import dev.qixils.crowdcontrol.plugin.paper.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.paper.PaperCrowdControlPlugin;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

import static dev.qixils.crowdcontrol.TimedEffect.isActive;

@Getter
public class RespawnCommand extends ImmediateCommand {
	private final String effectName = "respawn";

	public RespawnCommand(PaperCrowdControlPlugin plugin) {
		super(plugin);
	}

	@Override
	public Response.@NotNull Builder executeImmediately(@NotNull List<@NotNull Player> players, @NotNull Request request) {
		if (isActive("walk", request) || isActive("look", request))
			return request.buildResponse().type(Response.ResultType.RETRY).message("Cannot fling while frozen");
		sync(() -> players
				.forEach(player -> player.teleport(Objects.requireNonNullElseGet(player.getBedSpawnLocation(), () -> getDefaultWorld().getSpawnLocation()))));
		return request.buildResponse().type(Response.ResultType.SUCCESS);
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
}
