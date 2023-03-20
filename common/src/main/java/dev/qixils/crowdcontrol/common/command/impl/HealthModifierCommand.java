package dev.qixils.crowdcontrol.common.command.impl;

import dev.qixils.crowdcontrol.TimedEffect;
import dev.qixils.crowdcontrol.common.Plugin;
import dev.qixils.crowdcontrol.common.command.TimedCommand;
import dev.qixils.crowdcontrol.common.command.VoidCommand;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.*;

@Getter
public class HealthModifierCommand<P> implements TimedCommand<P>, VoidCommand<P> {
	public static final @NotNull Map<UUID, Modifier> ACTIVE_MODIFIERS = new HashMap<>();
	private final @NotNull Duration defaultDuration = Duration.ofSeconds(15);
	private final @NotNull Modifier type;
	private final @NotNull Plugin<P, ?> plugin;
	private final @NotNull String effectName;

	public HealthModifierCommand(@NotNull Plugin<P, ?> plugin, @NotNull Modifier type) {
		this.plugin = plugin;
		this.type = type;
		this.effectName = type.name().toLowerCase(Locale.US);
	}

	@Override
	public void voidExecute(@NotNull List<@NotNull P> ignored, @NotNull Request request) {
		List<P> players = new ArrayList<>();
		new TimedEffect.Builder()
				.request(request)
				.effectGroup("health_modifiers")
				.duration(getDuration(request))
				.startCallback($ -> {
					players.addAll(plugin.getPlayers(request));
					if (players.isEmpty()) return request.buildResponse().type(Response.ResultType.FAILURE).message("No players found");
					for (P player : players)
						ACTIVE_MODIFIERS.put(plugin.playerMapper().getUniqueId(player), type);
					playerAnnounce(players, request);
					return null;
				})
				.completionCallback($ -> {
					for (P player : players)
						ACTIVE_MODIFIERS.remove(plugin.playerMapper().getUniqueId(player), type);
				})
				.build().queue();
	}

	public enum Modifier {
		INVINCIBLE,
		OHKO
	}
}
