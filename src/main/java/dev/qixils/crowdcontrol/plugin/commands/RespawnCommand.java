package dev.qixils.crowdcontrol.plugin.commands;

import dev.qixils.crowdcontrol.plugin.Command;
import dev.qixils.crowdcontrol.plugin.CrowdControlPlugin;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@Getter
public class RespawnCommand extends Command {
	public RespawnCommand(CrowdControlPlugin plugin) {
		super(plugin);
	}

	private final String effectName = "respawn";
	private final String displayName = "Respawn Players";

	@Override
	public Response.@NotNull Result execute(@NotNull Request request) {
		Bukkit.getScheduler().runTask(plugin, () -> CrowdControlPlugin.getPlayers()
				.forEach(player -> player.teleport(Objects.requireNonNullElseGet(player.getBedSpawnLocation(), () -> getDefaultWorld().getSpawnLocation()))));
		return Response.Result.SUCCESS;
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
