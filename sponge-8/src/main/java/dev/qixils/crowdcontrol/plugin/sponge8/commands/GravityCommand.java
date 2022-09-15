package dev.qixils.crowdcontrol.plugin.sponge8.commands;

import dev.qixils.crowdcontrol.TimedEffect;
import dev.qixils.crowdcontrol.plugin.sponge8.SpongeCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.sponge8.TimedCommand;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.data.value.ListValue;
import org.spongepowered.api.effect.potion.PotionEffect;
import org.spongepowered.api.effect.potion.PotionEffectTypes;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.util.Ticks;

import java.time.Duration;
import java.util.List;
import java.util.stream.StreamSupport;

import static dev.qixils.crowdcontrol.common.command.CommandConstants.POTION_SECONDS;

@Getter
public class GravityCommand extends TimedCommand {
	private final Duration duration = Duration.ofSeconds(POTION_SECONDS);
	private final String effectName;
	private final String displayName;
	private final int level;

	private GravityCommand(SpongeCrowdControlPlugin plugin, String effectName, String displayName, int level) {
		super(plugin);
		this.effectName = effectName;
		this.displayName = displayName;
		this.level = level;
	}

	@Override
	public void voidExecute(@NotNull List<@NotNull ServerPlayer> ignored, @NotNull Request request) {
		new TimedEffect.Builder()
				.request(request)
				.duration(duration)
				.startCallback($ -> {
					List<ServerPlayer> players = plugin.getPlayers(request);
					Response.Builder response = request.buildResponse()
							.type(Response.ResultType.RETRY)
							.message("A conflicting potion effect is already active");
					for (ServerPlayer player : players) {
						ListValue.Mutable<PotionEffect> effects = player.potionEffects();
						if (StreamSupport.stream(effects.spliterator(), false).anyMatch(e -> e.type().equals(PotionEffectTypes.LEVITATION.get())))
							continue;
						sync(() -> {
							effects.add(PotionEffect.builder()
									.potionType(PotionEffectTypes.LEVITATION)
									.duration(Ticks.ofWallClockSeconds(plugin.getGame().server(), POTION_SECONDS))
									.amplifier(level)
									.ambient(true)
									.showParticles(true)
									.showIcon(false)
									.build());
							player.offer(effects);
						});
						response.type(Response.ResultType.SUCCESS).message("SUCCESS");
					}
					return response;
				})
				.build().queue();
	}

	@NotNull
	public static GravityCommand zero(SpongeCrowdControlPlugin plugin) {
		return new GravityCommand(plugin, "zero_gravity", "Zero Gravity", 255);
	}

	@NotNull
	public static GravityCommand low(SpongeCrowdControlPlugin plugin) {
		return new GravityCommand(plugin, "low_gravity", "Low Gravity", 254);
	}

	@NotNull
	public static GravityCommand high(SpongeCrowdControlPlugin plugin) {
		return new GravityCommand(plugin, "high_gravity", "High Gravity", 250);
	}

	@NotNull
	public static GravityCommand maximum(SpongeCrowdControlPlugin plugin) {
		return new GravityCommand(plugin, "maximum_gravity", "Maximum Gravity", 128);
	}
}
