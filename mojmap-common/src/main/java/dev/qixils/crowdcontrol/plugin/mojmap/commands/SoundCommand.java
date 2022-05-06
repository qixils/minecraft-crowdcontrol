package dev.qixils.crowdcontrol.plugin.mojmap.commands;

import dev.qixils.crowdcontrol.common.util.sound.Sounds;
import dev.qixils.crowdcontrol.plugin.mojmap.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.mojmap.MojmapPlugin;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Getter
public class SoundCommand extends ImmediateCommand {
	private final String effectName = "sfx";
	private final String displayName = "Spooky Sound Effect";

	public SoundCommand(MojmapPlugin<?> plugin) {
		super(plugin);
	}

	@Override
	public Response.@NotNull Builder executeImmediately(@NotNull List<@NotNull ServerPlayer> players, @NotNull Request request) {
		for (ServerPlayer player : players) {
			Vec3 playAt = player.position().add(new Vec3(player.getDirection().getOpposite().step()));
			plugin.adventure().player(player).playSound(
					Sounds.SPOOKY.get(),
					playAt.x(),
					playAt.y(),
					playAt.z()
			);
		}
		return request.buildResponse().type(Response.ResultType.SUCCESS);
	}
}
