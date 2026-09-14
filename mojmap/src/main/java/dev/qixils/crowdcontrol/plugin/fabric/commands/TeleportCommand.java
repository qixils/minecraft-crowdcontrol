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
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.BlockUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
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
		ccPlayer.sendResponse(ThreadUtil.waitForSuccess(request, () -> {
			if (isArrayActive(ccPlayer))
				return new CCInstantEffectResponse(request.getRequestId(), ResponseStatus.FAIL_TEMPORARY, "Cannot teleport while frozen");
			boolean success = false;
			for (ServerPlayer player : playerSupplier.get()) {
				if (player.isPassenger())
					player.stopRiding();
				ServerLevel level = player.level();
				Vec3 oldPos = player.position();
				double x = oldPos.x();
				double y = oldPos.y();
				double z = oldPos.z();
				// wtf mojang gives us variable names now?
				for (int attempt = 0; attempt < 16; attempt++) {
					double xx = x + nextDoubleOffset();
					double yy = Mth.clamp(
						y + nextDoubleOffset(),
						level.getMinY(),
						level.getMinY() + level.getLogicalHeight() - 1
					);
					double zz = z + nextDoubleOffset();

					if (player.randomTeleport(xx, yy, zz, true, BlockTags.CONSUMABLE_DOES_NOT_TELEPORT_TO)) {
						level.gameEvent(GameEvent.TELEPORT, oldPos, GameEvent.Context.of(player));

						level.playSound(null, xx, yy, zz, SoundEvents.CHORUS_FRUIT_TELEPORT, SoundSource.PLAYERS);

						BlockPos origin = BlockPos.containing(oldPos);
						BlockPos target = player.blockPosition();
						level.levelEvent(2017, origin, BlockUtil.clampedPackDifferenceInPosition(origin, target, 127, 127, 127));

						player.resetFallDistance();
						player.resetCurrentImpulseContext();
						success = true;
						break;
					}
				}
			}
			return success
				? new CCInstantEffectResponse(request.getRequestId(), ResponseStatus.SUCCESS)
				: new CCInstantEffectResponse(request.getRequestId(), ResponseStatus.FAIL_TEMPORARY, "No teleportation destinations were available");
		}, plugin.getSyncExecutor()));
	}
}
