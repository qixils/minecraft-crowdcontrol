package dev.qixils.crowdcontrol.plugin.paper.commands;

import dev.qixils.crowdcontrol.TimedEffect;
import dev.qixils.crowdcontrol.plugin.paper.PaperCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.paper.TimedVoidCommand;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.List;

import static dev.qixils.crowdcontrol.TimedEffect.isActive;
import static dev.qixils.crowdcontrol.common.command.CommandConstants.POTION_DURATION;

@Getter
public class GravityCommand extends TimedVoidCommand {
	private final Duration defaultDuration = POTION_DURATION;
	private final String effectName;
	private final int level;

	private GravityCommand(PaperCrowdControlPlugin plugin, String effectName, int level) {
		super(plugin);
		this.effectName = effectName;
		this.level = level;
	}

	@Override
	public void voidExecute(@NotNull List<@NotNull Player> ignored, @NotNull Request request) {
		if (isActive("walk", request)) {
			request.buildResponse().type(Response.ResultType.RETRY).message("Cannot fling while frozen").send();
			return;
		}
		new TimedEffect.Builder()
				.request(request)
				.effectGroup("gravity")
				.duration(getDuration(request))
				.startCallback(effect -> {
					List<Player> players = plugin.getPlayers(request);
					Response.Builder response = request.buildResponse()
							.type(Response.ResultType.RETRY)
							.message("A conflicting potion effect is already active");
					for (Player player : players) {
						if (player.hasPotionEffect(PotionEffectType.LEVITATION))
							continue;
						sync(() -> player.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, (int) effect.getCurrentDuration().getSeconds() * 20, level, true, true, false)));
						response.type(Response.ResultType.SUCCESS).message("SUCCESS");
					}
					if (response.type() == Response.ResultType.SUCCESS)
						playerAnnounce(players, request);
					return response;
				})
				.build().queue();
	}

	@NotNull
	public static GravityCommand zero(PaperCrowdControlPlugin plugin) {
		return new GravityCommand(plugin, "zero_gravity", 255);
	}

	@NotNull
	public static GravityCommand low(PaperCrowdControlPlugin plugin) {
		return new GravityCommand(plugin, "low_gravity", 254);
	}

	@NotNull
	public static GravityCommand high(PaperCrowdControlPlugin plugin) {
		return new GravityCommand(plugin, "high_gravity", 250);
	}

	@NotNull
	public static GravityCommand maximum(PaperCrowdControlPlugin plugin) {
		return new GravityCommand(plugin, "maximum_gravity", 179);
	}
}
