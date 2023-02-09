package dev.qixils.crowdcontrol.plugin.paper.commands;

import dev.qixils.crowdcontrol.common.util.sound.Sounds;
import dev.qixils.crowdcontrol.plugin.paper.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.paper.PaperCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.paper.utils.BlockUtil;
import dev.qixils.crowdcontrol.plugin.paper.utils.ParticleUtil;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Getter
public class TeleportCommand extends ImmediateCommand {
	private final String effectName = "chorus_fruit";

	public TeleportCommand(PaperCrowdControlPlugin plugin) {
		super(plugin);
	}

	@Override
	public Response.@NotNull Builder executeImmediately(@NotNull List<@NotNull Player> players, @NotNull Request request) {
		Response.Builder result = request.buildResponse().type(Response.ResultType.FAILURE).message("No teleportation destinations were available");
		for (Player player : players) {
			Location destination = BlockUtil.blockFinderBuilder()
					.origin(player.getLocation())
					.minRadius(3)
					.maxRadius(15)
					.locationValidator(BlockUtil.SPAWNING_SPACE)
					.build().next();
			if (destination == null)
				continue;
			destination.add(.5, 0, .5);
			if (!destination.getWorld().getWorldBorder().isInside(destination))
				continue;
			result.type(Response.ResultType.SUCCESS).message("SUCCESS");
			sync(() -> player.teleportAsync(destination).thenRun(() -> {
				ParticleUtil.spawnPlayerParticles(player, Particle.PORTAL, 100);
				player.getWorld().playSound(Sounds.TELEPORT.get(), player);
			}));
		}
		return result;
	}
}
