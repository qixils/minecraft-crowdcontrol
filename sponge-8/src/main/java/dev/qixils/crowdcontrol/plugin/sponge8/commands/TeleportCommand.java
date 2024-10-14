package dev.qixils.crowdcontrol.plugin.sponge8.commands;

import dev.qixils.crowdcontrol.common.util.sound.Sounds;
import dev.qixils.crowdcontrol.plugin.sponge8.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.sponge8.SpongeCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.sponge8.utils.BlockFinder;
import live.crowdcontrol.cc4j.websocket.payload.PublicEffectPayload;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import net.kyori.adventure.sound.Sound;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.effect.particle.ParticleEffect;
import org.spongepowered.api.effect.particle.ParticleTypes;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.math.vector.Vector3d;

import java.util.List;

import static dev.qixils.crowdcontrol.TimedEffect.isActive;
import static dev.qixils.crowdcontrol.common.command.CommandConstants.EAT_CHORUS_FRUIT_MAX_RADIUS;
import static dev.qixils.crowdcontrol.common.command.CommandConstants.EAT_CHORUS_FRUIT_MIN_RADIUS;

@Getter
public class TeleportCommand extends ImmediateCommand {
	private final String effectName = "chorus_fruit";

	public TeleportCommand(SpongeCrowdControlPlugin plugin) {
		super(plugin);
	}

	@Override
	public Response.@NotNull Builder executeImmediately(@NotNull List<@NotNull ServerPlayer> players, @NotNull Request request) {
		if (isActive("walk", request) || isActive("look", request))
			return request.buildResponse().type(Response.ResultType.RETRY).message("Cannot fling while frozen");
		Response.Builder result = request.buildResponse()
				.type(Response.ResultType.RETRY)
				.message("No teleportation destinations were available");
		for (ServerPlayer player : players) {
			ServerLocation tempDest = BlockFinder.builder()
					.origin(player.serverLocation())
					.minRadius(EAT_CHORUS_FRUIT_MIN_RADIUS)
					.maxRadius(EAT_CHORUS_FRUIT_MAX_RADIUS)
					.locationValidator(BlockFinder.SPAWNING_SPACE)
					.build().next();
			if (tempDest == null) {
				continue;
			}
			final ServerLocation destination = tempDest.add(.5, 0, .5);
			result.type(Response.ResultType.SUCCESS).message("SUCCESS");
			sync(() -> {
				player.setLocation(destination);
				SpongeCrowdControlPlugin.spawnPlayerParticles(
						player,
						ParticleEffect.builder()
								.type(ParticleTypes.PORTAL)
								.quantity(100)
								.offset(new Vector3d(.5d, 1d, .5d))
								.build()
				);
				player.world().playSound(Sounds.TELEPORT.get(), Sound.Emitter.self());
			});
		}
		return result;
	}
}
