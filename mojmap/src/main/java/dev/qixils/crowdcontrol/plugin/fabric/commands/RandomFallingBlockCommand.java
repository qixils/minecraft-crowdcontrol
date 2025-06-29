package dev.qixils.crowdcontrol.plugin.fabric.commands;

import dev.qixils.crowdcontrol.common.util.RandomUtil;
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
import java.util.function.Supplier;

import static dev.qixils.crowdcontrol.common.command.CommandConstants.FALLING_BLOCK_FALL_DISTANCE;

public class RandomFallingBlockCommand extends ModdedCommand {
	public RandomFallingBlockCommand(ModdedCrowdControlPlugin plugin) {
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

	@Override
	public void execute(@NotNull Supplier<@NotNull List<@NotNull ServerPlayer>> playerSupplier, @NotNull PublicEffectPayload request, @NotNull CCPlayer ccPlayer) {
		ccPlayer.sendResponse(ThreadUtil.waitForSuccess(request, () -> {
			boolean success = false;

			ploop:
			for (ServerPlayer player : playerSupplier.get()) {
				Location playerLoc = new Location(player);
				ServerLevel world = player.level();
				BlockPos position = BlockPos.containing(
					playerLoc.x(),
					Math.min(
						playerLoc.y() + FALLING_BLOCK_FALL_DISTANCE,
						world.getMaxY() - 1
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
				Block block = getRandomBlock(player.level());
				FallingBlockEntity entity = FallingBlockEntity.fall(player.level(), position, block.defaultBlockState());
				entity.fallDamagePerDistance = 0.75f;
				entity.fallDamageMax = 4;
				entity.dropItem = true;

				success = true;
			}

			return success
				? new CCInstantEffectResponse(request.getRequestId(), ResponseStatus.SUCCESS)
				: new CCInstantEffectResponse(request.getRequestId(), ResponseStatus.FAIL_TEMPORARY, "Unable to find valid spawning location");
		}, plugin.getSyncExecutor()));
	}
}
