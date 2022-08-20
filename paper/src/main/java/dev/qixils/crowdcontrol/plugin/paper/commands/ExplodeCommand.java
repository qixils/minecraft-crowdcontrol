package dev.qixils.crowdcontrol.plugin.paper.commands;

import dev.qixils.crowdcontrol.plugin.paper.Command;
import dev.qixils.crowdcontrol.plugin.paper.PaperCrowdControlPlugin;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response.Builder;
import dev.qixils.crowdcontrol.socket.Response.ResultType;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static dev.qixils.crowdcontrol.common.command.CommandConstants.explosionPower;
import static dev.qixils.crowdcontrol.common.command.CommandConstants.shouldSpawnFire;

@Getter
public class ExplodeCommand extends Command {
	private final String displayName = "Explode";
	private final String effectName = "explode";

	public ExplodeCommand(PaperCrowdControlPlugin plugin) {
		super(plugin);
	}

	@Override
	public @NotNull CompletableFuture<@Nullable Builder> execute(@NotNull List<@NotNull Player> players, @NotNull Request request) {
		CompletableFuture<Boolean> future = new CompletableFuture<>();
		// generate a random explosion power
		float power = (float) explosionPower();
		// whether the explosion should place fire blocks (5% chance)
		boolean fire = shouldSpawnFire();

		// spawn explosions
		sync(() -> {
			boolean success = false;
			for (Player player : players) {
				if (player.getWorld().createExplosion(
						player.getLocation().subtract(0, .5, 0),
						power,
						fire,
						true
				)) {
					success = true;
					player.setVelocity(new Vector(0, .5, 0));
				}
			}
			future.complete(success);
		});

		// return
		return future.thenApply(success -> success
				? request.buildResponse().type(ResultType.SUCCESS)
				: request.buildResponse().type(ResultType.FAILURE).message("Explosions were cancelled by another plugin :("));
	}
}
