package dev.qixils.crowdcontrol.plugin.paper.commands;

import dev.qixils.crowdcontrol.common.util.sound.Sounds;
import dev.qixils.crowdcontrol.plugin.paper.PaperCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.paper.RegionalCommandSync;
import live.crowdcontrol.cc4j.websocket.payload.PublicEffectPayload;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@Getter
public class SoundCommand extends RegionalCommandSync {
	private final String effectName = "sfx";

	public SoundCommand(PaperCrowdControlPlugin plugin) {
		super(plugin);
	}

	@Override
	protected Response.@NotNull Builder buildFailure(@NotNull Request request) {
		return request.buildResponse()
			.type(Response.ResultType.RETRY)
			.message("Unable to play sound");
	}

	@Override
	protected boolean executeRegionallySync(@NotNull Player player, @NotNull Request request) {
		Location playAt = player.getLocation().add(player.getFacing().getOppositeFace().getDirection());
		player.playSound(
			Sounds.SPOOKY.get(),
			playAt.getX(),
			playAt.getY(),
			playAt.getZ()
		);
		return true;
	}
}
