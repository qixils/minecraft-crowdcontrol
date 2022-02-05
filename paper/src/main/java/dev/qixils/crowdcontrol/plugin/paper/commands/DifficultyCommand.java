package dev.qixils.crowdcontrol.plugin.paper.commands;

import dev.qixils.crowdcontrol.plugin.paper.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.paper.PaperCrowdControlPlugin;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import dev.qixils.crowdcontrol.socket.Response.ResultType;
import lombok.Getter;
import org.bukkit.Difficulty;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Getter
public class DifficultyCommand extends ImmediateCommand {
	private final Difficulty difficulty;
	private final String effectName;
	private final String displayName;

	public DifficultyCommand(PaperCrowdControlPlugin plugin, Difficulty difficulty) {
		super(plugin);
		this.difficulty = difficulty;
		this.effectName = "difficulty_" + difficulty.name();
		this.displayName = "Set Difficulty: " + plugin.getTextUtil().translate(difficulty);
	}

	@Override
	public Response.@NotNull Builder executeImmediately(@NotNull List<@NotNull Player> players, @NotNull Request request) {
		if (!isGlobalCommandUsable(players, request))
			return globalCommandUnusable(request);

		boolean success = false;
		for (World world : plugin.getServer().getWorlds()) {
			if (world.getDifficulty() != difficulty) {
				success = true;
				world.setDifficulty(difficulty);
			}
		}

		if (success)
			return request.buildResponse().type(ResultType.SUCCESS);
		else
			return request.buildResponse().type(ResultType.FAILURE).message("World is already on that difficulty");
	}
}
