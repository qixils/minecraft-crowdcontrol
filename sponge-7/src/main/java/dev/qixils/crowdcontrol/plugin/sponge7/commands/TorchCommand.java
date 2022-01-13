package dev.qixils.crowdcontrol.plugin.sponge7.commands;

import dev.qixils.crowdcontrol.common.CommandConstants;
import dev.qixils.crowdcontrol.plugin.sponge7.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.sponge7.SpongeCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.sponge7.utils.BlockFinder;
import dev.qixils.crowdcontrol.plugin.sponge7.utils.TypedTag;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.BlockChangeFlags;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

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
	protected final TypedTag<BlockType> torches;
	protected final boolean placeTorches;
	protected final String effectName;
	protected final String displayName;

	public TorchCommand(SpongeCrowdControlPlugin plugin, boolean placeTorches) {
		super(plugin);
		this.placeTorches = placeTorches;
		this.effectName = placeTorches ? "Lit" : "Dim";
		this.displayName = (placeTorches ? "Place" : "Break") + " Torches";
		this.torches = new TypedTag<>(CommandConstants.TORCHES, plugin, BlockType.class);
	}

	@Override
	public Response.@NotNull Builder executeImmediately(@NotNull List<@NotNull Player> players, @NotNull Request request) {
		Predicate<Location<World>> predicate = placeTorches
				? loc -> BlockFinder.isReplaceable(loc.getBlock())
				: loc -> torches.contains(loc.getBlock().getType());

		List<Location<World>> nearbyBlocks = new ArrayList<>();
		players.forEach(player -> nearbyBlocks.addAll(BlockFinder.builder()
				.origin(player.getLocation())
				.maxRadius(5)
				.locationValidator(predicate)
				.shuffleLocations(false)
				.build().getAll()));

		if (nearbyBlocks.isEmpty())
			return request.buildResponse().type(Response.ResultType.FAILURE).message("No available blocks to place/remove");

		sync(() -> {
			for (Location<World> location : nearbyBlocks) {
				if (placeTorches)
					placeTorch(location);
				else
					location.setBlockType(BlockTypes.AIR, BlockChangeFlags.NONE);
			}
		});
		return request.buildResponse().type(Response.ResultType.SUCCESS);
	}

	protected void placeTorch(Location<World> location) {
		Direction placeFace = null;
		for (Direction blockFace : BLOCK_FACES) {
			boolean facingDown = blockFace == Direction.DOWN;
			if (!facingDown && placeFace != null) {
				continue;
			}
			BlockState target = location.getBlockRelative(facingDown
					? blockFace
					: blockFace.getOpposite()
			).getBlock();
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

		BlockState.Builder setBlock = BlockState.builder().blockType(BlockTypes.TORCH);
		if (placeFace != Direction.DOWN)
			setBlock.add(Keys.DIRECTION, placeFace);

		location.setBlock(setBlock.build(), BlockChangeFlags.NONE);
	}
}
