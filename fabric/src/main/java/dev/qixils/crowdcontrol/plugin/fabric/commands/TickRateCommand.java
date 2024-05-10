package dev.qixils.crowdcontrol.plugin.fabric.commands;

import dev.qixils.crowdcontrol.TimedEffect;
import dev.qixils.crowdcontrol.common.Global;
import dev.qixils.crowdcontrol.plugin.fabric.FabricCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.fabric.TimedVoidCommand;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import net.minecraft.server.ServerTickRateManager;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.List;

@Global
@Getter
public class TickRateCommand extends TimedVoidCommand {
	private static final float RATE = 20f;
	private final Duration defaultDuration = Duration.ofSeconds(20);
	private final String effectName;
	private final float multiplier;

	private TickRateCommand(FabricCrowdControlPlugin plugin, String effectName, float multiplier) {
		super(plugin);
		this.effectName = effectName;
		this.multiplier = multiplier;
	}

	@Override
	public void voidExecute(@NotNull List<@NotNull ServerPlayer> players, @NotNull Request request) {
		new TimedEffect.Builder()
			.request(request)
			.effectGroup("tick_rate")
			.duration(request.getDuration())
			.startCallback(effect -> {
				ServerTickRateManager serverTickRateManager = plugin.server().tickRateManager();
				serverTickRateManager.setTickRate(RATE * multiplier);
				return request.buildResponse().type(Response.ResultType.SUCCESS);
			})
			.completionCallback(effect -> {
				ServerTickRateManager serverTickRateManager = plugin.server().tickRateManager();
				serverTickRateManager.setTickRate(RATE);
			})
			.build().queue();
	}

	public static TickRateCommand doubleRate(FabricCrowdControlPlugin plugin) {
		return new TickRateCommand(plugin, "tick_double", 2.0f);
	}

	public static TickRateCommand halfRate(FabricCrowdControlPlugin plugin) {
		return new TickRateCommand(plugin, "tick_halve", 0.5f);
	}
}
