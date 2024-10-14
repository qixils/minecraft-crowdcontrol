package dev.qixils.crowdcontrol.plugin.sponge7.commands;

import dev.qixils.crowdcontrol.plugin.sponge7.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.sponge7.SpongeCrowdControlPlugin;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.data.manipulator.mutable.entity.RespawnLocationData;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.util.RespawnLocation;
import org.spongepowered.api.world.DimensionTypes;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Getter
public class RespawnCommand extends ImmediateCommand {
	private final String effectName = "respawn";

	public RespawnCommand(SpongeCrowdControlPlugin plugin) {
		super(plugin);
	}

	@NotNull
	@Override
	public Response.Builder executeImmediately(@NotNull List<@NotNull Player> players, @NotNull Request request) {
		if (isActive("walk", request) || isActive("look", request))
			return request.buildResponse().type(Response.ResultType.RETRY).message("Cannot fling while frozen");
		// TODO: this is always going to the world spawn, not the player's bed spawn
		sync(() -> {
			for (Player player : players) {
				Optional<RespawnLocationData> optionalData = player.get(RespawnLocationData.class);
				if (!optionalData.isPresent()) {
					player.setLocationSafely(getDefaultSpawn());
					continue;
				}
				Map<UUID, RespawnLocation> data = optionalData.get().respawnLocation().get();
				RespawnLocation location = data.get(player.getWorldUniqueId().orElse(null));
				if ((location == null || !location.asLocation().isPresent()) && !data.isEmpty()) {
					for (RespawnLocation curLocation : data.values()) {
						if (curLocation.asLocation().isPresent()) {
							location = curLocation;
							break;
						}
					}
				}
				Location<World> asLocation;
				if (location == null) {
					asLocation = getDefaultSpawn();
				} else {
					asLocation = location.asLocation().get();
				}
				player.setLocationSafely(asLocation);
			}
		});
		return request.buildResponse().type(Response.ResultType.SUCCESS);
	}

	private Location<World> getDefaultSpawn() {
		return getDefaultWorld().getSpawnLocation();
	}

	private World getDefaultWorld() {
		Optional<World> world = plugin.getGame().getServer().getWorld("world");
		if (!world.isPresent()) {
			for (World iworld : plugin.getGame().getServer().getWorlds()) {
				if (iworld.getDimension().getType().equals(DimensionTypes.OVERWORLD)) {
					world = Optional.of(iworld);
					break;
				}
			}
			if (!world.isPresent())
				world = Optional.of(plugin.getGame().getServer().getWorlds().iterator().next());
		}
		return world.orElseThrow(() -> new IllegalStateException("Couldn't find an overworld world"));
	}
}
