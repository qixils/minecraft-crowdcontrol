package dev.qixils.crowdcontrol.plugin.fabric.commands;

import dev.qixils.crowdcontrol.common.util.SemVer;
import dev.qixils.crowdcontrol.plugin.fabric.FabricCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.fabric.TimedImmediateCommand;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Getter
public class ShaderCommand extends TimedImmediateCommand {
	private static final @NotNull Set<UUID> ACTIVE_SHADERS = new HashSet<>();
	private final @NotNull String effectName;
	private final @NotNull String shader;
	private final @NotNull SemVer minimumModVersion;
	private final @NotNull Duration defaultDuration = Duration.ofSeconds(30);

	public ShaderCommand(@NotNull FabricCrowdControlPlugin plugin, @NotNull String shader, @NotNull SemVer minimumModVersion) {
		super(plugin);
		this.effectName = "shader_" + shader.replaceFirst("^cc_", "");
		this.minimumModVersion = minimumModVersion;
		this.shader = shader;
	}

	@NotNull
	@Override
	public Response.Builder executeImmediately(@NotNull List<@NotNull ServerPlayer> players, @NotNull Request request) {
		players.removeIf(player -> ACTIVE_SHADERS.contains(player.getUUID()));
		if (players.isEmpty())
			return request.buildResponse().type(Response.ResultType.RETRY).message("All players already have an active screen effect");
		// create byte buf
		FriendlyByteBuf buf = PacketByteBufs.create();
		buf.writeUtf(shader, 64);
		Duration duration = getDuration(request);
		buf.writeLong(duration.toMillis());
		// send packet
		players.forEach(player -> {
			ACTIVE_SHADERS.add(player.getUUID());
			ServerPlayNetworking.send(player, FabricCrowdControlPlugin.SHADER_ID, buf);
		});
		// schedule removal
		plugin.getAsyncExecutor().runLater(duration, () -> players.forEach(player -> ACTIVE_SHADERS.remove(player.getUUID())));
		return request.buildResponse().type(Response.ResultType.SUCCESS);
	}
}
