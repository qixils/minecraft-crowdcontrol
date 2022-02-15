package dev.qixils.crowdcontrol.plugin.sponge8.commands;

import dev.qixils.crowdcontrol.common.Global;
import dev.qixils.crowdcontrol.plugin.sponge8.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.sponge8.SpongeCrowdControlPlugin;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import dev.qixils.crowdcontrol.socket.Response.Builder;
import dev.qixils.crowdcontrol.socket.Response.ResultType;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.world.difficulty.Difficulty;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.api.world.server.storage.ServerWorldProperties;

import java.util.List;

@Getter
@Global
public class DifficultyCommand extends ImmediateCommand {
	private final Difficulty difficulty;
	private final String effectName;
	private final String displayName;

	public DifficultyCommand(SpongeCrowdControlPlugin plugin, Difficulty difficulty) {
		super(plugin);
		this.difficulty = difficulty;
		this.effectName = "difficulty_" + difficulty.key(RegistryTypes.DIFFICULTY).value();
		this.displayName = plugin.getTextUtil().asPlain(difficulty) + " Mode";
	}

	@NotNull
	@Override
	public Response.Builder executeImmediately(@NotNull List<@NotNull ServerPlayer> players, @NotNull Request request) {
		Builder response = request.buildResponse().type(ResultType.FAILURE)
				.message("Server difficulty is already on " + displayName);

		for (ServerWorld world : plugin.getGame().server().worldManager().worlds()) {
			ServerWorldProperties properties = world.properties();
			if (!properties.difficulty().equals(difficulty)) {
				response.type(ResultType.SUCCESS).message("SUCCESS");
				properties.setDifficulty(difficulty);
			}
		}

		return response;
	}
}
