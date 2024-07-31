package dev.qixils.crowdcontrol.plugin.paper.commands;

import dev.qixils.crowdcontrol.common.packets.SetLanguagePacketS2C;
import dev.qixils.crowdcontrol.common.packets.util.LanguageState;
import dev.qixils.crowdcontrol.common.util.SemVer;
import dev.qixils.crowdcontrol.plugin.paper.PaperCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.paper.TimedImmediateCommand;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Getter
public class LanguageCommand extends TimedImmediateCommand {
	private static final @NotNull Set<UUID> ACTIVE = new HashSet<>();
	private final @NotNull String effectName = "language_random";
	private final @NotNull Duration defaultDuration = Duration.ofSeconds(30);

	public LanguageCommand(@NotNull PaperCrowdControlPlugin plugin) {
		super(plugin);
	}

	public @NotNull SemVer getMinimumModVersion() {
		return LanguageState.RANDOM.addedIn();
	}

	@NotNull
	@Override
	public Response.Builder executeImmediately(@NotNull List<@NotNull Player> players, @NotNull Request request) {
		players.removeIf(player -> ACTIVE.contains(player.getUniqueId()));
		if (players.isEmpty())
			return request.buildResponse().type(Response.ResultType.RETRY).message("All players already have an active language effect");

		// create byte buf
		Duration duration = getDuration(request);
		SetLanguagePacketS2C packet = new SetLanguagePacketS2C(LanguageState.RANDOM, duration);

		// send packet
		for (Player player : players) {
			ACTIVE.add(player.getUniqueId());
			plugin.getPluginChannel().sendMessage(player, packet);
		}

		// schedule removal
		plugin.getScheduledExecutor().schedule(
			() -> players.forEach(player -> ACTIVE.remove(player.getUniqueId())),
			duration.toMillis(),
			TimeUnit.MILLISECONDS
		);

		return request.buildResponse().type(Response.ResultType.SUCCESS).timeRemaining(duration);
	}
}
