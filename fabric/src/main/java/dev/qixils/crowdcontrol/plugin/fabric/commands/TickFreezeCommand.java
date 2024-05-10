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
public class TickFreezeCommand extends TimedVoidCommand {
	private final Duration defaultDuration = Duration.ofSeconds(20);
	private final String effectName = "tick_freeze";

	public TickFreezeCommand(FabricCrowdControlPlugin plugin) {
		super(plugin);
	}

	@Override
	public void voidExecute(@NotNull List<@NotNull ServerPlayer> players, @NotNull Request request) {
		new TimedEffect.Builder()
			.request(request)
			.effectGroup("tick_rate")
			.duration(request.getDuration())
			.startCallback(effect -> {
				ServerTickRateManager serverTickRateManager = plugin.server().tickRateManager();
				serverTickRateManager.setFrozen(true);
				return request.buildResponse().type(Response.ResultType.SUCCESS);
			})
			.completionCallback(effect -> {
				ServerTickRateManager serverTickRateManager = plugin.server().tickRateManager();
				serverTickRateManager.setFrozen(false);
			})
			.build().queue();
	}
}
