package dev.qixils.crowdcontrol.plugin.sponge7.commands;

import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;
import dev.qixils.crowdcontrol.common.ExecuteUsing;
import dev.qixils.crowdcontrol.common.util.RandomUtil;
import dev.qixils.crowdcontrol.plugin.sponge7.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.sponge7.SpongeCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.sponge7.utils.BlockFinder;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.FallingBlock;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

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

	public BlockType getRandomBlock(World world) {
		return RandomUtil.randomElementFrom(blocks.computeIfAbsent(world.getUniqueId(), $ -> plugin.getRegistry().getAllOf(BlockType.class)
			.stream()
			.filter(BlockFinder::isSolid)
			.collect(Collectors.toList())));
	}

	@NotNull
	@Override
	public Response.Builder executeImmediately(@NotNull List<@NotNull Player> players, @NotNull Request request) {
		boolean success = false;

		ploop:
		for (Player player : players) {
			Location<World> playerLoc = player.getLocation();
			World world = playerLoc.getExtent();
			Vector3i position = new Vector3i(
				playerLoc.getX(),
				Math.min(
					playerLoc.getY() + FALLING_BLOCK_FALL_DISTANCE,
					world.getBlockMax().getY()
				),
				playerLoc.getZ()
			);
			// the below for loop does not use <= because the main execute method performs its own
			// checks
			for (int y = playerLoc.getBlockY()+1; y < position.getY(); y++) {
				BlockState block = world.getBlock(new Vector3i(position.getX(), y, position.getZ()));
				if (!BlockFinder.isPassable(block)) {
					continue ploop;
				}
			}

			Vector3d destination = new Vector3d(position.getX() + 0.5, position.getY(), position.getZ() + 0.5);

			// get block to place
			BlockType block = getRandomBlock(player.getWorld());
			FallingBlock entity = (FallingBlock) world.createEntity(EntityTypes.FALLING_BLOCK, destination);
			entity.offer(Keys.FALL_TIME, 1);
			entity.offer(Keys.FALLING_BLOCK_STATE, block.getDefaultState());
			entity.offer(Keys.FALL_DAMAGE_PER_BLOCK, 0.75);
			entity.offer(Keys.MAX_FALL_DAMAGE, 4.0);
			entity.offer(Keys.CAN_DROP_AS_ITEM, true);
			entity.offer(Keys.FALLING_BLOCK_CAN_HURT_ENTITIES, true);

			success |= world.spawnEntity(entity);
		}

		return success
			? request.buildResponse().type(Response.ResultType.SUCCESS)
			: request.buildResponse().type(Response.ResultType.FAILURE).message("Unable to find valid spawning location");
	}
}
