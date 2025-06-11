package dev.qixils.crowdcontrol.plugin.paper.commands;

import dev.qixils.crowdcontrol.common.Global;
import dev.qixils.crowdcontrol.common.util.ThreadUtil;
import dev.qixils.crowdcontrol.plugin.paper.PaperCommand;
import dev.qixils.crowdcontrol.plugin.paper.PaperCrowdControlPlugin;
import live.crowdcontrol.cc4j.CCPlayer;
import live.crowdcontrol.cc4j.websocket.data.CCInstantEffectResponse;
import live.crowdcontrol.cc4j.websocket.data.ResponseStatus;
import live.crowdcontrol.cc4j.websocket.payload.PublicEffectPayload;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Supplier;

import static dev.qixils.crowdcontrol.common.command.CommandConstants.WEATHER_TICKS;

@Global
public abstract class AbstractWeatherCommand extends PaperCommand {
	protected static final int WEATHER_DURATION = (int) WEATHER_TICKS;

	public AbstractWeatherCommand(PaperCrowdControlPlugin plugin) {
		super(plugin);
	}

	protected abstract void applyWeather(World world);

	protected abstract boolean isWeatherActive(World world);

	@Override
	public void execute(@NotNull Supplier<@NotNull List<@NotNull Player>> playerSupplier, @NotNull PublicEffectPayload request, @NotNull CCPlayer ccPlayer) {
		ccPlayer.sendResponse(ThreadUtil.waitForSuccess(request, () -> {
			playerSupplier.get(); // validate now is ok to start
			boolean success = false;
			for (World world : Bukkit.getWorlds()) {
				if (world.getEnvironment() != World.Environment.NORMAL)
					continue;
				if (isWeatherActive(world))
					continue;
				applyWeather(world);
				success = true;
			}
			return success
				? new CCInstantEffectResponse(request.getRequestId(), ResponseStatus.SUCCESS)
				: new CCInstantEffectResponse(request.getRequestId(), ResponseStatus.FAIL_TEMPORARY, "This weather is already applied");
		}, plugin.getSyncExecutor()));
	}
}
