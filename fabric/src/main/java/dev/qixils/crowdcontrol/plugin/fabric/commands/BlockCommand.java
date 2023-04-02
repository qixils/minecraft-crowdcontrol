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
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
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
				"block_" + Registries.BLOCK.getId(blockType).getPath(),
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
	protected Location getLocation(ServerPlayerEntity player) {
		Location location = new Location(player);
		if (!BlockFinder.isReplaceable(location))
			return null;
		return location;
	}

	@NotNull
	@Override
	public Response.Builder executeImmediately(@NotNull List<@NotNull ServerPlayerEntity> players, @NotNull Request request) {
		Builder result = request.buildResponse()
				.type(ResultType.RETRY)
				.message("No available locations to set blocks");
		for (ServerPlayerEntity player : players) {
			Location location = getLocation(player);
			if (location == null)
				continue;
			BlockState currentBlock = location.block();
			Block currentType = currentBlock.getBlock();
			if (BlockFinder.isReplaceable(currentBlock) && !currentType.equals(blockType)) {
				result.type(ResultType.SUCCESS).message("SUCCESS");
				sync(() -> location.block(blockType.getDefaultState()));
			}
		}
		return result;
	}
}
