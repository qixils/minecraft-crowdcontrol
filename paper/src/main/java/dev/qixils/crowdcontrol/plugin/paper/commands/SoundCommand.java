package dev.qixils.crowdcontrol.plugin.paper.commands;

import dev.qixils.crowdcontrol.common.util.sound.Sounds;
import dev.qixils.crowdcontrol.plugin.paper.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.paper.PaperCrowdControlPlugin;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Getter
public class SoundCommand extends ImmediateCommand {
	private final String effectName = "sfx";

	public SoundCommand(PaperCrowdControlPlugin plugin) {
		super(plugin);
	}

	@Override
	public Response.@NotNull Builder executeImmediately(@NotNull List<@NotNull Player> players, @NotNull Request request) {
		for (Player player : players) {
			Location playAt = player.getLocation().add(player.getFacing().getOppositeFace().getDirection());
			player.playSound(
					Sounds.SPOOKY.get(),
					playAt.getX(),
					playAt.getY(),
					playAt.getZ()
			);
		}
		return request.buildResponse().type(Response.ResultType.SUCCESS);
	}
}
