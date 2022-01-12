package dev.qixils.crowdcontrol.plugin.sponge7.commands;

import com.flowpowered.math.vector.Vector3d;
import dev.qixils.crowdcontrol.plugin.sponge7.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.sponge7.SpongeCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.sponge7.utils.BlockFinder;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.effect.particle.ParticleEffect;
import org.spongepowered.api.effect.particle.ParticleTypes;
import org.spongepowered.api.effect.sound.SoundCategories;
import org.spongepowered.api.effect.sound.SoundTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.List;

@Getter
public class TeleportCommand extends ImmediateCommand {
	private final String effectName = "chorus_fruit";
	private final String displayName = "Eat Chorus Fruit";

	public TeleportCommand(SpongeCrowdControlPlugin plugin) {
		super(plugin);
	}

	@Override
	public Response.@NotNull Builder executeImmediately(@NotNull List<@NotNull Player> players, @NotNull Request request) {
		Response.Builder result = request.buildResponse()
				.type(Response.ResultType.FAILURE)
				.message("No teleportation destinations were available");
		for (Player player : players) {
			Location<World> tempDest = BlockFinder.builder()
					.origin(player.getLocation())
					.minRadius(3)
					.maxRadius(15)
					.locationValidator(BlockFinder.SPAWNING_SPACE)
					.build().next();
			if (tempDest == null) {
				continue;
			}
			final Location<World> destination = tempDest.add(.5, 0, .5);
			result.type(Response.ResultType.SUCCESS).message("SUCCESS");
			sync(() -> {
				player.setLocation(destination.getBlockRelative(Direction.NONE));
				SpongeCrowdControlPlugin.spawnPlayerParticles(
						player,
						ParticleEffect.builder()
								.type(ParticleTypes.PORTAL)
								.quantity(100)
								.offset(new Vector3d(.5d, 1d, .5d))
								.build()
				);
				player.getWorld().playSound(
						SoundTypes.ENTITY_ENDERMEN_TELEPORT,
						SoundCategories.AMBIENT,
						destination.getPosition(),
						1d,
						1d
				);
			});
		}
		return result;
	}
}
