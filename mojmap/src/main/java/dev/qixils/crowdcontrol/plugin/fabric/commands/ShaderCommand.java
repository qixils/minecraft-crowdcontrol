package dev.qixils.crowdcontrol.plugin.fabric.commands;

import dev.qixils.crowdcontrol.common.command.impl.Shader;
import dev.qixils.crowdcontrol.common.util.SemVer;
import dev.qixils.crowdcontrol.common.util.ThreadUtil;
import dev.qixils.crowdcontrol.plugin.fabric.ModdedCommand;
import dev.qixils.crowdcontrol.plugin.fabric.ModdedCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.fabric.event.Join;
import dev.qixils.crowdcontrol.plugin.fabric.event.Listener;
import live.crowdcontrol.cc4j.CCPlayer;
import live.crowdcontrol.cc4j.CCTimedEffect;
import live.crowdcontrol.cc4j.websocket.data.CCInstantEffectResponse;
import live.crowdcontrol.cc4j.websocket.data.CCTimedEffectResponse;
import live.crowdcontrol.cc4j.websocket.data.ResponseStatus;
import live.crowdcontrol.cc4j.websocket.payload.PublicEffectPayload;
import lombok.Getter;
import net.kyori.adventure.platform.modcommon.MinecraftAudiences;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Supplier;

@Getter
public class ShaderCommand extends ModdedCommand implements CCTimedEffect {
	private final @NotNull String effectName;
	private final @NotNull Identifier shader;
	private final @Nullable SemVer minimumModVersion;
	private final @NotNull String effectGroup = "shaders";
	private final @NotNull List<String> effectGroups = Collections.singletonList(effectGroup);

	private final Map<UUID, List<UUID>> uuidMap = new HashMap<>();

	public ShaderCommand(@NotNull ModdedCrowdControlPlugin plugin, @NotNull Shader shader) {
		super(plugin);
		this.effectName = shader.getEffectId();
		this.minimumModVersion = shader.addedIn();
		this.shader = MinecraftAudiences.asNative(shader.getIdentifier());
	}

	@Override
	public void execute(@NotNull Supplier<@NotNull List<@NotNull ServerPlayer>> playerSupplier, @NotNull PublicEffectPayload request, @NotNull CCPlayer ccPlayer) {
		ccPlayer.sendResponse(ThreadUtil.waitForSuccess(request, () -> {
			List<ServerPlayer> players = playerSupplier.get();
			boolean success = false;
			for (ServerPlayer player : players) {
				if (player.getPostEffects().contains(shader))
					continue;
				success = true;
				sync(() -> player.addPostEffect(shader));
			}
			if (!success)
				return new CCInstantEffectResponse(request.getRequestId(), ResponseStatus.FAIL_TEMPORARY, "Target already has shader or cannot receive it");
			uuidMap.put(request.getRequestId(), players.stream().map(ServerPlayer::getUUID).toList());
			return new CCTimedEffectResponse(request.getRequestId(), ResponseStatus.TIMED_BEGIN, request.getEffect().getDurationMillis());
		}));
	}

	@Override
	public void onPause(@NotNull PublicEffectPayload request, @NotNull CCPlayer source) {
		List<ServerPlayer> players = plugin.toPlayerList(uuidMap.get(request.getRequestId()));
		sync(() -> players.forEach(player -> player.removePostEffect(shader)));
	}

	@Override
	public void onResume(@NotNull PublicEffectPayload request, @NotNull CCPlayer source) {
		List<ServerPlayer> players = plugin.toPlayerList(uuidMap.get(request.getRequestId()));
		sync(() -> players.forEach(player -> player.addPostEffect(shader)));
	}

	@Override
	public void onEnd(@NotNull PublicEffectPayload request, @NotNull CCPlayer source) {
		List<ServerPlayer> players = plugin.toPlayerList(uuidMap.remove(request.getRequestId()));
		sync(() -> players.forEach(player -> player.removePostEffect(shader)));
	}

	@Listener
	public void onJoin(Join event) {
		event.player().clearPostEffects();
	}
}
