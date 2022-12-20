package dev.qixils.crowdcontrol.plugin.fabric.commands;

import dev.qixils.crowdcontrol.plugin.fabric.FabricCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.fabric.ImmediateCommand;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import dev.qixils.crowdcontrol.socket.Response.ResultType;
import lombok.Getter;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static dev.qixils.crowdcontrol.common.command.CommandConstants.explosionPower;
import static dev.qixils.crowdcontrol.common.command.CommandConstants.shouldSpawnFire;

@Getter
public class ExplodeCommand extends ImmediateCommand {
	private final String effectName = "explode";

	public ExplodeCommand(FabricCrowdControlPlugin plugin) {
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
				Vec3 pos = player.position().subtract(0, .5, 0);
				player.getLevel().explode(
						null,
						pos.x,
						pos.y,
						pos.z,
						power,
						fire,
						Level.ExplosionInteraction.TNT
				);
				player.setDeltaMovement(0, .5, 0);
				player.hurtMarked = true;
			});
		}

		// return
		return request.buildResponse().type(ResultType.SUCCESS);
	}
}
