package dev.qixils.crowdcontrol.plugin.fabric.commands;

import dev.qixils.crowdcontrol.common.util.RandomUtil;
import dev.qixils.crowdcontrol.common.util.ThreadUtil;
import dev.qixils.crowdcontrol.plugin.fabric.ModdedCommand;
import dev.qixils.crowdcontrol.plugin.fabric.ModdedCrowdControlPlugin;
import live.crowdcontrol.cc4j.CCPlayer;
import live.crowdcontrol.cc4j.websocket.data.CCInstantEffectResponse;
import live.crowdcontrol.cc4j.websocket.data.ResponseStatus;
import live.crowdcontrol.cc4j.websocket.payload.PublicEffectPayload;
import lombok.Getter;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Supplier;

import static dev.qixils.crowdcontrol.common.command.CommandConstants.EAT_CHORUS_FRUIT_MAX_RADIUS;
import static dev.qixils.crowdcontrol.common.command.CommandConstants.EAT_CHORUS_FRUIT_MIN_RADIUS;

@Getter
public class TeleportCommand extends ModdedCommand {
	private final String effectName = "chorus_fruit";
	private final List<String> effectGroups = List.of("walk", "look");

	public TeleportCommand(ModdedCrowdControlPlugin plugin) {
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
	public void execute(@NotNull Supplier<@NotNull List<@NotNull ServerPlayer>> playerSupplier, @NotNull PublicEffectPayload request, @NotNull CCPlayer ccPlayer) {
		ccPlayer.sendResponse(ThreadUtil.waitForSuccess(() -> {
			if (isArrayActive(ccPlayer))
				return new CCInstantEffectResponse(request.getRequestId(), ResponseStatus.FAIL_TEMPORARY, "Cannot fling while frozen");
			boolean success = false;
			for (ServerPlayer player : playerSupplier.get()) {
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
					success = true;
					break;
				}
			}
			return success
				? new CCInstantEffectResponse(request.getRequestId(), ResponseStatus.SUCCESS)
				: new CCInstantEffectResponse(request.getRequestId(), ResponseStatus.FAIL_TEMPORARY, "No teleportation destinations were available");
		}, plugin.getSyncExecutor()));
	}
}
