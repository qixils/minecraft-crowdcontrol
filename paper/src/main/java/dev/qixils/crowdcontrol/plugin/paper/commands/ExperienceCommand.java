package dev.qixils.crowdcontrol.plugin.paper.commands;

import dev.qixils.crowdcontrol.plugin.paper.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.paper.PaperCrowdControlPlugin;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import dev.qixils.crowdcontrol.socket.Response.ResultType;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Getter
public class ExperienceCommand extends ImmediateCommand {
	private final String effectName;
	private final int amount;

	public ExperienceCommand(PaperCrowdControlPlugin plugin, String effectName, int amount) {
		super(plugin);
		this.effectName = effectName;
		assert amount != 0;
		this.amount = amount;
	}

	@Override
	public Response.@NotNull Builder executeImmediately(@NotNull List<@NotNull Player> players, @NotNull Request request) {
		Response.Builder resp = request.buildResponse()
				.type(ResultType.FAILURE)
				.message("Player does not have enough XP levels");
		for (Player player : players) {
			int curLevel = player.getLevel();
			int newLevel = curLevel + amount;
			if (newLevel >= 0) {
				resp.type(ResultType.SUCCESS).message("SUCCESS");
				sync(() -> player.setLevel(newLevel));
			}
		}
		return resp;
	}
}
