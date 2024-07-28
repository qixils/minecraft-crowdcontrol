package dev.qixils.crowdcontrol.plugin.fabric.commands;

import dev.qixils.crowdcontrol.TimedEffect;
import dev.qixils.crowdcontrol.common.EventListener;
import dev.qixils.crowdcontrol.common.components.MovementStatusType;
import dev.qixils.crowdcontrol.common.components.MovementStatusValue;
import dev.qixils.crowdcontrol.plugin.fabric.FabricCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.fabric.TimedVoidCommand;
import dev.qixils.crowdcontrol.plugin.fabric.event.Death;
import dev.qixils.crowdcontrol.plugin.fabric.event.Listener;
import dev.qixils.crowdcontrol.plugin.fabric.utils.Location;
import dev.qixils.crowdcontrol.socket.Request;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;

import static dev.qixils.crowdcontrol.common.command.CommandConstants.FREEZE_DURATION;

@Getter
public final class FreezeCommand extends TimedVoidCommand {
	public static final Map<UUID, List<FreezeData>> DATA = new HashMap<>();
	private static final Map<UUID, TimedEffect> TIMED_EFFECTS = new HashMap<>();

	private final String effectName;
	private final String effectGroup;
	private final LocationModifier modifier;
	private final MovementStatusType freezeType;
	private final MovementStatusValue freezeValue;

	public FreezeCommand(FabricCrowdControlPlugin plugin, String effectName, String effectGroup, LocationModifier modifier, MovementStatusType freezeType, MovementStatusValue freezeValue) {
		super(plugin);
		this.effectName = effectName;
		this.effectGroup = effectGroup;
		this.modifier = modifier;
		this.freezeType = freezeType;
		this.freezeValue = freezeValue;
	}

	public FreezeCommand(FabricCrowdControlPlugin plugin, String effectName, LocationModifier modifier, MovementStatusType freezeType, MovementStatusValue freezeValue) {
		this(plugin, effectName, effectName, modifier, freezeType, freezeValue);
	}

	public FreezeCommand(FabricCrowdControlPlugin plugin, String effectName, String effectGroup, LocationModifier modifier, MovementStatusType freezeType) {
		this(plugin, effectName, effectGroup, modifier, freezeType, MovementStatusValue.DENIED);
	}

	public FreezeCommand(FabricCrowdControlPlugin plugin, String effectName, LocationModifier modifier, MovementStatusType freezeType) {
		this(plugin, effectName, effectName, modifier, freezeType);
	}

	public @NotNull Duration getDefaultDuration() {
		return FREEZE_DURATION;
	}

	@Override
	public void voidExecute(@NotNull List<@NotNull ServerPlayer> ignored, @NotNull Request request) {
		AtomicReference<Map<UUID, FreezeData>> atomic = new AtomicReference<>();
		new TimedEffect.Builder()
				.request(request)
				.effectGroup(effectGroup)
				.duration(getDuration(request))
				.startCallback(timedEffect -> {
					List<ServerPlayer> players = getPlugin().getPlayers(request);
					Map<UUID, FreezeData> locations = new HashMap<>();
					players.forEach(player -> {
						UUID uuid = player.getUUID();
						TIMED_EFFECTS.put(uuid, timedEffect);
						locations.put(uuid, new FreezeData(modifier, new Location(player)));
						player.cc$setMovementStatus(freezeType, freezeValue);
					});
					atomic.set(locations);
					locations.forEach((uuid, data) -> DATA.computeIfAbsent(uuid, $2 -> new ArrayList<>()).add(data));
					playerAnnounce(players, request);
					return null;
				})
				.completionCallback($ -> atomic.get().forEach((uuid, data) -> {
					TIMED_EFFECTS.remove(uuid);
					DATA.get(uuid).remove(data);
					MinecraftServer server = getPlugin().getServer();
					if (server == null)
						return;
					ServerPlayer player = server.getPlayerList().getPlayer(uuid);
					if (player == null)
						return;
					player.cc$setMovementStatus(freezeType, MovementStatusValue.ALLOWED);
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
		// TODO: smoother client-side freeze (stop mid-air jitter)
		return new FreezeCommand(plugin, "freeze", "walk", (newLocation, previousLocation) -> previousLocation.withRotationOf(newLocation), MovementStatusType.WALK);
	}

	public static FreezeCommand camera(FabricCrowdControlPlugin plugin) {
		return new FreezeCommand(plugin, "camera_lock", "look", Location::withRotationOf, MovementStatusType.LOOK); // (cur, prev) -> cur.withRotationOf(prev)
	}

	public static FreezeCommand skyCamera(FabricCrowdControlPlugin plugin) {
		return new FreezeCommand(plugin, "camera_lock_to_sky", "look", (cur, prev) -> cur.withRotation(cur.yaw(), -90), MovementStatusType.LOOK, MovementStatusValue.PARTIAL);
	}

	public static FreezeCommand groundCamera(FabricCrowdControlPlugin plugin) {
		return new FreezeCommand(plugin, "camera_lock_to_ground", "look", (cur, prev) -> cur.withRotation(cur.yaw(), 90), MovementStatusType.LOOK, MovementStatusValue.PARTIAL);
	}

	@EventListener
	@RequiredArgsConstructor
	public static final class Manager {
		private final FabricCrowdControlPlugin plugin;

		@Listener
		public void onDeath(Death death) {
			UUID uuid = death.entity().getUUID();
			TimedEffect effect = TIMED_EFFECTS.get(uuid);
			if (effect == null) return;

			try {
				effect.complete();
			} catch (Exception e) {
				plugin.getSLF4JLogger().warn("Failed to stop freeze effect", e);
			}
		}
	}
}
