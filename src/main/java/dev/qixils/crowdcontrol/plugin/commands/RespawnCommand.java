package dev.qixils.crowdcontrol.plugin.commands;

import dev.qixils.crowdcontrol.plugin.CrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.ImmediateCommand;
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

@Getter
public class RespawnCommand extends ImmediateCommand {
	public RespawnCommand(CrowdControlPlugin plugin) {
		super(plugin);
	}

	private final String effectName = "respawn";
	private final String displayName = "Respawn Players";

	@Override
	public Response.@NotNull Builder executeImmediately(@NotNull List<@NotNull Player> players, @NotNull Request request) {
		Bukkit.getScheduler().runTask(plugin, () -> players
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
