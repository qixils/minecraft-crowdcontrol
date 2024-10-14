package dev.qixils.crowdcontrol.plugin.sponge8.commands;

import dev.qixils.crowdcontrol.plugin.sponge8.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.sponge8.SpongeCrowdControlPlugin;
import live.crowdcontrol.cc4j.websocket.payload.PublicEffectPayload;
import dev.qixils.crowdcontrol.socket.Response;
import dev.qixils.crowdcontrol.socket.Response.ResultType;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.world.explosion.Explosion;
import org.spongepowered.math.vector.Vector3d;

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
	public Response.Builder executeImmediately(@NotNull List<@NotNull ServerPlayer> players, @NotNull Request request) {
		// generate a random explosion ~~power~~ radius
		float power = (float) explosionPower();
		// whether the explosion should place fire blocks (5% chance)
		boolean fire = shouldSpawnFire();

		// spawn explosions
		for (ServerPlayer player : players) {
			sync(() -> {
				player.world().triggerExplosion(Explosion.builder()
						.canCauseFire(fire)
						.location(player.serverLocation().sub(0, .5, 0))
						.shouldBreakBlocks(true)
						.shouldDamageEntities(true)
						.radius(power)
						.build());
				player.offer(Keys.VELOCITY, new Vector3d(0, .5, 0));
			});
		}

		// return
		return request.buildResponse().type(ResultType.SUCCESS);
	}
}
