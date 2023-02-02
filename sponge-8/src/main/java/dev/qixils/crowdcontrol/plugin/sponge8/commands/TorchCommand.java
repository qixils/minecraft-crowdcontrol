package dev.qixils.crowdcontrol.plugin.sponge8.commands;

import dev.qixils.crowdcontrol.common.command.CommandConstants;
import dev.qixils.crowdcontrol.plugin.sponge8.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.sponge8.SpongeCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.sponge8.utils.BlockFinder;
import dev.qixils.crowdcontrol.plugin.sponge8.utils.TypedTag;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.server.ServerLocation;

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

	public TorchCommand(SpongeCrowdControlPlugin plugin, boolean placeTorches) {
		super(plugin);
		this.placeTorches = placeTorches;
		this.effectName = placeTorches ? "lit" : "dim";
		this.torches = new TypedTag<>(CommandConstants.TORCHES, plugin, RegistryTypes.BLOCK_TYPE);
	}

	@Override
	public Response.@NotNull Builder executeImmediately(@NotNull List<@NotNull ServerPlayer> players, @NotNull Request request) {
		Predicate<ServerLocation> predicate = placeTorches
				? loc -> BlockFinder.isReplaceable(loc.block())
				: loc -> torches.contains(loc.blockType());

		List<ServerLocation> nearbyBlocks = new ArrayList<>();
		players.forEach(player -> nearbyBlocks.addAll(BlockFinder.builder()
				.origin(player.serverLocation())
				.maxRadius(5)
				.locationValidator(predicate)
				.shuffleLocations(false)
				.build().getAll()));

		if (nearbyBlocks.isEmpty())
			return request.buildResponse().type(Response.ResultType.RETRY).message("No available blocks to place/remove");

		sync(() -> {
			for (ServerLocation location : nearbyBlocks) {
				if (placeTorches)
					placeTorch(location);
				else
					location.setBlockType(BlockTypes.AIR.get());
			}
		});
		// TODO: this can technically return success even if no blocks were placed. probably on all platforms but kinda minor
		return request.buildResponse().type(Response.ResultType.SUCCESS);
	}

	protected void placeTorch(ServerLocation location) {
		Direction placeFace = null;
		for (Direction blockFace : BLOCK_FACES) {
			boolean facingDown = blockFace == Direction.DOWN;
			if (!facingDown && placeFace != null) {
				continue;
			}
			BlockState target = location.relativeToBlock(facingDown
					? blockFace
					: blockFace.opposite()
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

		BlockState.Builder setBlock = BlockState.builder().blockType(BlockTypes.TORCH);
		if (placeFace != Direction.DOWN)
			setBlock.blockType(BlockTypes.WALL_TORCH).add(Keys.DIRECTION, placeFace);

		location.setBlock(setBlock.build());
	}
}
