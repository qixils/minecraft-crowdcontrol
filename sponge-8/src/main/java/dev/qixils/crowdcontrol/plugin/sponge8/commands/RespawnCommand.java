package dev.qixils.crowdcontrol.plugin.sponge8.commands;

import dev.qixils.crowdcontrol.plugin.sponge8.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.sponge8.SpongeCrowdControlPlugin;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.util.RespawnLocation;
import org.spongepowered.api.world.WorldTypes;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.api.world.server.WorldManager;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Getter
public class RespawnCommand extends ImmediateCommand {
	private final String effectName = "respawn";

	public RespawnCommand(SpongeCrowdControlPlugin plugin) {
		super(plugin);
	}

	@NotNull
	@Override
	public Response.Builder executeImmediately(@NotNull List<@NotNull ServerPlayer> players, @NotNull Request request) {
		sync(() -> {
			for (ServerPlayer player : players) {
				RespawnLocation location = null;
				Optional<Map<ResourceKey, RespawnLocation>> optionalData = player.get(Keys.RESPAWN_LOCATIONS);
				if (optionalData.isPresent()) {
					Map<ResourceKey, RespawnLocation> data = optionalData.get();
					location = data.get(player.world().key());
					if ((location == null || !location.asLocation().isPresent()) && !data.isEmpty()) {
						for (RespawnLocation curLocation : data.values()) {
							if (curLocation.asLocation().isPresent()) {
								location = curLocation;
								break;
							}
						}
					}
				}
				ServerLocation asLocation;
				if (location == null) {
					asLocation = getDefaultSpawn();
				} else {
					asLocation = location.asLocation().get();
				}
				player.setLocation(plugin.getGame().server().teleportHelper().findSafeLocation(asLocation)
						.orElseGet(() -> asLocation.world().worldType().equals(WorldTypes.OVERWORLD.get()) ? asLocation.asHighestLocation() : asLocation));
			}
		});
		return request.buildResponse().type(Response.ResultType.SUCCESS);
	}

	private ServerLocation getDefaultSpawn() {
		// TODO i cannot find a new version of #getSpawnLocation for the life of me
		return getDefaultWorld().location(0, 0, 0).asHighestLocation();
	}

	private ServerWorld getDefaultWorld() {
		WorldManager manager = plugin.getGame().server().worldManager();
		Optional<ServerWorld> world = manager.world(ResourceKey.minecraft("world"));
		if (!world.isPresent()) {
			for (ServerWorld iworld : manager.worlds()) {
				if (iworld.worldType().equals(WorldTypes.OVERWORLD.get())) {
					world = Optional.of(iworld);
					break;
				}
			}
		}
		return world.orElseThrow(() -> new IllegalStateException("Couldn't find an overworld world"));
	}
}
