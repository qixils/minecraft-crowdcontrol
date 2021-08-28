package dev.qixils.crowdcontrol.plugin.commands;

import dev.qixils.crowdcontrol.plugin.Command;
import dev.qixils.crowdcontrol.plugin.CrowdControlPlugin;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

@Getter
public class RespawnCommand extends Command {
	public RespawnCommand(CrowdControlPlugin plugin) {
		super(plugin);
	}

	private final String effectName = "respawn";
	private final String displayName = "Respawn Players";

	@Override
	public Response.@NotNull Result execute(@NotNull Request request) {
		// TODO: this is kinda janky especially for the nether/end, could PR a respawn method upstream
		Bukkit.getScheduler().runTask(plugin, () -> CrowdControlPlugin.getPlayers().forEach(player -> player.teleport(Optional.ofNullable(player.getBedSpawnLocation()).orElse(player.getWorld().getSpawnLocation()))));
		return Response.Result.SUCCESS;
	}
}
