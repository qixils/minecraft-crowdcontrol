package dev.qixils.crowdcontrol.plugin.fabric.commands;

import dev.qixils.crowdcontrol.plugin.fabric.FabricCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.fabric.ImmediateCommand;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import dev.qixils.crowdcontrol.socket.Response.ResultType;
import lombok.Getter;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Getter
public class ExperienceCommand extends ImmediateCommand {
	private final String effectName;
	private final int amount;

	public ExperienceCommand(FabricCrowdControlPlugin plugin, String effectName, int amount) {
		super(plugin);
		this.effectName = effectName;
		assert amount != 0;
		this.amount = amount;
	}

	@Override
	public Response.@NotNull Builder executeImmediately(@NotNull List<@NotNull ServerPlayer> players, @NotNull Request request) {
		Response.Builder resp = request.buildResponse()
				.type(ResultType.FAILURE)
				.message("Player does not have enough XP levels");
		for (ServerPlayer player : players) {
			int curLevel = player.experienceLevel;
			int newLevel = curLevel + amount;
			if (newLevel >= 0) {
				resp.type(ResultType.SUCCESS).message("SUCCESS");
				sync(() -> player.setExperienceLevels(newLevel));
			}
		}
		return resp;
	}
}
