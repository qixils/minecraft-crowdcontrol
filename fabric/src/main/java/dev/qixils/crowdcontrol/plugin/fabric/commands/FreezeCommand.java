package dev.qixils.crowdcontrol.plugin.fabric.commands;

import dev.qixils.crowdcontrol.TimedEffect;
import dev.qixils.crowdcontrol.plugin.fabric.FabricCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.fabric.TimedCommand;
import dev.qixils.crowdcontrol.plugin.fabric.utils.Location;
import dev.qixils.crowdcontrol.socket.Request;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;

import static dev.qixils.crowdcontrol.common.CommandConstants.FREEZE_DURATION;

@Getter
public final class FreezeCommand extends TimedCommand {
	public static final Map<UUID, FreezeData> DATA = new HashMap<>();

	private final String effectName;
	private final String displayName;
	private final LocationModifier modifier;

	public FreezeCommand(FabricCrowdControlPlugin plugin, String effectName, String displayName, LocationModifier modifier) {
		super(plugin);
		this.effectName = effectName;
		this.displayName = displayName;
		this.modifier = modifier;
	}

	public @NotNull Duration getDuration() {
		return FREEZE_DURATION;
	}

	@Override
	public void voidExecute(@NotNull List<@NotNull ServerPlayer> ignored, @NotNull Request request) {
		AtomicReference<Map<UUID, FreezeData>> atomic = new AtomicReference<>();
		new TimedEffect.Builder()
				.request(request)
				.duration(getDuration())
				.startCallback($ -> {
					List<ServerPlayer> players = getPlugin().getPlayers(request);
					Map<UUID, FreezeData> locations = new HashMap<>();
					players.forEach(player -> locations.put(player.getUUID(), new FreezeData(modifier, new Location(player))));
					atomic.set(locations);
					DATA.putAll(locations);
					playerAnnounce(players, request);
					return null;
				})
				.completionCallback($ -> atomic.get().forEach(DATA::remove))
				.build().queue();
	}

	@FunctionalInterface
	public interface LocationModifier extends BiFunction<Location, Location, Location> {
		@Override
		Location apply(@NotNull Location newLocation, @NotNull Location previousLocation);
	}

	@AllArgsConstructor
	public static final class FreezeData {
		public final LocationModifier modifier;
		public Location previousLocation;

		public Location getDestination(Location currentLocation) {
			return modifier.apply(currentLocation, previousLocation);
		}
	}

	public static FreezeCommand createDefault(FabricCrowdControlPlugin plugin) {
		return new FreezeCommand(plugin, "freeze", "Freeze", (newLocation, previousLocation) -> previousLocation.withRotationOf(newLocation));
	}
}
