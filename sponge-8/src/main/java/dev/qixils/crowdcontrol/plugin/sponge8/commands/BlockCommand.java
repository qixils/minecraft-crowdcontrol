package dev.qixils.crowdcontrol.plugin.sponge8.commands;

import dev.qixils.crowdcontrol.plugin.sponge8.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.sponge8.SpongeCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.sponge8.utils.BlockFinder;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response.Builder;
import dev.qixils.crowdcontrol.socket.Response.ResultType;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.world.Location;

import java.util.List;

@Getter
public class BlockCommand extends ImmediateCommand {
	private final BlockType blockType;
	private final String effectName;
	private final String displayName;

	public BlockCommand(SpongeCrowdControlPlugin plugin, BlockType blockType) {
		this(
				plugin,
				blockType,
				"block_" + blockType.key(RegistryTypes.BLOCK_TYPE).value(),
				"Place " + plugin.getTextUtil().asPlain(blockType) + " Block"
		);
	}

	protected BlockCommand(SpongeCrowdControlPlugin plugin, BlockType blockType, String effectName, String displayName) {
		super(plugin);
		this.blockType = blockType;
		this.effectName = effectName;
		this.displayName = displayName;
	}

	@Nullable
	protected Location<?, ?> getLocation(ServerPlayer player) {
		return player.location();
	}

	@NotNull
	@Override
	public Builder executeImmediately(@NotNull List<@NotNull ServerPlayer> players, @NotNull Request request) {
		Builder result = request.buildResponse()
				.type(ResultType.RETRY)
				.message("No available locations to set blocks");
		for (ServerPlayer player : players) {
			if (!BlockFinder.isReplaceable(player.location().block()))
				continue;
			Location<?, ?> location = getLocation(player);
			if (location == null)
				continue;
			BlockState currentBlock = location.block();
			BlockType currentType = currentBlock.type();
			if (BlockFinder.isReplaceable(currentBlock) && !currentType.equals(blockType)) {
				result.type(ResultType.SUCCESS).message("SUCCESS");
				sync(() -> location.setBlockType(blockType));
			}
		}
		return result;
	}
}
