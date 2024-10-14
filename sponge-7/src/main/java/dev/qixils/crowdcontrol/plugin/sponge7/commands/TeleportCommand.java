package dev.qixils.crowdcontrol.plugin.sponge7.commands;

import com.flowpowered.math.vector.Vector3d;
import dev.qixils.crowdcontrol.common.util.sound.Sounds;
import dev.qixils.crowdcontrol.plugin.sponge7.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.sponge7.SpongeCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.sponge7.utils.BlockFinder;
import live.crowdcontrol.cc4j.websocket.payload.PublicEffectPayload;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.effect.particle.ParticleEffect;
import org.spongepowered.api.effect.particle.ParticleTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

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
	public Response.@NotNull Builder executeImmediately(@NotNull List<@NotNull Player> players, @NotNull Request request) {
		if (isActive("walk", request) || isActive("look", request))
			return request.buildResponse().type(Response.ResultType.RETRY).message("Cannot fling while frozen");
		Response.Builder result = request.buildResponse()
				.type(Response.ResultType.RETRY)
				.message("No teleportation destinations were available");
		for (Player player : players) {
			Location<World> tempDest = BlockFinder.builder()
					.origin(player.getLocation())
					.minRadius(EAT_CHORUS_FRUIT_MIN_RADIUS)
					.maxRadius(EAT_CHORUS_FRUIT_MAX_RADIUS)
					.locationValidator(BlockFinder.SPAWNING_SPACE)
					.build().next();
			if (tempDest == null) {
				continue;
			}
			final Location<World> destination = tempDest.add(.5, 0, .5);
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
				plugin.asAudience(player.getWorld()).playSound(
						Sounds.TELEPORT.get(),
						destination.getX(),
						destination.getY(),
						destination.getZ()
				);
			});
		}
		return result;
	}
}
