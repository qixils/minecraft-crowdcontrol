package dev.qixils.crowdcontrol.plugin.sponge7.commands;

import dev.qixils.crowdcontrol.plugin.sponge7.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.sponge7.SpongeCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.sponge7.utils.BlockFinder;
import dev.qixils.crowdcontrol.plugin.sponge7.utils.SpongeTextUtil;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response.Builder;
import dev.qixils.crowdcontrol.socket.Response.ResultType;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.List;

import static net.kyori.adventure.text.Component.translatable;

@Getter
public class BlockCommand extends ImmediateCommand {
	private final BlockType blockType;
	private final String effectName;
	private final Component displayName;

	public BlockCommand(SpongeCrowdControlPlugin plugin, BlockType blockType) {
		this(
				plugin,
				blockType,
				"block_" + SpongeTextUtil.valueOf(blockType)
		);
	}

	public BlockCommand(SpongeCrowdControlPlugin plugin, BlockType blockType, String effectName) {
		this(
				plugin,
				blockType,
				effectName,
				translatable("cc.effect.block.name", translatable(blockType.getTranslation().getId()))
		);
	}

	public BlockCommand(SpongeCrowdControlPlugin plugin, BlockType blockType, String effectName, Component displayName) {
		super(plugin);
		this.blockType = blockType;
		this.effectName = effectName;
		this.displayName = displayName;
	}

	@Nullable
	protected Location<World> getLocation(Player player) {
		Location<World> location = player.getLocation();
		if (!BlockFinder.isReplaceable(location.getBlock()))
			return null;
		return location;
	}

	@NotNull
	@Override
	public Builder executeImmediately(@NotNull List<@NotNull Player> players, @NotNull Request request) {
		Builder result = request.buildResponse()
				.type(ResultType.RETRY)
				.message("No available locations to set blocks");
		for (Player player : players) {
			Location<World> location = getLocation(player);
			if (location == null)
				continue;
			BlockState currentBlock = location.getBlock();
			BlockType currentType = currentBlock.getType();
			if (BlockFinder.isReplaceable(currentBlock) && !currentType.equals(blockType)) {
				result.type(ResultType.SUCCESS).message("SUCCESS");
				sync(() -> location.setBlockType(blockType));
			}
		}
		return result;
	}
}
