package dev.qixils.crowdcontrol.plugin.paper.commands;

import dev.qixils.crowdcontrol.common.Global;
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
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

@Global
@Getter
public class TickRateCommand extends PaperCommand implements CCTimedEffect {
	private static final float RATE = 20f;
	private final String effectName;
	private final float multiplier;

	private final String effectGroup = "tick_rate";
	private final List<String> effectGroups = Collections.singletonList(effectGroup);

	private TickRateCommand(PaperCrowdControlPlugin plugin, String effectName, float multiplier) {
		super(plugin);
		this.effectName = effectName;
		this.multiplier = multiplier;
	}

	@Override
	public void execute(@NotNull Supplier<@NotNull List<@NotNull Player>> playerSupplier, @NotNull PublicEffectPayload request, @NotNull CCPlayer ccPlayer) {
		ccPlayer.sendResponse(ThreadUtil.waitForSuccess(() -> {
			if (isArrayActive(ccPlayer))
				return new CCInstantEffectResponse(request.getRequestId(), ResponseStatus.FAIL_TEMPORARY, "Conflicting effects active");
			onResume(request, ccPlayer);
			return new CCTimedEffectResponse(request.getRequestId(), ResponseStatus.TIMED_BEGIN, request.getEffect().getDuration() * 1000L);
		}));
	}

	private void set(float value) {
		Bukkit.getServerTickManager().setTickRate(value);
	}

	@Override
	public void onPause(@NotNull PublicEffectPayload request, @NotNull CCPlayer source) {
		set(RATE);
	}

	@Override
	public void onResume(@NotNull PublicEffectPayload request, @NotNull CCPlayer source) {
		set(RATE * multiplier);
	}

	@Override
	public void onEnd(@NotNull PublicEffectPayload request, @NotNull CCPlayer source) {
		onPause(request, source);
	}

	public static TickRateCommand doubleRate(PaperCrowdControlPlugin plugin) {
		return new TickRateCommand(plugin, "tick_double", 2.0f);
	}

	public static TickRateCommand halfRate(PaperCrowdControlPlugin plugin) {
		return new TickRateCommand(plugin, "tick_halve", 0.5f);
	}
}
