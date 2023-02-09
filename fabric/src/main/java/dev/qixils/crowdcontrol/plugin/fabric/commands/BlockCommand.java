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
import net.kyori.adventure.text.Component;
import net.minecraft.core.registries.BuiltInRegistries;
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
	private final Component displayName;

	public BlockCommand(FabricCrowdControlPlugin plugin, Block blockType) {
		this(
				plugin,
				blockType,
				"block_" + BuiltInRegistries.BLOCK.getKey(blockType).getPath(),
				Component.translatable("cc.effect.block.name", blockType.getName())
		);
	}

	protected BlockCommand(FabricCrowdControlPlugin plugin, Block blockType, String effectName, Component displayName) {
		super(plugin);
		this.blockType = blockType;
		this.effectName = effectName;
		this.displayName = displayName;
	}

	@Nullable
	protected Location getLocation(ServerPlayer player) {
		Location location = new Location(player);
		if (!BlockFinder.isReplaceable(location))
			return null;
		return location;
	}

	@NotNull
	@Override
	public Response.Builder executeImmediately(@NotNull List<@NotNull ServerPlayer> players, @NotNull Request request) {
		Builder result = request.buildResponse()
				.type(ResultType.RETRY)
				.message("No available locations to set blocks");
		for (ServerPlayer player : players) {
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
