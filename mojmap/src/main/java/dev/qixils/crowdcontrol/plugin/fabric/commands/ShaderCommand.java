package dev.qixils.crowdcontrol.plugin.fabric.commands;

import dev.qixils.crowdcontrol.TriState;
import dev.qixils.crowdcontrol.common.command.impl.Shader;
import dev.qixils.crowdcontrol.common.util.ThreadUtil;
import dev.qixils.crowdcontrol.plugin.fabric.ModdedCommand;
import dev.qixils.crowdcontrol.plugin.fabric.ModdedCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.fabric.event.Join;
import dev.qixils.crowdcontrol.plugin.fabric.event.Listener;
import live.crowdcontrol.cc4j.CCPlayer;
import live.crowdcontrol.cc4j.CCTimedEffect;
import live.crowdcontrol.cc4j.IUserRecord;
import live.crowdcontrol.cc4j.websocket.data.CCInstantEffectResponse;
import live.crowdcontrol.cc4j.websocket.data.CCTimedEffectResponse;
import live.crowdcontrol.cc4j.websocket.data.ResponseStatus;
import live.crowdcontrol.cc4j.websocket.payload.PublicEffectPayload;
import lombok.Getter;
import net.kyori.adventure.platform.modcommon.MinecraftAudiences;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Supplier;

@Getter
public class ShaderCommand extends ModdedCommand implements CCTimedEffect {
	private final @NotNull String effectName;
	private final @NotNull Identifier postEffect;
	private final @NotNull Shader shader;
	private final @NotNull String effectGroup = "shaders";
	private final @NotNull List<String> effectGroups = Collections.singletonList(effectGroup);

	private final Map<UUID, List<UUID>> uuidMap = new HashMap<>();

	public ShaderCommand(@NotNull ModdedCrowdControlPlugin plugin, @NotNull Shader shader) {
		super(plugin);
		this.effectName = shader.getEffectId();
		this.shader = shader;
		this.postEffect = MinecraftAudiences.asNative(shader.getIdentifier());
	}

	/**
	 * Whether the given player's client knows about this post effect.
	 */
	private boolean isAvailableTo(@NotNull ServerPlayer player) {
		return plugin.getShaderPackManager().canRender(player, shader);
	}

	/**
	 * Can't stack on ourselves or lidar
	 */
	@Override
	public String[] getEffectArray() {
		return new String[]{effectName, "lidar"};
	}

	@Override
	public TriState isVisible(@NotNull IUserRecord user, @NotNull List<ServerPlayer> potentialPlayers) {
		if (!shader.bundled()) return TriState.UNKNOWN;
		return TriState.fromBoolean(potentialPlayers.stream().anyMatch(this::isAvailableTo));
	}

	@Override
	public void execute(@NotNull Supplier<@NotNull List<@NotNull ServerPlayer>> playerSupplier, @NotNull PublicEffectPayload request, @NotNull CCPlayer ccPlayer) {
		ccPlayer.sendResponse(ThreadUtil.waitForSuccess(request, () -> {
			List<ServerPlayer> players = new ArrayList<>();
			for (ServerPlayer player : playerSupplier.get()) {
				if (!isAvailableTo(player)) continue;
				if (player.getPostEffects().contains(postEffect)) continue;
				players.add(player);
			}
			if (players.isEmpty())
				return new CCInstantEffectResponse(request.getRequestId(), ResponseStatus.FAIL_TEMPORARY, "Target already has shader or cannot receive it");
			sync(() -> players.forEach(player -> player.addPostEffect(postEffect)));
			uuidMap.put(request.getRequestId(), players.stream().map(ServerPlayer::getUUID).toList());
			return new CCTimedEffectResponse(request.getRequestId(), ResponseStatus.TIMED_BEGIN, request.getEffect().getDurationMillis());
		}));
	}

	@Override
	public void onPause(@NotNull PublicEffectPayload request, @NotNull CCPlayer source) {
		List<ServerPlayer> players = plugin.toPlayerList(uuidMap.get(request.getRequestId()));
		sync(() -> players.forEach(player -> player.removePostEffect(postEffect)));
	}

	@Override
	public void onResume(@NotNull PublicEffectPayload request, @NotNull CCPlayer source) {
		List<ServerPlayer> players = plugin.toPlayerList(uuidMap.get(request.getRequestId()));
		sync(() -> players.forEach(player -> player.addPostEffect(postEffect)));
	}

	@Override
	public void onEnd(@NotNull PublicEffectPayload request, @NotNull CCPlayer source) {
		List<ServerPlayer> players = plugin.toPlayerList(uuidMap.remove(request.getRequestId()));
		sync(() -> players.forEach(player -> player.removePostEffect(postEffect)));
	}

	@Listener
	public void onJoin(Join event) {
		event.player().clearPostEffects();
	}
}
