package dev.qixils.crowdcontrol.plugin.commands;

import dev.qixils.crowdcontrol.plugin.BukkitCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.utils.BlockUtil;
import dev.qixils.crowdcontrol.plugin.utils.ParticleUtil;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Getter
public class TeleportCommand extends ImmediateCommand {
	private final String effectName = "chorus_fruit";
	private final String displayName = "Eat Chorus Fruit";

	public TeleportCommand(BukkitCrowdControlPlugin plugin) {
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
			if (destination == null) {
				continue;
			}
			result.type(Response.ResultType.SUCCESS).message("SUCCESS");
			Bukkit.getScheduler().runTask(plugin, () -> {
				player.teleport(destination);
				ParticleUtil.spawnPlayerParticles(player, Particle.PORTAL, 100);
				player.getWorld().playSound(destination, Sound.ENTITY_ENDERMAN_TELEPORT, SoundCategory.AMBIENT, 1.0f, 1.0f);
			});
		}
		return result;
	}
}
