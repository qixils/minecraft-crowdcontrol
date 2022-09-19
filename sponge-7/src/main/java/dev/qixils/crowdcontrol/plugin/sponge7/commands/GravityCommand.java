package dev.qixils.crowdcontrol.plugin.sponge7.commands;

import dev.qixils.crowdcontrol.TimedEffect;
import dev.qixils.crowdcontrol.plugin.sponge7.SpongeCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.sponge7.TimedCommand;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.effect.potion.PotionEffect;
import org.spongepowered.api.effect.potion.PotionEffectTypes;
import org.spongepowered.api.entity.living.player.Player;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static dev.qixils.crowdcontrol.common.command.CommandConstants.POTION_SECONDS;

@Getter
public class GravityCommand extends TimedCommand {
	private final Duration duration = Duration.ofSeconds(POTION_SECONDS);
	private final String effectName;
	private final int level;

	private GravityCommand(SpongeCrowdControlPlugin plugin, String effectName, int level) {
		super(plugin);
		this.effectName = effectName;
		this.level = level;
	}

	@Override
	public void voidExecute(@NotNull List<@NotNull Player> ignored, @NotNull Request request) {
		new TimedEffect.Builder()
				.request(request)
				.duration(duration)
				.startCallback($ -> {
					List<Player> players = plugin.getPlayers(request);
					Response.Builder response = request.buildResponse()
							.type(Response.ResultType.RETRY)
							.message("A conflicting potion effect is already active");
					for (Player player : players) {
						List<PotionEffect> effects = player.get(Keys.POTION_EFFECTS).orElseGet(ArrayList::new);
						if (effects.stream().anyMatch(e -> e.getType().equals(PotionEffectTypes.LEVITATION)))
							continue;
						sync(() -> {
							effects.add(PotionEffect.builder()
									.potionType(PotionEffectTypes.LEVITATION)
									.duration(POTION_SECONDS * 20)
									.amplifier(level)
									.ambience(true)
									.particles(true)
									.build());
							player.offer(Keys.POTION_EFFECTS, effects);
						});
						response.type(Response.ResultType.SUCCESS).message("SUCCESS");
					}
					return response;
				})
				.build().queue();
	}

	@NotNull
	public static GravityCommand zero(SpongeCrowdControlPlugin plugin) {
		return new GravityCommand(plugin, "zero_gravity", 255);
	}

	@NotNull
	public static GravityCommand low(SpongeCrowdControlPlugin plugin) {
		return new GravityCommand(plugin, "low_gravity", 254);
	}

	@NotNull
	public static GravityCommand high(SpongeCrowdControlPlugin plugin) {
		return new GravityCommand(plugin, "high_gravity", 250);
	}

	@NotNull
	public static GravityCommand maximum(SpongeCrowdControlPlugin plugin) {
		return new GravityCommand(plugin, "maximum_gravity", 128);
	}
}
