package dev.qixils.crowdcontrol.plugin.sponge8.commands;

import dev.qixils.crowdcontrol.common.util.sound.Sounds;
import dev.qixils.crowdcontrol.plugin.sponge8.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.sponge8.SpongeCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.sponge8.utils.BlockFinder;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.effect.particle.ParticleEffect;
import org.spongepowered.api.effect.particle.ParticleTypes;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.math.vector.Vector3d;

import java.util.List;

@Getter
public class TeleportCommand extends ImmediateCommand {
	private final String effectName = "chorus_fruit";
	private final String displayName = "Eat Chorus Fruit";

	public TeleportCommand(SpongeCrowdControlPlugin plugin) {
		super(plugin);
	}

	@Override
	public Response.@NotNull Builder executeImmediately(@NotNull List<@NotNull ServerPlayer> players, @NotNull Request request) {
		Response.Builder result = request.buildResponse()
				.type(Response.ResultType.FAILURE)
				.message("No teleportation destinations were available");
		for (ServerPlayer player : players) {
			ServerLocation tempDest = BlockFinder.builder()
					.origin(player.serverLocation())
					.minRadius(3)
					.maxRadius(15)
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
				player.world().playSound(Sounds.TELEPORT.get(), destination.position());
			});
		}
		return result;
	}
}
