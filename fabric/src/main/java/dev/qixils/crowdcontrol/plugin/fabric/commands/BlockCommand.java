package dev.qixils.crowdcontrol.plugin.fabric.commands;

import dev.qixils.crowdcontrol.plugin.fabric.FabricCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.fabric.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.fabric.utils.BlockFinder;
import dev.qixils.crowdcontrol.plugin.fabric.utils.Location;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import dev.qixils.crowdcontrol.socket.Response.Builder;
import dev.qixils.crowdcontrol.socket.Response.ResultType;
import lombok.Getter;
import net.minecraft.core.Registry;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Getter
public class BlockCommand extends ImmediateCommand {
	private final Block blockType;
	private final String effectName;
	private final String displayName;

	public BlockCommand(FabricCrowdControlPlugin plugin, Block blockType) {
		this(
				plugin,
				blockType,
				"block_" + Registry.BLOCK.getKey(blockType).getPath(),
				"Place " + plugin.getTextUtil().asPlain(blockType.getName()) + " Block"
		);
	}

	protected BlockCommand(FabricCrowdControlPlugin plugin, Block blockType, String effectName, String displayName) {
		super(plugin);
		this.blockType = blockType;
		this.effectName = effectName;
		this.displayName = displayName;
	}

	@Nullable
	protected Location getLocation(ServerPlayer player) {
		return new Location(player);
	}

	@NotNull
	@Override
	public Response.Builder executeImmediately(@NotNull List<@NotNull ServerPlayer> players, @NotNull Request request) {
		Builder result = request.buildResponse()
				.type(ResultType.RETRY)
				.message("No available locations to set blocks");
		for (ServerPlayer player : players) {
			if (!BlockFinder.isReplaceable(new Location(player)))
				continue;
			Location location = getLocation(player);
			if (location == null)
				continue;
			BlockState currentBlock = location.block();
			Block currentType = currentBlock.getBlock();
			if (BlockFinder.isReplaceable(currentBlock) && !currentType.equals(blockType)) {
				result.type(ResultType.SUCCESS).message("SUCCESS");
				sync(() -> location.block(blockType.defaultBlockState()));
			}
		}
		return result;
	}
}
