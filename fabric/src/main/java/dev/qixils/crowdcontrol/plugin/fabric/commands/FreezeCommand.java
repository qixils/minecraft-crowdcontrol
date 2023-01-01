package dev.qixils.crowdcontrol.plugin.fabric.commands;

import dev.qixils.crowdcontrol.TimedEffect;
import dev.qixils.crowdcontrol.plugin.fabric.FabricCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.fabric.TimedVoidCommand;
import dev.qixils.crowdcontrol.plugin.fabric.interfaces.Components;
import dev.qixils.crowdcontrol.plugin.fabric.interfaces.MovementStatus;
import dev.qixils.crowdcontrol.plugin.fabric.utils.Location;
import dev.qixils.crowdcontrol.socket.Request;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;

import static dev.qixils.crowdcontrol.common.command.CommandConstants.FREEZE_DURATION;

@Getter
public final class FreezeCommand extends TimedVoidCommand {
	public static final Map<UUID, FreezeData> DATA = new HashMap<>();

	private final String effectName;
	private final LocationModifier modifier;
	private final MovementStatus.Type freezeType;

	public FreezeCommand(FabricCrowdControlPlugin plugin, String effectName, LocationModifier modifier, MovementStatus.Type freezeType) {
		super(plugin);
		this.effectName = effectName;
		this.modifier = modifier;
		this.freezeType = freezeType;
	}

	public @NotNull Duration getDefaultDuration() {
		return FREEZE_DURATION;
	}

	@Override
	public void voidExecute(@NotNull List<@NotNull ServerPlayer> ignored, @NotNull Request request) {
		AtomicReference<Map<UUID, FreezeData>> atomic = new AtomicReference<>();
		new TimedEffect.Builder()
				.request(request)
				.duration(getDuration(request))
				.startCallback($ -> {
					List<ServerPlayer> players = getPlugin().getPlayers(request);
					Map<UUID, FreezeData> locations = new HashMap<>();
					players.forEach(player -> {
						locations.put(player.getUUID(), new FreezeData(modifier, new Location(player)));
						Components.MOVEMENT_STATUS.get(player).setProhibited(freezeType, true);
					});
					atomic.set(locations);
					DATA.putAll(locations);
					playerAnnounce(players, request);
					return null;
				})
				.completionCallback($ -> atomic.get().forEach((uuid, data) -> {
					DATA.remove(uuid, data);
					MinecraftServer server = getPlugin().getServer();
					if (server == null)
						return;
					ServerPlayer player = server.getPlayerList().getPlayer(uuid);
					if (player == null)
						return;
					Components.MOVEMENT_STATUS.get(player).setProhibited(freezeType, false);
				}))
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

	public static FreezeCommand feet(FabricCrowdControlPlugin plugin) {
		return new FreezeCommand(plugin, "freeze", (newLocation, previousLocation) -> previousLocation.withRotationOf(newLocation), MovementStatus.Type.WALK);
	}

	public static FreezeCommand camera(FabricCrowdControlPlugin plugin) {
		return new FreezeCommand(plugin, "camera_lock", Location::withRotationOf, MovementStatus.Type.LOOK); // (cur, prev) -> cur.withRotationOf(prev)
	}

	// TODO: allow clients to look left and right when sky/ground lock is enabled

	public static FreezeCommand skyCamera(FabricCrowdControlPlugin plugin) {
		return new FreezeCommand(plugin, "camera_lock_to_sky", (cur, prev) -> cur.withRotation(prev.yaw(), -90), MovementStatus.Type.LOOK);
	}

	public static FreezeCommand groundCamera(FabricCrowdControlPlugin plugin) {
		return new FreezeCommand(plugin, "camera_lock_to_ground", (cur, prev) -> cur.withRotation(prev.yaw(), 90), MovementStatus.Type.LOOK);
	}
}
