package dev.qixils.crowdcontrol.plugin.fabric.commands;

import dev.qixils.crowdcontrol.common.command.impl.Shader;
import dev.qixils.crowdcontrol.common.util.SemVer;
import dev.qixils.crowdcontrol.plugin.fabric.FabricCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.fabric.TimedImmediateCommand;
import dev.qixils.crowdcontrol.plugin.fabric.packets.SetShaderS2C;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;
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

	public ShaderCommand(@NotNull FabricCrowdControlPlugin plugin, @NotNull Shader shader) {
		super(plugin);
		this.effectName = shader.getEffectId();
		this.minimumModVersion = shader.addedIn();
		this.shader = shader.getShaderId();
	}

	@NotNull
	@Override
	public Response.Builder executeImmediately(@NotNull List<@NotNull ServerPlayer> players, @NotNull Request request) {
		players.removeIf(player -> ACTIVE_SHADERS.contains(player.getUUID()));
		if (players.isEmpty())
			return request.buildResponse().type(Response.ResultType.RETRY).message("All players already have an active screen effect");

		// create byte buf
		Duration duration = getDuration(request);
		SetShaderS2C packet = new SetShaderS2C(shader, duration);

		// send packet
		for (ServerPlayer player : players) {
			ACTIVE_SHADERS.add(player.getUUID());
			ServerPlayNetworking.send(player, packet);
		}

		// schedule removal
		plugin.getScheduledExecutor().schedule(
				() -> players.forEach(player -> ACTIVE_SHADERS.remove(player.getUUID())), duration.toMillis(), TimeUnit.MILLISECONDS);
		return request.buildResponse().type(Response.ResultType.SUCCESS).timeRemaining(duration);
	}
}
