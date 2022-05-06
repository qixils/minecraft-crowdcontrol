package dev.qixils.crowdcontrol.plugin.mojmap.commands;

import dev.qixils.crowdcontrol.plugin.mojmap.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.mojmap.MojmapPlugin;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Getter
public class ResetExpProgressCommand extends ImmediateCommand {
	private final String effectName = "reset_exp_progress";
	private final String displayName = "Reset Experience Progress";

	public ResetExpProgressCommand(MojmapPlugin<?> plugin) {
		super(plugin);
	}

	@Override
	public Response.@NotNull Builder executeImmediately(@NotNull List<@NotNull ServerPlayer> players, @NotNull Request request) {
		Response.Builder result = request.buildResponse().type(Response.ResultType.FAILURE).message("No players have XP");
		for (ServerPlayer player : players) {
			if (player.totalExperience > 0 || player.experienceProgress > 0 || player.experienceLevel > 0) {
				result.type(Response.ResultType.SUCCESS).message("SUCCESS");
				sync(() -> {
					player.setExperiencePoints(0);
					player.setExperienceLevels(0);
				});
			}
		}
		return result;
	}
}
