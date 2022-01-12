package dev.qixils.crowdcontrol.plugin.sponge7.commands;

import dev.qixils.crowdcontrol.common.util.CommonTags;
import dev.qixils.crowdcontrol.plugin.sponge7.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.sponge7.SpongeCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.sponge7.utils.Sponge7TextUtil;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response.Builder;
import dev.qixils.crowdcontrol.socket.Response.ResultType;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

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
				"block_" + Sponge7TextUtil.valueOf(blockType),
				"Place " + blockType.getTranslation().get() + " Block"
		);
	}

	protected BlockCommand(SpongeCrowdControlPlugin plugin, BlockType blockType, String effectName, String displayName) {
		super(plugin);
		this.blockType = blockType;
		this.effectName = effectName;
		this.displayName = displayName;
	}

	protected Location<World> getLocation(Player player) {
		return player.getLocation();
	}

	@NotNull
	@Override
	public Builder executeImmediately(@NotNull List<@NotNull Player> players, @NotNull Request request) {
		Builder result = request.buildResponse().type(ResultType.RETRY).message("No available locations to set blocks");
		for (Player player : players) {
			Location<World> location = getLocation(player);
			BlockType currentType = location.getBlockType();
			if (CommonTags.REPLACEABLE_BLOCKS.contains(SpongeCrowdControlPlugin.key(currentType))
					&& !currentType.equals(blockType)) {
				result.type(ResultType.SUCCESS).message("SUCCESS");
				plugin.getSyncExecutor().execute(() -> location.setBlockType(blockType));
			}
		}
		return result;
	}
}
