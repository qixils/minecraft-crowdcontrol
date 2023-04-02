package dev.qixils.crowdcontrol.plugin.fabric.commands;

import dev.qixils.crowdcontrol.common.util.SemVer;
import dev.qixils.crowdcontrol.plugin.fabric.FabricCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.fabric.TimedImmediateCommand;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
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

	public ShaderCommand(@NotNull FabricCrowdControlPlugin plugin, @NotNull String shader, @NotNull SemVer minimumModVersion) {
		super(plugin);
		this.effectName = "shader_" + shader.replaceFirst("^cc_", "");
		this.minimumModVersion = minimumModVersion;
		this.shader = shader;
	}

	@NotNull
	@Override
	public Response.Builder executeImmediately(@NotNull List<@NotNull ServerPlayerEntity> players, @NotNull Request request) {
		players.removeIf(player -> ACTIVE_SHADERS.contains(player.getUuid()));
		if (players.isEmpty())
			return request.buildResponse().type(Response.ResultType.RETRY).message("All players already have an active screen effect");
		// create byte buf
		PacketByteBuf buf = PacketByteBufs.create();
		buf.writeString(shader, 64);
		long duration = getDuration(request).toMillis();
		buf.writeLong(duration);
		// send packet
		players.forEach(player -> {
			ACTIVE_SHADERS.add(player.getUuid());
			ServerPlayNetworking.send(player, FabricCrowdControlPlugin.SHADER_ID, buf);
		});
		// schedule removal
		plugin.getScheduledExecutor().schedule(
				() -> players.forEach(player -> ACTIVE_SHADERS.remove(player.getUuid())), duration, TimeUnit.MILLISECONDS);
		return request.buildResponse().type(Response.ResultType.SUCCESS);
	}
}
