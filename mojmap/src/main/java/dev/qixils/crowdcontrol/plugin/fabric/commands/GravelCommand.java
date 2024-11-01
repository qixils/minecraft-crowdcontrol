package dev.qixils.crowdcontrol.plugin.fabric.commands;

import dev.qixils.crowdcontrol.common.util.ThreadUtil;
import dev.qixils.crowdcontrol.plugin.fabric.ModdedCommand;
import dev.qixils.crowdcontrol.plugin.fabric.ModdedCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.fabric.utils.BlockFinder;
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

@Getter
public class GravelCommand extends ModdedCommand {
	private final String effectName = "gravel_hell";

	public GravelCommand(ModdedCrowdControlPlugin plugin) {
		super(plugin);
	}

	@Override
	public void execute(@NotNull Supplier<@NotNull List<@NotNull ServerPlayer>> playerSupplier, @NotNull PublicEffectPayload request, @NotNull CCPlayer ccPlayer) {
		ccPlayer.sendResponse(ThreadUtil.waitForSuccess(() -> {
			Set<Location> locations = new HashSet<>();
			for (ServerPlayer player : playerSupplier.get())
				locations.addAll(BlockFinder.builder()
					.origin(player)
					.locationValidator(loc -> !loc.block().isAir() && !loc.block().is(Blocks.GRAVEL))
					.shuffleLocations(false)
					.maxRadius(7)
					.build().getAll());

			if (locations.isEmpty())
				return new CCInstantEffectResponse(request.getRequestId(), ResponseStatus.FAIL_TEMPORARY, "No replaceable blocks nearby");

			sync(() -> locations.forEach(location -> location.block(Blocks.GRAVEL.defaultBlockState())));
			return new CCInstantEffectResponse(request.getRequestId(), ResponseStatus.SUCCESS);
		}));
	}
}
