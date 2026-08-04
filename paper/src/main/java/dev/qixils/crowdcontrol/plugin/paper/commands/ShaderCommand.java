package dev.qixils.crowdcontrol.plugin.paper.commands;

import dev.qixils.crowdcontrol.common.command.impl.Shader;
import dev.qixils.crowdcontrol.common.util.SemVer;
import dev.qixils.crowdcontrol.common.util.ThreadUtil;
import dev.qixils.crowdcontrol.plugin.paper.PaperCommand;
import dev.qixils.crowdcontrol.plugin.paper.PaperCrowdControlPlugin;
import live.crowdcontrol.cc4j.CCPlayer;
import live.crowdcontrol.cc4j.CCTimedEffect;
import live.crowdcontrol.cc4j.websocket.data.CCInstantEffectResponse;
import live.crowdcontrol.cc4j.websocket.data.CCTimedEffectResponse;
import live.crowdcontrol.cc4j.websocket.data.ResponseStatus;
import live.crowdcontrol.cc4j.websocket.payload.PublicEffectPayload;
import lombok.Getter;
import net.kyori.adventure.key.Key;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.*;
import java.util.function.Supplier;

@Getter
public class ShaderCommand extends PaperCommand implements CCTimedEffect {
	private final @NotNull String effectName;
	private final @NotNull Key shader;
	private final @Nullable SemVer minimumModVersion;
	private final @NotNull String effectGroup = "shaders";
	private final @NotNull List<String> effectGroups = Collections.singletonList(effectGroup);

	private final Map<UUID, List<UUID>> uuidMap = new HashMap<>();

	public ShaderCommand(@NotNull PaperCrowdControlPlugin plugin, @NotNull Shader shader) {
		super(plugin);
		this.effectName = shader.getEffectId();
		this.minimumModVersion = shader.addedIn();
		this.shader = shader.getIdentifier();
	}

	@Override
	public void execute(@NotNull Supplier<@NotNull List<@NotNull Player>> playerSupplier, @NotNull PublicEffectPayload request, @NotNull CCPlayer ccPlayer) {
		ccPlayer.sendResponse(ThreadUtil.waitForSuccess(request, () -> {
			List<Player> players = playerSupplier.get();
			boolean success = false;
			for (Player player : players) {
				if (player.getPostEffects().contains(shader))
					continue;
				success = true;
				sync(() -> player.addPostEffect(shader));
			}
			if (!success)
				return new CCInstantEffectResponse(request.getRequestId(), ResponseStatus.FAIL_TEMPORARY, "Target already has shader or cannot receive it");
			uuidMap.put(request.getRequestId(), players.stream().map(Player::getUniqueId).toList());
			return new CCTimedEffectResponse(request.getRequestId(), ResponseStatus.TIMED_BEGIN, request.getEffect().getDurationMillis());
		}));
	}

	@Override
	public void onPause(@NotNull PublicEffectPayload request, @NotNull CCPlayer source) {
		List<UUID> players = uuidMap.get(request.getRequestId());
		if (players == null) return;
		players.stream().map(Bukkit::getPlayer).filter(Objects::nonNull).forEach(player -> player.removePostEffect(shader));
	}

	@Override
	public void onResume(@NotNull PublicEffectPayload request, @NotNull CCPlayer source) {
		List<UUID> players = uuidMap.get(request.getRequestId());
		if (players == null) return;
		players.stream().map(Bukkit::getPlayer).filter(Objects::nonNull).forEach(player -> player.addPostEffect(shader));
	}

	@Override
	public void onEnd(@NotNull PublicEffectPayload request, @NotNull CCPlayer source) {
		List<UUID> players = uuidMap.get(request.getRequestId());
		if (players == null) return;
		players.stream().map(Bukkit::getPlayer).filter(Objects::nonNull).forEach(player -> player.removePostEffect(shader));
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		event.getPlayer().clearPostEffects();
	}
}
