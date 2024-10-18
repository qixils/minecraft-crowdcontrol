package dev.qixils.crowdcontrol.plugin.paper.commands;

import dev.qixils.crowdcontrol.common.Global;
import dev.qixils.crowdcontrol.common.util.ThreadUtil;
import dev.qixils.crowdcontrol.plugin.paper.PaperCommand;
import dev.qixils.crowdcontrol.plugin.paper.PaperCrowdControlPlugin;
import live.crowdcontrol.cc4j.CCPlayer;
import live.crowdcontrol.cc4j.CCTimedEffect;
import live.crowdcontrol.cc4j.websocket.data.CCTimedEffectResponse;
import live.crowdcontrol.cc4j.websocket.data.ResponseStatus;
import live.crowdcontrol.cc4j.websocket.payload.PublicEffectPayload;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

@Global
@Getter
public class TickFreezeCommand extends PaperCommand implements CCTimedEffect {
	private final Duration defaultDuration = Duration.ofSeconds(20);
	private final String effectName = "tick_freeze";

	private final String effectGroup = "tick_rate";
	private final List<String> effectGroups = Collections.singletonList(effectGroup);

	public TickFreezeCommand(PaperCrowdControlPlugin plugin) {
		super(plugin);
	}

	@Override
	public void execute(@NotNull Supplier<@NotNull List<@NotNull Player>> playerSupplier, @NotNull PublicEffectPayload request, @NotNull CCPlayer ccPlayer) {
		ccPlayer.sendResponse(ThreadUtil.waitForSuccess(() -> {
			onResume(request, ccPlayer);
			return new CCTimedEffectResponse(request.getRequestId(), ResponseStatus.TIMED_BEGIN, request.getEffect().getDuration() * 1000L);
		}));
	}

	@Override
	public void onPause(@NotNull PublicEffectPayload request, @NotNull CCPlayer source) {
		Bukkit.getServerTickManager().setFrozen(false);
	}

	@Override
	public void onResume(@NotNull PublicEffectPayload request, @NotNull CCPlayer source) {
		Bukkit.getServerTickManager().setFrozen(true);
	}

	@Override
	public void onEnd(@NotNull PublicEffectPayload request, @NotNull CCPlayer source) {
		onPause(request, source);
	}
}
