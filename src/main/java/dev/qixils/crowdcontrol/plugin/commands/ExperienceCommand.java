package dev.qixils.crowdcontrol.plugin.commands;

import dev.qixils.crowdcontrol.plugin.CrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.ImmediateCommand;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Getter
public class ExperienceCommand extends ImmediateCommand {
	private final String effectName;
	private final String displayName;
	private final int amount;

	public ExperienceCommand(CrowdControlPlugin plugin, String effectName, String displayName, int amount) {
		super(plugin);
		this.effectName = effectName;
		this.displayName = displayName;
		this.amount = amount;
	}

	@Override
	public Response.@NotNull Builder executeImmediately(@NotNull List<@NotNull Player> players, @NotNull Request request) {
		Bukkit.getScheduler().runTask(plugin, () -> players.forEach(player -> player.setLevel(player.getLevel() + amount)));
		return request.buildResponse().type(Response.ResultType.SUCCESS);
	}
}
