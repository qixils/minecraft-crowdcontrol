package dev.qixils.crowdcontrol.plugin.fabric.commands;

import dev.qixils.crowdcontrol.common.util.sound.Sounds;
import dev.qixils.crowdcontrol.plugin.fabric.FabricCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.fabric.ImmediateCommand;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Getter
public class SoundCommand extends ImmediateCommand {
	private final String effectName = "sfx";

	public SoundCommand(FabricCrowdControlPlugin plugin) {
		super(plugin);
	}

	@Override
	public Response.@NotNull Builder executeImmediately(@NotNull List<@NotNull ServerPlayerEntity> players, @NotNull Request request) {
		for (ServerPlayerEntity player : players) {
			Vec3d playAt = player.getPos().add(new Vec3d(player.getHorizontalFacing().getOpposite().getUnitVector()));
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
