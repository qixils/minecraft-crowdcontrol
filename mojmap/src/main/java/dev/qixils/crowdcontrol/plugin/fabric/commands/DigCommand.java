package dev.qixils.crowdcontrol.plugin.fabric.commands;

import dev.qixils.crowdcontrol.common.util.ThreadUtil;
import dev.qixils.crowdcontrol.plugin.fabric.ModdedCommand;
import dev.qixils.crowdcontrol.plugin.fabric.ModdedCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.fabric.utils.Location;
import live.crowdcontrol.cc4j.CCPlayer;
import live.crowdcontrol.cc4j.websocket.data.CCInstantEffectResponse;
import live.crowdcontrol.cc4j.websocket.data.ResponseStatus;
import live.crowdcontrol.cc4j.websocket.payload.PublicEffectPayload;
import lombok.Getter;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import static dev.qixils.crowdcontrol.common.command.CommandConstants.DIG_RADIUS;
import static dev.qixils.crowdcontrol.common.command.CommandConstants.getDigDepth;

@Getter
public class DigCommand extends ModdedCommand {
	private final String effectName = "dig";

	public DigCommand(ModdedCrowdControlPlugin plugin) {
		super(plugin);
	}

	@Override
	public void execute(@NotNull Supplier<@NotNull List<@NotNull ServerPlayer>> playerSupplier, @NotNull PublicEffectPayload request, @NotNull CCPlayer ccPlayer) {
		ccPlayer.sendResponse(ThreadUtil.waitForSuccess(() -> {
			Set<Location> locations = new HashSet<>();
			int depth = getDigDepth();
			for (ServerPlayer player : playerSupplier.get()) {
				Location playerLocation = new Location(player);
				for (double x = -DIG_RADIUS; x <= DIG_RADIUS; ++x) {
					for (int y = depth; y <= 0; ++y) {
						for (double z = -DIG_RADIUS; z <= DIG_RADIUS; ++z) {
							Location block = playerLocation.add(x, y, z);
							if (!block.block().isAir())
								locations.add(block);
						}
					}
				}
			}

			if (locations.isEmpty())
				return new CCInstantEffectResponse(request.getRequestId(), ResponseStatus.FAIL_TEMPORARY, "Streamer(s) not standing on any blocks");

			sync(() -> {
				for (Location location : locations)
					location.block(Blocks.AIR.defaultBlockState());
			});

			return new CCInstantEffectResponse(request.getRequestId(), ResponseStatus.SUCCESS);
		}));
	}
}
