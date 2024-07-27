package dev.qixils.crowdcontrol.plugin.paper.commands;

import dev.qixils.crowdcontrol.common.command.impl.Shader;
import dev.qixils.crowdcontrol.common.packets.ShaderPacketS2C;
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
public class ShaderCommand extends TimedImmediateCommand {
	private static final @NotNull Set<UUID> ACTIVE_SHADERS = new HashSet<>();
	private final @NotNull String effectName;
	private final @NotNull String shader;
	private final @NotNull SemVer minimumModVersion;
	private final @NotNull Duration defaultDuration = Duration.ofSeconds(30);

	public ShaderCommand(@NotNull PaperCrowdControlPlugin plugin, @NotNull Shader shader) {
		super(plugin);
		this.effectName = shader.getEffectId();
		this.minimumModVersion = shader.addedIn();
		this.shader = shader.getShaderId();
	}

	@NotNull
	@Override
	public Response.Builder executeImmediately(@NotNull List<@NotNull Player> players, @NotNull Request request) {
		players.removeIf(player -> ACTIVE_SHADERS.contains(player.getUniqueId()));
		if (players.isEmpty())
			return request.buildResponse().type(Response.ResultType.RETRY).message("All players already have an active screen effect");

		// create byte buf
		Duration duration = getDuration(request);
		ShaderPacketS2C packet = new ShaderPacketS2C(shader, duration);

		// send packet
		for (Player player : players) {
			ACTIVE_SHADERS.add(player.getUniqueId());
			plugin.getPluginChannel().sendMessage(player, packet);
		}

		// schedule removal
		plugin.getScheduledExecutor().schedule(
			() -> players.forEach(player -> ACTIVE_SHADERS.remove(player.getUniqueId())), duration.toMillis(), TimeUnit.MILLISECONDS);

		return request.buildResponse().type(Response.ResultType.SUCCESS).timeRemaining(duration);
	}
}
