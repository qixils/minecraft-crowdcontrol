package dev.qixils.crowdcontrol.plugin.commands;

import dev.qixils.crowdcontrol.common.CommandConstants;
import dev.qixils.crowdcontrol.common.util.CommonTags;
import dev.qixils.crowdcontrol.common.util.RandomUtil;
import dev.qixils.crowdcontrol.plugin.BukkitCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.ImmediateCommand;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import net.kyori.adventure.key.Key;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Getter
public class SoundCommand extends ImmediateCommand {
	private final String effectName = "sfx";
	private final String displayName = "Spooky Sound Effect";

	public SoundCommand(BukkitCrowdControlPlugin plugin) {
		super(plugin);
	}

	@Override
	public Response.@NotNull Builder executeImmediately(@NotNull List<@NotNull Player> players, @NotNull Request request) {
		Key sound = RandomUtil.randomElementFrom(CommonTags.SPOOKY_SOUNDS.getKeys());
		for (Player player : players) {
			Location playAt = player.getLocation().add(player.getFacing().getOppositeFace().getDirection());
			player.playSound(
					CommandConstants.spookySoundOf(sound),
					playAt.getX(),
					playAt.getY(),
					playAt.getZ()
			);
		}
		return request.buildResponse().type(Response.ResultType.SUCCESS);
	}
}
