package dev.qixils.crowdcontrol.plugin.sponge8.commands;

import dev.qixils.crowdcontrol.common.ExecuteUsing;
import dev.qixils.crowdcontrol.common.util.RandomUtil;
import dev.qixils.crowdcontrol.plugin.sponge8.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.sponge8.SpongeCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.sponge8.utils.BlockFinder;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.FallingBlock;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.math.vector.Vector3i;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static dev.qixils.crowdcontrol.common.command.CommandConstants.FALLING_BLOCK_FALL_DISTANCE;

@ExecuteUsing(ExecuteUsing.Type.SYNC_GLOBAL)
public class RandomFallingBlockCommand extends ImmediateCommand {
	public RandomFallingBlockCommand(SpongeCrowdControlPlugin plugin) {
		super(plugin);
	}

	@Getter
	private final String effectName = "falling_block_random";
	private final Map<UUID, List<BlockType>> blocks = new HashMap<>();

	public BlockType getRandomBlock(ServerWorld world) {
		return RandomUtil.randomElementFrom(blocks.computeIfAbsent(world.uniqueId(), $ -> plugin.registryStream(RegistryTypes.BLOCK_TYPE)
			.filter(BlockFinder::isSolid)
			.collect(Collectors.toList())));
	}

	@NotNull
	@Override
	public Response.Builder executeImmediately(@NotNull List<@NotNull ServerPlayer> players, @NotNull Request request) {
		boolean success = false;

		ploop:
		for (ServerPlayer player : players) {
			Location<?, ?> playerLoc = player.location();
			World<?, ?> world = playerLoc.world();
			Vector3i position = new Vector3i(
				playerLoc.x(),
				Math.min(
					playerLoc.y() + FALLING_BLOCK_FALL_DISTANCE,
					world.maximumHeight() - 1
				),
				playerLoc.z()
			);
			// the below for loop does not use <= because the main execute method performs its own
			// checks
			for (int y = playerLoc.blockY()+1; y < position.y(); y++) {
				BlockState block = world.block(new Vector3i(position.x(), y, position.z()));
				if (!BlockFinder.isPassable(block)) {
					continue ploop;
				}
			}

			// get block to place
			BlockType block = getRandomBlock(player.world());
			FallingBlock entity = world.createEntity(EntityTypes.FALLING_BLOCK, position);
			entity.offer(Keys.BLOCK_STATE, block.defaultState());
			entity.offer(Keys.DAMAGE_PER_BLOCK, 0.75);
			entity.offer(Keys.MAX_FALL_DAMAGE, 4.0);
			entity.offer(Keys.CAN_DROP_AS_ITEM, true);

			success = true;
		}

		return success
			? request.buildResponse().type(Response.ResultType.SUCCESS)
			: request.buildResponse().type(Response.ResultType.FAILURE).message("Unable to find valid spawning location");
	}
}
