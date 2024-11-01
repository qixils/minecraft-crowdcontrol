package dev.qixils.crowdcontrol.plugin.fabric.commands;

import dev.qixils.crowdcontrol.common.command.CommandConstants;
import dev.qixils.crowdcontrol.common.util.ThreadUtil;
import dev.qixils.crowdcontrol.plugin.fabric.ModdedCommand;
import dev.qixils.crowdcontrol.plugin.fabric.ModdedCrowdControlPlugin;
import live.crowdcontrol.cc4j.CCPlayer;
import live.crowdcontrol.cc4j.websocket.data.CCInstantEffectResponse;
import live.crowdcontrol.cc4j.websocket.data.ResponseStatus;
import live.crowdcontrol.cc4j.websocket.payload.PublicEffectPayload;
import lombok.Getter;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Supplier;

@Getter
public class FlingCommand extends ModdedCommand {
	private final @NotNull String effectName = "fling";

	public FlingCommand(@NotNull ModdedCrowdControlPlugin plugin) {
		super(plugin);
	}

	@NotNull
	private static Vec3 randomVector() {
		double[] vector = CommandConstants.randomFlingVector();
		return new Vec3(vector[0], vector[1], vector[2]);
	}

	@Override
	public void execute(@NotNull Supplier<List<ServerPlayer>> playerSupplier, @NotNull PublicEffectPayload request, @NotNull CCPlayer ccPlayer) {
		ccPlayer.sendResponse(ThreadUtil.waitForSuccess(() -> {
			if (isActive(ccPlayer, "walk") || isActive(ccPlayer, "look"))
				return new CCInstantEffectResponse(request.getRequestId(), ResponseStatus.FAIL_TEMPORARY, "Cannot fling while frozen");

			boolean success = false;
			for (ServerPlayer player : playerSupplier.get()) {
				if (player.isPassenger()) continue;

				player.setDeltaMovement(randomVector());
				player.hurtMarked = true;
				success = true;
			}

			return success
				? new CCInstantEffectResponse(request.getRequestId(), ResponseStatus.SUCCESS)
				: new CCInstantEffectResponse(request.getRequestId(), ResponseStatus.FAIL_TEMPORARY, "Cannot fling while inside vehicle");
		}, plugin.getSyncExecutor()));
	}
}
