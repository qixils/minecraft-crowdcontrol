package dev.qixils.crowdcontrol.plugin.paper.commands;

import dev.qixils.crowdcontrol.TimedEffect;
import dev.qixils.crowdcontrol.common.Global;
import dev.qixils.crowdcontrol.plugin.paper.PaperCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.paper.TimedVoidCommand;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.List;

@Global
@Getter
public class TickFreezeCommand extends TimedVoidCommand {
	private final Duration defaultDuration = Duration.ofSeconds(20);
	private final String effectName = "tick_freeze";

	public TickFreezeCommand(PaperCrowdControlPlugin plugin) {
		super(plugin);
	}

	@Override
	public void voidExecute(@NotNull List<@NotNull Player> players, @NotNull Request request) {
		new TimedEffect.Builder()
			.request(request)
			.effectGroup("tick_rate")
			.duration(getDuration(request))
			.startCallback(effect -> {
				Bukkit.getServerTickManager().setFrozen(true);
				playerAnnounce(players, request);
				return request.buildResponse().type(Response.ResultType.SUCCESS);
			})
			.completionCallback(effect -> {
				Bukkit.getServerTickManager().setFrozen(false);
			})
			.build().queue();
	}
}
