package dev.qixils.crowdcontrol.plugin.sponge8.commands;

import dev.qixils.crowdcontrol.plugin.sponge8.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.sponge8.SpongeCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.sponge8.utils.BlockFinder;
import live.crowdcontrol.cc4j.websocket.payload.PublicEffectPayload;
import dev.qixils.crowdcontrol.socket.Response.Builder;
import dev.qixils.crowdcontrol.socket.Response.ResultType;
import lombok.Getter;
import net.kyori.adventure.text.Component;
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
	private final Component displayName;

	public BlockCommand(SpongeCrowdControlPlugin plugin, BlockType blockType) {
		this(
				plugin,
				blockType,
				"block_" + blockType.key(RegistryTypes.BLOCK_TYPE).value(),
				Component.translatable("cc.effect.block.name", blockType)
		);
	}

	protected BlockCommand(SpongeCrowdControlPlugin plugin, BlockType blockType, String effectName, Component displayName) {
		super(plugin);
		this.blockType = blockType;
		this.effectName = effectName;
		this.displayName = displayName;
	}

	@Nullable
	protected Location<?, ?> getLocation(ServerPlayer player) {
		Location<?, ?> location = player.location();
		if (!BlockFinder.isReplaceable(location.block()))
			return null;
		return location;
	}

	@NotNull
	@Override
	public Builder executeImmediately(@NotNull List<@NotNull ServerPlayer> players, @NotNull Request request) {
		Builder result = request.buildResponse()
				.type(ResultType.RETRY)
				.message("No available locations to set blocks");
		for (ServerPlayer player : players) {
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
