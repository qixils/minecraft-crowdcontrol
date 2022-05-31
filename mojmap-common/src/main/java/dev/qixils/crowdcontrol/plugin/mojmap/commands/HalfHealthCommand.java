package dev.qixils.crowdcontrol.plugin.mojmap.commands;

import dev.qixils.crowdcontrol.plugin.mojmap.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.mojmap.MojmapPlugin;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import dev.qixils.crowdcontrol.socket.Response.ResultType;
import lombok.Getter;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Getter
public class HalfHealthCommand extends ImmediateCommand {
	private final String effectName = "half_health";
	private final String displayName = "Half Health";

	public HalfHealthCommand(MojmapPlugin<?> plugin) {
		super(plugin);
	}

	@NotNull
	@Override
	public Response.Builder executeImmediately(@NotNull List<@NotNull ServerPlayer> players, @NotNull Request request) {
		Response.Builder response = request.buildResponse()
				.type(ResultType.FAILURE)
				.message("Health is already minimum");

		for (ServerPlayer player : players) {
			float health = player.getHealth();
			if (health > 0.5) {
				response.type(ResultType.SUCCESS).message("SUCCESS");
				// TODO: test this new one-liner on fabric, sponge 7, and sponge 8
				sync(() -> player.hurt(DamageSource.MAGIC, health / 2f));
			}
		}

		return response;
	}
}