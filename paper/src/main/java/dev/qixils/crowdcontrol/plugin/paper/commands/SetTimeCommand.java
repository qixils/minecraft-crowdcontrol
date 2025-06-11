package dev.qixils.crowdcontrol.plugin.paper.commands;

import dev.qixils.crowdcontrol.common.Global;
import dev.qixils.crowdcontrol.common.util.ThreadUtil;
import dev.qixils.crowdcontrol.plugin.paper.PaperCommand;
import dev.qixils.crowdcontrol.plugin.paper.PaperCrowdControlPlugin;
import live.crowdcontrol.cc4j.CCPlayer;
import live.crowdcontrol.cc4j.websocket.data.CCInstantEffectResponse;
import live.crowdcontrol.cc4j.websocket.data.ResponseStatus;
import live.crowdcontrol.cc4j.websocket.payload.PublicEffectPayload;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Supplier;

@Getter
@Global
public class SetTimeCommand extends PaperCommand {
	private final @NotNull String effectName;
	private final long time;

	public SetTimeCommand(PaperCrowdControlPlugin plugin, String effectName, long time) {
		super(plugin);
		this.effectName = effectName;
		this.time = time;
	}

	@Override
	public void execute(@NotNull Supplier<@NotNull List<@NotNull Player>> playerSupplier, @NotNull PublicEffectPayload request, @NotNull CCPlayer ccPlayer) {
		ccPlayer.sendResponse(ThreadUtil.waitForSuccess(request, () -> {
			playerSupplier.get(); // validate now is ok to start
			for (World world : Bukkit.getWorlds())
				world.setTime(time);
			return new CCInstantEffectResponse(request.getRequestId(), ResponseStatus.SUCCESS);
		}, plugin.getSyncExecutor()));
	}
}
