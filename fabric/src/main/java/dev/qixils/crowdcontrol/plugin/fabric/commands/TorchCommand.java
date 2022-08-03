package dev.qixils.crowdcontrol.plugin.fabric.commands;

import dev.qixils.crowdcontrol.common.CommandConstants;
import dev.qixils.crowdcontrol.plugin.fabric.FabricCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.fabric.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.fabric.utils.BlockFinder;
import dev.qixils.crowdcontrol.plugin.fabric.utils.Location;
import dev.qixils.crowdcontrol.plugin.fabric.utils.TypedTag;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

@Getter
public class TorchCommand extends ImmediateCommand {
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
	protected final String displayName;

	public TorchCommand(FabricCrowdControlPlugin plugin, boolean placeTorches) {
		super(plugin);
		this.placeTorches = placeTorches;
		this.effectName = placeTorches ? "Lit" : "Dim";
		this.displayName = (placeTorches ? "Place" : "Break") + " Torches";
		this.torches = new TypedTag<>(CommandConstants.TORCHES, Registry.BLOCK);
	}

	@Override
	public Response.@NotNull Builder executeImmediately(@NotNull List<@NotNull ServerPlayer> players, @NotNull Request request) {
		Predicate<Location> predicate = placeTorches
				? loc -> BlockFinder.isReplaceable(loc.block())
				: loc -> torches.contains(loc.block().getBlock());

		List<Location> nearbyBlocks = new ArrayList<>();
		players.forEach(player -> nearbyBlocks.addAll(BlockFinder.builder()
				.origin(player)
				.maxRadius(5)
				.locationValidator(predicate)
				.shuffleLocations(false)
				.build().getAll()));

		if (nearbyBlocks.isEmpty())
			return request.buildResponse().type(Response.ResultType.FAILURE).message("No available blocks to place/remove");

		sync(() -> {
			for (Location location : nearbyBlocks) {
				if (placeTorches)
					placeTorch(location);
				else
					location.block(Blocks.AIR.defaultBlockState());
			}
		});
		return request.buildResponse().type(Response.ResultType.SUCCESS);
	}

	protected void placeTorch(Location location) {
		Direction placeFace = null;
		for (Direction blockFace : BLOCK_FACES) {
			boolean facingDown = blockFace == Direction.DOWN;
			if (!facingDown && placeFace != null) {
				continue;
			}
			BlockState target = location.relative(facingDown
					? blockFace
					: blockFace.getOpposite()
			).block();
			if (BlockFinder.isSolid(target)) {
				placeFace = blockFace;
				// down takes priority
				if (facingDown) {
					break;
				}
			}
		}
		if (placeFace == null) {
			return;
		}

		BlockState setBlock = Blocks.TORCH.defaultBlockState();
		if (placeFace != Direction.DOWN) {
			setBlock = Blocks.WALL_TORCH.defaultBlockState();
			setBlock.setValue(WallTorchBlock.FACING, placeFace);
		}

		location.block(setBlock);
	}
}
