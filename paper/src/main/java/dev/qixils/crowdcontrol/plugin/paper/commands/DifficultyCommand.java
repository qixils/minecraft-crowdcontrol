package dev.qixils.crowdcontrol.plugin.paper.commands;

import dev.qixils.crowdcontrol.common.Global;
import dev.qixils.crowdcontrol.plugin.paper.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.paper.PaperCrowdControlPlugin;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import dev.qixils.crowdcontrol.socket.Response.ResultType;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.Difficulty;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Getter
@Global
public class DifficultyCommand extends ImmediateCommand {
	private final Difficulty difficulty;
	private final String effectName;
	private final Component displayName;

	private static String effectNameOf(Difficulty difficulty) {
		return "difficulty_" + difficulty.name();
	}

	public DifficultyCommand(PaperCrowdControlPlugin plugin, Difficulty difficulty) {
		super(plugin);
		this.difficulty = difficulty;
		this.effectName = effectNameOf(difficulty);
		this.displayName = Component.translatable("cc.effect.difficulty.name", Component.translatable(difficulty));
	}

	@Override
	public Response.@NotNull Builder executeImmediately(@NotNull List<@NotNull Player> players, @NotNull Request request) {
		boolean success = false;
		for (World world : plugin.getServer().getWorlds()) {
			if (world.getDifficulty() != difficulty) {
				success = true;
				world.setDifficulty(difficulty);
			}
		}

		if (success) {
			for (Difficulty dif : Difficulty.values())
				plugin.updateEffectStatus(null, effectNameOf(dif), dif == difficulty ? ResultType.NOT_SELECTABLE : ResultType.SELECTABLE);
			return request.buildResponse().type(ResultType.SUCCESS);
		} else
			return request.buildResponse().type(ResultType.FAILURE).message("World is already on that difficulty");
	}
}
