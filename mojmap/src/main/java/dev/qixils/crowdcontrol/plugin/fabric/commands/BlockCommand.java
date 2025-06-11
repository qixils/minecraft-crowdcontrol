package dev.qixils.crowdcontrol.plugin.fabric.commands;

import dev.qixils.crowdcontrol.common.util.ThreadUtil;
import dev.qixils.crowdcontrol.plugin.fabric.FeatureElementCommand;
import dev.qixils.crowdcontrol.plugin.fabric.ModdedCommand;
import dev.qixils.crowdcontrol.plugin.fabric.ModdedCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.fabric.utils.BlockFinder;
import dev.qixils.crowdcontrol.plugin.fabric.utils.Location;
import live.crowdcontrol.cc4j.CCPlayer;
import live.crowdcontrol.cc4j.websocket.data.CCInstantEffectResponse;
import live.crowdcontrol.cc4j.websocket.data.ResponseStatus;
import live.crowdcontrol.cc4j.websocket.payload.PublicEffectPayload;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

@Getter
public class BlockCommand extends ModdedCommand implements FeatureElementCommand {
	private final Block blockType;
	private final String effectName;
	private final Component displayName;

	public BlockCommand(ModdedCrowdControlPlugin plugin, Block blockType) {
		this(
				plugin,
				blockType,
				"block_" + BuiltInRegistries.BLOCK.getKey(blockType).getPath(),
				Component.translatable("cc.effect.block.name", plugin.toAdventure(blockType.getName()))
		);
	}

	protected BlockCommand(ModdedCrowdControlPlugin plugin, Block blockType, String effectName, Component displayName) {
		super(plugin);
		this.blockType = blockType;
		this.effectName = effectName;
		this.displayName = displayName;
	}

	@Override
	public @NotNull FeatureFlagSet requiredFeatures() {
		return blockType.requiredFeatures();
	}

	@Nullable
	protected Location getLocation(ServerPlayer player) {
		Location location = new Location(player);
		if (!BlockFinder.isReplaceable(location))
			return null;
		return location;
	}

	@Override
	public void execute(@NotNull Supplier<@NotNull List<@NotNull ServerPlayer>> playerSupplier, @NotNull PublicEffectPayload request, @NotNull CCPlayer ccPlayer) {
		ccPlayer.sendResponse(ThreadUtil.waitForSuccess(request, () -> {
			boolean success = false;
			for (ServerPlayer player : playerSupplier.get()) {
				Location location = getLocation(player);
				if (location == null)
					continue;
				BlockState currentBlock = location.block();
				Block currentType = currentBlock.getBlock();
				if (BlockFinder.isReplaceable(currentBlock) && !currentType.equals(blockType)) {
					success = true;
					sync(() -> location.block(blockType.defaultBlockState()));
				}
			}
			return success
				? new CCInstantEffectResponse(request.getRequestId(), ResponseStatus.SUCCESS)
				: new CCInstantEffectResponse(request.getRequestId(), ResponseStatus.FAIL_TEMPORARY, "No available locations to set blocks");
		}));
	}
}
