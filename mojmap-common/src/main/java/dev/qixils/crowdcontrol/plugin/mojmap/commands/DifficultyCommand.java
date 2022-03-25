package dev.qixils.crowdcontrol.plugin.mojmap.commands;

import dev.qixils.crowdcontrol.common.Global;
import dev.qixils.crowdcontrol.plugin.mojmap.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.mojmap.MojmapPlugin;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import dev.qixils.crowdcontrol.socket.Response.Builder;
import dev.qixils.crowdcontrol.socket.Response.ResultType;
import lombok.Getter;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Getter
@Global
public class DifficultyCommand extends ImmediateCommand {
	private final Difficulty difficulty;
	private final String effectName;
	private final String displayName;

	public DifficultyCommand(MojmapPlugin plugin, Difficulty difficulty) {
		super(plugin);
		this.difficulty = difficulty;
		this.effectName = "difficulty_" + difficulty.getKey();
		this.displayName = plugin.getTextUtil().asPlain(difficulty.getDisplayName()) + " Mode";
	}

	@NotNull
	@Override
	public Response.Builder executeImmediately(@NotNull List<@NotNull ServerPlayer> players, @NotNull Request request) {
		Builder response = request.buildResponse()
				.type(ResultType.FAILURE)
				.message("Server difficulty is locked or already set to " + displayName);

		if (plugin.server().getWorldData().isDifficultyLocked())
			return response;
		if (plugin.server().getWorldData().getDifficulty() == difficulty)
			return response;

		sync(() -> plugin.server().setDifficulty(difficulty, true));
		return response.type(ResultType.SUCCESS).message("SUCCESS");
	}
}
