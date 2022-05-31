package dev.qixils.crowdcontrol.plugin.mojmap.commands;

import dev.qixils.crowdcontrol.common.util.sound.Sounds;
import dev.qixils.crowdcontrol.plugin.mojmap.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.mojmap.MojmapPlugin;
import dev.qixils.crowdcontrol.plugin.mojmap.utils.BlockFinder;
import dev.qixils.crowdcontrol.plugin.mojmap.utils.Location;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import net.kyori.adventure.sound.Sound;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Getter
public class TeleportCommand extends ImmediateCommand {
	private final String effectName = "chorus_fruit";
	private final String displayName = "Eat Chorus Fruit";

	public TeleportCommand(MojmapPlugin<?> plugin) {
		super(plugin);
	}

	@Override
	public Response.@NotNull Builder executeImmediately(@NotNull List<@NotNull ServerPlayer> players, @NotNull Request request) {
		Response.Builder result = request.buildResponse()
				.type(Response.ResultType.FAILURE)
				.message("No teleportation destinations were available");
		for (ServerPlayer player : players) {
			Location tempDest = BlockFinder.builder()
					.origin(player)
					.minRadius(3)
					.maxRadius(15)
					.locationValidator(BlockFinder.SPAWNING_SPACE)
					.build().next();
			if (tempDest == null) {
				continue;
			}
			final Location destination = tempDest.add(.5, 0, .5);
			result.type(Response.ResultType.SUCCESS).message("SUCCESS");
			sync(() -> {
				destination.teleportHere(player);
				destination.atVertCeil()
						.buildParticleEffect(ParticleTypes.PORTAL)
						.count(100)
						.distance(.5f, 1f, .5f)
						.send();
				plugin.adventure().world(player.level).playSound(
						Sounds.TELEPORT.get(),
						Sound.Emitter.self()
				);
			});
		}
		return result;
	}
}