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
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

@Getter
public class WaterCommand extends ModdedCommand {
	private final String effectName = "make_water";

	public WaterCommand(ModdedCrowdControlPlugin plugin) {
		super(plugin);
	}

	@Override
	public void execute(@NotNull Supplier<@NotNull List<@NotNull ServerPlayer>> playerSupplier, @NotNull PublicEffectPayload request, @NotNull CCPlayer ccPlayer) {
		ccPlayer.sendResponse(ThreadUtil.waitForSuccess(request, () -> {
			Set<Location> locations = new HashSet<>();
			for (ServerPlayer player : playerSupplier.get())
				locations.addAll(BlockFinder.builder()
					.origin(player)
					.locationValidator(loc -> {
						var block = loc.block();
						if (block.is(BlockTags.AIR)) return true;
						var waterlogged = block.getOptionalValue(BlockStateProperties.WATERLOGGED);
						if (waterlogged.isEmpty()) return false; // waterloggable
						if (waterlogged.get()) return false; // but not watterlogged
						return true;
					})
					.shuffleLocations(false)
					.maxRadius(10)
					.build().getAll());

			if (locations.isEmpty())
				return new CCInstantEffectResponse(request.getRequestId(), ResponseStatus.FAIL_TEMPORARY, "No replaceable blocks nearby");

			sync(() -> locations.forEach(loc -> {
				var block = loc.block();
				loc.block(
					block.is(BlockTags.AIR)
						? Blocks.WATER.defaultBlockState()
						: block.trySetValue(BlockStateProperties.WATERLOGGED, true)
				);
			}));
			return new CCInstantEffectResponse(request.getRequestId(), ResponseStatus.SUCCESS);
		}));
	}
}
