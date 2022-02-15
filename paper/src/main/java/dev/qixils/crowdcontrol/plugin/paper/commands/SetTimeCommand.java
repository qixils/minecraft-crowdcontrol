package dev.qixils.crowdcontrol.plugin.paper.commands;

import dev.qixils.crowdcontrol.common.Global;
import dev.qixils.crowdcontrol.plugin.paper.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.paper.PaperCrowdControlPlugin;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import dev.qixils.crowdcontrol.socket.Response.ResultType;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Getter
@Global
public class SetTimeCommand extends ImmediateCommand {
	private final @NotNull String displayName;
	private final @NotNull String effectName;
	private final long time;

	public SetTimeCommand(PaperCrowdControlPlugin plugin, String displayName, String effectName, long time) {
		super(plugin);
		this.displayName = displayName;
		this.effectName = effectName;
		this.time = time;
	}

	@NotNull
	@Override
	public Response.Builder executeImmediately(@NotNull List<@NotNull Player> players, @NotNull Request request) {
		sync(() -> {
			for (World world : Bukkit.getWorlds())
				world.setTime(time);
		});
		return request.buildResponse().type(ResultType.SUCCESS);
	}
}
