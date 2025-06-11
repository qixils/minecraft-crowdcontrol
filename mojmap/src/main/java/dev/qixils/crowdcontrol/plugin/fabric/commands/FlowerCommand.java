package dev.qixils.crowdcontrol.plugin.fabric.commands;

import dev.qixils.crowdcontrol.common.util.MappedKeyedTag;
import dev.qixils.crowdcontrol.common.util.RandomUtil;
import dev.qixils.crowdcontrol.common.util.ThreadUtil;
import dev.qixils.crowdcontrol.plugin.fabric.ModdedCommand;
import dev.qixils.crowdcontrol.plugin.fabric.ModdedCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.fabric.utils.BlockFinder;
import dev.qixils.crowdcontrol.plugin.fabric.utils.Location;
import dev.qixils.crowdcontrol.plugin.fabric.utils.TypedTag;
import live.crowdcontrol.cc4j.CCPlayer;
import live.crowdcontrol.cc4j.websocket.data.CCInstantEffectResponse;
import live.crowdcontrol.cc4j.websocket.data.ResponseStatus;
import live.crowdcontrol.cc4j.websocket.payload.PublicEffectPayload;
import lombok.Getter;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import static dev.qixils.crowdcontrol.common.command.CommandConstants.*;

@Getter
public class FlowerCommand extends ModdedCommand {
	private final String effectName = "flowers";
	private final MappedKeyedTag<Block> flowers = new TypedTag<>(FLOWERS, BuiltInRegistries.BLOCK);

	public FlowerCommand(ModdedCrowdControlPlugin plugin) {
		super(plugin);
	}

	@Override
	public void execute(@NotNull Supplier<@NotNull List<@NotNull ServerPlayer>> playerSupplier, @NotNull PublicEffectPayload request, @NotNull CCPlayer ccPlayer) {
		ccPlayer.sendResponse(ThreadUtil.waitForSuccess(request, () -> {
			List<ServerPlayer> players = playerSupplier.get();
			Set<Location> placeLocations = new HashSet<>(FLOWER_MAX * players.size());
			for (ServerPlayer player : players) {
				BlockFinder finder = BlockFinder.builder()
					.origin(player)
					.maxRadius(FLOWER_RADIUS)
					.locationValidator(location ->
						!placeLocations.contains(location)
							&& BlockFinder.isReplaceable(location)
							&& BlockFinder.isSolid(location.add(0, -1, 0).block()))
					.build();
				Location location = finder.next();
				int placed = 0;
				int toPlace = RandomUtil.nextInclusiveInt(FLOWER_MIN, FLOWER_MAX);
				while (location != null) {
					placeLocations.add(location);
					if (++placed == toPlace)
						break;
					location = finder.next();
				}
			}

			if (placeLocations.isEmpty())
				return new CCInstantEffectResponse(request.getRequestId(), ResponseStatus.FAIL_TEMPORARY, "Could not find a suitable location to place flowers");

			sync(() -> {
				for (Location location : placeLocations) {
					location.block(flowers.getRandom().defaultBlockState());
				}
			});

			return new CCInstantEffectResponse(request.getRequestId(), ResponseStatus.SUCCESS);
		}));
	}
}
