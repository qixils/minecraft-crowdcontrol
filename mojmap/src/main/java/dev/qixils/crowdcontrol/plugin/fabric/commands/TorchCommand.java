package dev.qixils.crowdcontrol.plugin.fabric.commands;

import dev.qixils.crowdcontrol.common.command.CommandConstants;
import dev.qixils.crowdcontrol.common.util.ThreadUtil;
import dev.qixils.crowdcontrol.plugin.fabric.ModdedCommand;
import dev.qixils.crowdcontrol.plugin.fabric.ModdedCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.fabric.utils.BlockFinder;
import dev.qixils.crowdcontrol.plugin.fabric.utils.Location;
import dev.qixils.crowdcontrol.plugin.fabric.utils.TypedTag;
import live.crowdcontrol.cc4j.CCPlayer;
import live.crowdcontrol.cc4j.websocket.data.CCInstantEffectResponse;
import live.crowdcontrol.cc4j.websocket.data.ResponseStatus;
import live.crowdcontrol.cc4j.websocket.payload.PublicEffectPayload;
import lombok.Getter;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

@Getter
public class TorchCommand extends ModdedCommand {
	protected static final Direction[] BLOCK_FACES = new Direction[]{
			Direction.DOWN,
			Direction.EAST,
			Direction.WEST,
			Direction.SOUTH,
			Direction.NORTH
	};
	protected final TypedTag<Block> torches;
	protected final boolean placeTorches;
	protected final String effectName;

	public TorchCommand(ModdedCrowdControlPlugin plugin, boolean placeTorches) {
		super(plugin);
		this.placeTorches = placeTorches;
		this.effectName = placeTorches ? "lit" : "dim";
		this.torches = new TypedTag<>(CommandConstants.TORCHES, BuiltInRegistries.BLOCK);
	}

	@Override
	public void execute(@NotNull Supplier<@NotNull List<@NotNull ServerPlayer>> playerSupplier, @NotNull PublicEffectPayload request, @NotNull CCPlayer ccPlayer) {
		ccPlayer.sendResponse(ThreadUtil.waitForSuccess(request, () -> {
			Predicate<Location> predicate = placeTorches
				? loc -> BlockFinder.isReplaceable(loc.block())
				: loc -> torches.contains(loc.block().getBlock());

			List<Location> nearbyBlocks = new ArrayList<>();
			playerSupplier.get().forEach(player -> nearbyBlocks.addAll(BlockFinder.builder()
				.origin(player)
				.maxRadius(5)
				.locationValidator(predicate)
				.shuffleLocations(false)
				.build().getAll()));

			if (nearbyBlocks.isEmpty())
				return new CCInstantEffectResponse(request.getRequestId(), ResponseStatus.FAIL_TEMPORARY, "No available blocks to place/remove");

			sync(() -> {
				for (Location location : nearbyBlocks) {
					if (placeTorches)
						placeTorch(location);
					else
						location.block(Blocks.AIR.defaultBlockState());
				}
			});
			return new CCInstantEffectResponse(request.getRequestId(), ResponseStatus.SUCCESS);
		}));
	}

	protected void placeTorch(Location location) {
		Direction placeFace = null;
		for (Direction blockFace : BLOCK_FACES) {
			BlockState target = location.relative(blockFace == Direction.DOWN
					? blockFace
					: blockFace.getOpposite()
			).block();
			if (BlockFinder.isSolid(target)) {
				placeFace = blockFace;
				break;
			}
		}
		if (placeFace == null)
			return;

		BlockState setBlock = placeFace == Direction.DOWN
				? Blocks.TORCH.defaultBlockState()
				: Blocks.WALL_TORCH.defaultBlockState().setValue(WallTorchBlock.FACING, placeFace);
		location.block(setBlock);
	}
}
