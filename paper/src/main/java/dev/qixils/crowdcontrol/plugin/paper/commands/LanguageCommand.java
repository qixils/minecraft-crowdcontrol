package dev.qixils.crowdcontrol.plugin.paper.commands;

import dev.qixils.crowdcontrol.common.packets.SetLanguagePacketS2C;
import dev.qixils.crowdcontrol.common.packets.util.ExtraFeature;
import dev.qixils.crowdcontrol.common.packets.util.LanguageState;
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
import lombok.experimental.Accessors;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Getter
public class LanguageCommand extends PaperCommand implements CCTimedEffect {
	private static final @NotNull Set<UUID> ACTIVE = new HashSet<>();
	private final @NotNull String effectName = "language_random";
	private final @NotNull Duration defaultDuration = Duration.ofSeconds(30);
	private final @NotNull SemVer minimumModVersion = LanguageState.RANDOM.addedIn();
	@Accessors(fluent = true)
	private final @NotNull Set<ExtraFeature> requiredExtraFeatures = EnumSet.of(ExtraFeature.LANGUAGE_RELOAD);

	public LanguageCommand(@NotNull PaperCrowdControlPlugin plugin) {
		super(plugin);
	}

	@Override
	public void execute(@NotNull Supplier<@NotNull List<@NotNull Player>> playerSupplier, @NotNull PublicEffectPayload request, @NotNull CCPlayer ccPlayer) {
		ccPlayer.sendResponse(ThreadUtil.waitForSuccess(() -> {
			List<Player> players = playerSupplier.get();
			players.removeIf(player -> ACTIVE.contains(player.getUniqueId()));
			if (players.isEmpty())
				return new CCInstantEffectResponse(request.getRequestId(), ResponseStatus.FAIL_TEMPORARY, "All players already have an active language effect");

			// create byte buf
			Duration duration = Duration.ofSeconds(request.getEffect().getDuration());
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

			return new CCTimedEffectResponse(request.getRequestId(), ResponseStatus.TIMED_BEGIN, duration.toMillis());
		}));
	}
}
