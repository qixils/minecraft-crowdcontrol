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
public class TickRateCommand extends TimedVoidCommand {
	private static final float RATE = 20f;
	private final Duration defaultDuration = Duration.ofSeconds(20);
	private final String effectName;
	private final float multiplier;

	private TickRateCommand(PaperCrowdControlPlugin plugin, String effectName, float multiplier) {
		super(plugin);
		this.effectName = effectName;
		this.multiplier = multiplier;
	}

	@Override
	public void voidExecute(@NotNull List<@NotNull Player> players, @NotNull Request request) {
		new TimedEffect.Builder()
			.request(request)
			.effectGroup("tick_rate")
			.duration(request.getDuration())
			.startCallback(effect -> {
				Bukkit.getServerTickManager().setTickRate(RATE * multiplier);
				playerAnnounce(players, request);
				return request.buildResponse().type(Response.ResultType.SUCCESS);
			})
			.completionCallback(effect -> {
				Bukkit.getServerTickManager().setTickRate(RATE);
			})
			.build().queue();
	}

	public static TickRateCommand doubleRate(PaperCrowdControlPlugin plugin) {
		return new TickRateCommand(plugin, "tick_double", 2.0f);
	}

	public static TickRateCommand halfRate(PaperCrowdControlPlugin plugin) {
		return new TickRateCommand(plugin, "tick_halve", 0.5f);
	}
}
