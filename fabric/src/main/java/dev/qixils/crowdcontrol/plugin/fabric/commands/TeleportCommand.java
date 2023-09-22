package dev.qixils.crowdcontrol.plugin.fabric.commands;

import dev.qixils.crowdcontrol.common.ExecuteUsing;
import dev.qixils.crowdcontrol.common.util.RandomUtil;
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

import static dev.qixils.crowdcontrol.TimedEffect.isActive;
import static dev.qixils.crowdcontrol.common.command.CommandConstants.EAT_CHORUS_FRUIT_MAX_RADIUS;
import static dev.qixils.crowdcontrol.common.command.CommandConstants.EAT_CHORUS_FRUIT_MIN_RADIUS;

@Getter
@ExecuteUsing(ExecuteUsing.Type.SYNC_GLOBAL)
public class TeleportCommand extends ImmediateCommand {
	private final String effectName = "chorus_fruit";

	public TeleportCommand(FabricCrowdControlPlugin plugin) {
		super(plugin);
	}

	private static double nextDoubleOffset() {
		double value = RandomUtil.RNG.nextDouble(EAT_CHORUS_FRUIT_MIN_RADIUS, EAT_CHORUS_FRUIT_MAX_RADIUS);
		if (RandomUtil.RNG.nextBoolean()) {
			value = -value;
		}
		return value;
	}

	private static int nextIntOffset() {
		int value = RandomUtil.RNG.nextInt(EAT_CHORUS_FRUIT_MIN_RADIUS, EAT_CHORUS_FRUIT_MAX_RADIUS);
		if (RandomUtil.RNG.nextBoolean()) {
			value = -value;
		}
		return value;
	}

	@Override
	public Response.@NotNull Builder executeImmediately(@NotNull List<@NotNull ServerPlayer> players, @NotNull Request request) {
		if (isActive("walk", request) || isActive("look", request))
			return request.buildResponse().type(Response.ResultType.RETRY).message("Cannot fling while frozen");
		Response.Builder result = request.buildResponse()
				.type(Response.ResultType.RETRY)
				.message("No teleportation destinations were available");
		for (ServerPlayer player : players) {
			if (player.isPassenger())
				player.stopRiding();
			ServerLevel level = player.serverLevel();
			double x = player.getX();
			double y = player.getY();
			double z = player.getZ();
			for (int i = 0; i < 16; ++i) {
				double destX = x + nextDoubleOffset();
				double destY = Mth.clamp(y + nextIntOffset(), level.getMinBuildHeight(), level.getMinBuildHeight() + level.getLogicalHeight() - 1);
				double destZ = z + nextDoubleOffset();
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
