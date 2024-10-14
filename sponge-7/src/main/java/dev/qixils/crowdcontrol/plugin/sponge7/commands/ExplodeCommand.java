package dev.qixils.crowdcontrol.plugin.sponge7.commands;

import com.flowpowered.math.vector.Vector3d;
import dev.qixils.crowdcontrol.plugin.sponge7.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.sponge7.SpongeCrowdControlPlugin;
import dev.qixils.crowdcontrol.socket.Response;
import dev.qixils.crowdcontrol.socket.Response.ResultType;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.world.explosion.Explosion;

import java.util.List;

import static dev.qixils.crowdcontrol.common.command.CommandConstants.explosionPower;
import static dev.qixils.crowdcontrol.common.command.CommandConstants.shouldSpawnFire;

@Getter
public class ExplodeCommand extends ImmediateCommand {
	private final String effectName = "explode";

	public ExplodeCommand(SpongeCrowdControlPlugin plugin) {
		super(plugin);
	}

	@NotNull
	@Override
	public Response.Builder executeImmediately(@NotNull List<@NotNull Player> players, @NotNull Request request) {
		// generate a random explosion ~~power~~ radius
		float power = (float) explosionPower();
		// whether the explosion should place fire blocks (5% chance)
		boolean fire = shouldSpawnFire();

		// spawn explosions
		for (Player player : players) {
			sync(() -> {
				player.getWorld().triggerExplosion(Explosion.builder()
						.canCauseFire(fire)
						.location(player.getLocation().sub(0, .5, 0))
						.shouldBreakBlocks(true)
						.shouldDamageEntities(true)
						.radius(power)
						.build());
				player.setVelocity(new Vector3d(0, .5, 0));
			});
		}

		// return
		return request.buildResponse().type(ResultType.SUCCESS);
	}
}
