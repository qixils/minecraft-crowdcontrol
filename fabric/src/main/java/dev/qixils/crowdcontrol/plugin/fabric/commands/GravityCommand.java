package dev.qixils.crowdcontrol.plugin.fabric.commands;

import dev.qixils.crowdcontrol.TimedEffect;
import dev.qixils.crowdcontrol.plugin.fabric.FabricCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.fabric.TimedVoidCommand;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.List;

import static dev.qixils.crowdcontrol.common.command.CommandConstants.POTION_DURATION;

@Getter
public class GravityCommand extends TimedVoidCommand {
	private final Duration defaultDuration = POTION_DURATION;
	private final String effectName;
	private final int level;

	private GravityCommand(FabricCrowdControlPlugin plugin, String effectName, int level) {
		super(plugin);
		this.effectName = effectName;
		this.level = level;
	}

	@Override
	public void voidExecute(@NotNull List<@NotNull ServerPlayerEntity> ignored, @NotNull Request request) {
		new TimedEffect.Builder()
				.request(request)
				.effectGroup("gravity")
				.duration(getDuration(request))
				.startCallback($ -> {
					List<ServerPlayerEntity> players = plugin.getPlayers(request);
					Response.Builder response = request.buildResponse()
							.type(Response.ResultType.RETRY)
							.message("A conflicting potion effect is already active");
					for (PlayerEntity player : players) {
						if (player.hasStatusEffect(StatusEffects.LEVITATION))
							continue;
						sync(() -> player.addStatusEffect(new StatusEffectInstance(StatusEffects.LEVITATION, (int) getDuration(request).getSeconds() * 20, level, true, true, false)));
						response.type(Response.ResultType.SUCCESS).message("SUCCESS");
					}
					if (response.type() == Response.ResultType.SUCCESS)
						playerAnnounce(players, request);
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
