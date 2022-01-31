package dev.qixils.crowdcontrol.plugin.sponge8.commands;

import dev.qixils.crowdcontrol.plugin.sponge8.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.sponge8.SpongeCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.sponge8.utils.Sponge7TextUtil;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import dev.qixils.crowdcontrol.socket.Response.Builder;
import dev.qixils.crowdcontrol.socket.Response.ResultType;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.difficulty.Difficulty;
import org.spongepowered.api.world.storage.WorldProperties;

import java.util.List;

@Getter
public class DifficultyCommand extends ImmediateCommand {
	private final Difficulty difficulty;
	private final String effectName;
	private final String displayName;

	public DifficultyCommand(SpongeCrowdControlPlugin plugin, Difficulty difficulty) {
		super(plugin);
		this.difficulty = difficulty;
		this.effectName = "difficulty_" + Sponge7TextUtil.valueOf(difficulty);
		this.displayName = difficulty.getTranslation().get() + " Mode";
	}

	@NotNull
	@Override
	public Response.Builder executeImmediately(@NotNull List<@NotNull Player> players, @NotNull Request request) {
		if (!isGlobalCommandUsable(players, request))
			return request.buildResponse().type(ResultType.UNAVAILABLE).message("Global command cannot be used on this streamer");

		Builder response = request.buildResponse().type(ResultType.FAILURE)
				.message("Server difficulty is already on " + displayName);

		for (World world : plugin.getGame().getServer().getWorlds()) {
			WorldProperties properties = world.getProperties();
			if (!properties.getDifficulty().equals(difficulty)) {
				response.type(ResultType.SUCCESS).message("SUCCESS");
				properties.setDifficulty(difficulty);
			}
		}

		return response;
	}
}
