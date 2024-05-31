package dev.qixils.crowdcontrol.plugin.fabric.commands;

import dev.qixils.crowdcontrol.common.ExecuteUsing;
import dev.qixils.crowdcontrol.common.util.RandomUtil;
import dev.qixils.crowdcontrol.plugin.fabric.FabricCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.fabric.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.fabric.utils.BlockFinder;
import dev.qixils.crowdcontrol.plugin.fabric.utils.Location;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import static dev.qixils.crowdcontrol.common.command.CommandConstants.FALLING_BLOCK_FALL_DISTANCE;

@ExecuteUsing(ExecuteUsing.Type.SYNC_GLOBAL)
public class RandomFallingBlockCommand extends ImmediateCommand {
	public RandomFallingBlockCommand(FabricCrowdControlPlugin plugin) {
		super(plugin);
	}

	@Getter
	private final String effectName = "falling_block_random";
	private final Map<ServerLevel, List<Block>> blocks = new WeakHashMap<>();

	public Block getRandomBlock(ServerLevel level) {
		return RandomUtil.randomElementFrom(blocks.computeIfAbsent(level, $ -> BuiltInRegistries.BLOCK.stream()
			.filter(block -> block.defaultBlockState().isSolid()
				&& block.defaultBlockState().canOcclude()
				&& block.isEnabled(level.enabledFeatures()))
			.toList()));
	}

	@NotNull
	@Override
	public Response.Builder executeImmediately(@NotNull List<@NotNull ServerPlayer> players, @NotNull Request request) {
		boolean success = false;

		ploop:
		for (ServerPlayer player : players) {
			Location playerLoc = new Location(player);
			ServerLevel world = player.serverLevel();
			BlockPos position = BlockPos.containing(
				playerLoc.x(),
				Math.min(
					playerLoc.y() + FALLING_BLOCK_FALL_DISTANCE,
					world.getMaxBuildHeight() - 1
				),
				playerLoc.z()
			);
			// the below for loop does not use <= because the main execute method performs its own
			// checks
			for (int y = (int) Math.floor(playerLoc.y()+1); y < position.getY(); y++) {
				BlockState block = world.getBlockState(new BlockPos(position.getX(), y, position.getZ()));
				if (!BlockFinder.isPassable(block)) {
					continue ploop;
				}
			}

			// get block to place
			Block block = getRandomBlock(player.serverLevel());
			FallingBlockEntity entity = FallingBlockEntity.fall(player.serverLevel(), position, block.defaultBlockState());
			entity.fallDamagePerDistance = 0.75f;
			entity.fallDamageMax = 4;
			entity.dropItem = true;

			success = true;
		}

		return success
			? request.buildResponse().type(Response.ResultType.SUCCESS)
			: request.buildResponse().type(Response.ResultType.FAILURE).message("Unable to find valid spawning location");
	}
}
