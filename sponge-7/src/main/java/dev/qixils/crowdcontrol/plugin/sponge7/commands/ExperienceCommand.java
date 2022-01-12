package dev.qixils.crowdcontrol.plugin.sponge7.commands;

import dev.qixils.crowdcontrol.plugin.sponge7.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.sponge7.SpongeCrowdControlPlugin;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import dev.qixils.crowdcontrol.socket.Response.ResultType;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;

import java.util.List;
import java.util.Optional;

@Getter
public class ExperienceCommand extends ImmediateCommand {
	private final String effectName;
	private final String displayName;
	private final int amount;

	public ExperienceCommand(SpongeCrowdControlPlugin plugin, String effectName, String displayName, int amount) {
		super(plugin);
		this.effectName = effectName;
		this.displayName = displayName;
		assert amount != 0;
		this.amount = amount;
	}

	@NotNull
	@Override
	public Response.Builder executeImmediately(@NotNull List<@NotNull Player> players, @NotNull Request request) {
		Response.Builder resp = request.buildResponse()
				.type(ResultType.FAILURE)
				.message("Player does not have enough XP levels");
		for (Player player : players) {
			Optional<Integer> optionalLevel = player.get(Keys.EXPERIENCE_LEVEL);
			if (!optionalLevel.isPresent())
				continue;
			int curLevel = optionalLevel.get();
			int newLevel = curLevel + amount;
			if (newLevel >= 0) {
				resp.type(ResultType.SUCCESS).message("SUCCESS");
				sync(() -> player.offer(Keys.EXPERIENCE_LEVEL, newLevel));
			}
		}
		return resp;
	}
}