package dev.qixils.crowdcontrol.plugin.fabric.commands;

import dev.qixils.crowdcontrol.TimedEffect;
import dev.qixils.crowdcontrol.plugin.fabric.FabricCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.fabric.TimedCommand;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.List;

import static dev.qixils.crowdcontrol.common.command.CommandConstants.POTION_SECONDS;

@Getter
public class GravityCommand extends TimedCommand {
	private final Duration defaultDuration = Duration.ofSeconds(POTION_SECONDS);
	private final String effectName;
	private final int level;

	private GravityCommand(FabricCrowdControlPlugin plugin, String effectName, int level) {
		super(plugin);
		this.effectName = effectName;
		this.level = level;
	}

	@Override
	public void voidExecute(@NotNull List<@NotNull ServerPlayer> ignored, @NotNull Request request) {
		new TimedEffect.Builder()
				.request(request)
				.duration(getDuration(request))
				.startCallback($ -> {
					List<ServerPlayer> players = plugin.getPlayers(request);
					Response.Builder response = request.buildResponse()
							.type(Response.ResultType.RETRY)
							.message("A conflicting potion effect is already active");
					for (Player player : players) {
						if (player.hasEffect(MobEffects.LEVITATION))
							continue;
						sync(() -> player.addEffect(new MobEffectInstance(MobEffects.LEVITATION, POTION_SECONDS * 20, level, true, true, false)));
						response.type(Response.ResultType.SUCCESS).message("SUCCESS");
					}
					return response;
				})
				.build().queue();
	}

	@NotNull
	public static GravityCommand zero(FabricCrowdControlPlugin plugin) {
		return new GravityCommand(plugin, "zero_gravity", 255);
	}

	@NotNull
	public static GravityCommand low(FabricCrowdControlPlugin plugin) {
		return new GravityCommand(plugin, "low_gravity", 254);
	}

	@NotNull
	public static GravityCommand high(FabricCrowdControlPlugin plugin) {
		return new GravityCommand(plugin, "high_gravity", 250);
	}

	@NotNull
	public static GravityCommand maximum(FabricCrowdControlPlugin plugin) {
		return new GravityCommand(plugin, "maximum_gravity", 179);
	}
}
