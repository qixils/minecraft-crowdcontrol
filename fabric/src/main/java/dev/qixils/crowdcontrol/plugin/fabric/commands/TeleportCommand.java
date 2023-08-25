package dev.qixils.crowdcontrol.plugin.fabric.commands;

import dev.qixils.crowdcontrol.common.ExecuteUsing;
import dev.qixils.crowdcontrol.plugin.fabric.FabricCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.fabric.ImmediateCommand;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Getter
@ExecuteUsing(ExecuteUsing.Type.SYNC_GLOBAL)
public class TeleportCommand extends ImmediateCommand {
	private final String effectName = "chorus_fruit";

	public TeleportCommand(FabricCrowdControlPlugin plugin) {
		super(plugin);
	}

	@Override
	public Response.@NotNull Builder executeImmediately(@NotNull List<@NotNull ServerPlayer> players, @NotNull Request request) {
		Response.Builder result = request.buildResponse()
				.type(Response.ResultType.FAILURE)
				.message("No teleportation destinations were available");
		for (ServerPlayer player : players) {
			if (player.isPassenger())
				player.stopRiding();
			ServerLevel level = player.serverLevel();
			double x = player.getX();
			double y = player.getY();
			double z = player.getZ();
			for (int i = 0; i < 16; ++i) {
				double destX = player.getX() + (player.getRandom().nextDouble() - 0.5) * 16.0;
				double destY = Mth.clamp(player.getY() + (double)(player.getRandom().nextInt(16) - 8), level.getMinBuildHeight(), level.getMinBuildHeight() + level.getLogicalHeight() - 1);
				double destZ = player.getZ() + (player.getRandom().nextDouble() - 0.5) * 16.0;
				if (!player.randomTeleport(destX, destY, destZ, true)) continue;
				level.playSound(null, x, y, z, SoundEvents.CHORUS_FRUIT_TELEPORT, SoundSource.PLAYERS, 1.0f, 1.0f);
				player.playSound(SoundEvents.CHORUS_FRUIT_TELEPORT, 1.0f, 1.0f);
				result.type(Response.ResultType.SUCCESS).message("SUCCESS");
				break;
			}
		}
		return result;
	}
}
