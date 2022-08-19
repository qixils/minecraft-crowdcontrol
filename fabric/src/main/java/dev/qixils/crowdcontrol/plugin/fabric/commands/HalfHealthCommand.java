package dev.qixils.crowdcontrol.plugin.fabric.commands;

import dev.qixils.crowdcontrol.plugin.fabric.FabricCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.fabric.ImmediateCommand;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import dev.qixils.crowdcontrol.socket.Response.ResultType;
import lombok.Getter;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static dev.qixils.crowdcontrol.common.CommandConstants.HALVE_HEALTH_MIN_HEALTH;

@Getter
public class HalfHealthCommand extends ImmediateCommand {
	private final String effectName = "half_health";
	private final String displayName = "Half Health";

	public HalfHealthCommand(FabricCrowdControlPlugin plugin) {
		super(plugin);
	}

	@NotNull
	@Override
	public Response.Builder executeImmediately(@NotNull List<@NotNull ServerPlayer> players, @NotNull Request request) {
		Response.Builder response = request.buildResponse()
				.type(ResultType.RETRY)
				.message("Health is already minimum");

		for (ServerPlayer player : players) {
			float health = player.getHealth();
			if (health > HALVE_HEALTH_MIN_HEALTH) {
				response.type(ResultType.SUCCESS).message("SUCCESS");
				sync(() -> player.hurt(DamageSource.MAGIC, health / 2f));
			}
		}

		return response;
	}
}
