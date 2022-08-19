package dev.qixils.crowdcontrol.plugin.sponge7.commands;

import dev.qixils.crowdcontrol.plugin.sponge7.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.sponge7.SpongeCrowdControlPlugin;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import dev.qixils.crowdcontrol.socket.Response.ResultType;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.entity.damage.source.DamageSources;

import java.util.List;

import static dev.qixils.crowdcontrol.common.CommandConstants.HALVE_HEALTH_MIN_HEALTH;

@Getter
public class HalfHealthCommand extends ImmediateCommand {
	private final String effectName = "half_health";
	private final String displayName = "Half Health";

	public HalfHealthCommand(SpongeCrowdControlPlugin plugin) {
		super(plugin);
	}

	@NotNull
	@Override
	public Response.Builder executeImmediately(@NotNull List<@NotNull Player> players, @NotNull Request request) {
		Response.Builder response = request.buildResponse()
				.type(ResultType.FAILURE)
				.message("Health is already minimum");

		for (Player player : players) {
			double health = player.getHealthData().health().get();
			if (health > HALVE_HEALTH_MIN_HEALTH) {
				response.type(ResultType.SUCCESS).message("SUCCESS");
				sync(() -> player.damage(health / 2, DamageSources.GENERIC));
			}
		}

		return response;
	}
}
