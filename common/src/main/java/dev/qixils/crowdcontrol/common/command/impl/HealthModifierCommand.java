package dev.qixils.crowdcontrol.common.command.impl;

import dev.qixils.crowdcontrol.common.Plugin;
import dev.qixils.crowdcontrol.common.command.Command;
import dev.qixils.crowdcontrol.common.util.ThreadUtil;
import live.crowdcontrol.cc4j.CCPlayer;
import live.crowdcontrol.cc4j.CCTimedEffect;
import live.crowdcontrol.cc4j.websocket.data.CCTimedEffectResponse;
import live.crowdcontrol.cc4j.websocket.data.ResponseStatus;
import live.crowdcontrol.cc4j.websocket.payload.PublicEffectPayload;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Getter
public class HealthModifierCommand<P> implements Command<P>, CCTimedEffect {
	public static final @NotNull Map<UUID, Modifier> ACTIVE_MODIFIERS = new HashMap<>();
	private final @NotNull Duration defaultDuration = Duration.ofSeconds(15);
	private final @NotNull String effectGroup = "health_modifiers";
	private final @NotNull List<String> effectGroups = Collections.singletonList(effectGroup);
	private final @NotNull Modifier type;
	private final @NotNull Plugin<P, ?> plugin;
	private final @NotNull String effectName;
	private final @NotNull Map<UUID, Set<UUID>> playerMap = new HashMap<>();

	public HealthModifierCommand(@NotNull Plugin<P, ?> plugin, @NotNull Modifier type) {
		this.plugin = plugin;
		this.type = type;
		this.effectName = type.name().toLowerCase(Locale.US);
	}

	@Override
	public void execute(@NotNull Supplier<@NotNull List<@NotNull P>> playerSupplier, @NotNull PublicEffectPayload request, @NotNull CCPlayer ccPlayer) {
		ccPlayer.sendResponse(ThreadUtil.waitForSuccess(request, () -> {
			List<P> players = playerSupplier.get();
			playerMap.put(request.getRequestId(), players.stream().map(p -> plugin.playerMapper().getUniqueId(p)).collect(Collectors.toSet()));
			for (P player : players)
				ACTIVE_MODIFIERS.put(plugin.playerMapper().getUniqueId(player), type);
			return new CCTimedEffectResponse(
				request.getRequestId(),
				ResponseStatus.TIMED_BEGIN,
				request.getEffect().getDurationMillis()
			);
		}));
	}

	@Override
	public void onEnd(@NotNull PublicEffectPayload request, @NotNull CCPlayer source) {
		Set<UUID> players = playerMap.remove(request.getRequestId());
		if (players == null) return;
		for (UUID player : players)
			ACTIVE_MODIFIERS.remove(player, type);
	}

	public enum Modifier {
		INVINCIBLE,
		OHKO
	}
}
