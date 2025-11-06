package dev.qixils.crowdcontrol.plugin.paper.commands;

import dev.qixils.crowdcontrol.common.components.MovementStatusType;
import dev.qixils.crowdcontrol.common.components.MovementStatusValue;
import dev.qixils.crowdcontrol.common.util.ThreadUtil;
import dev.qixils.crowdcontrol.plugin.paper.PaperCommand;
import dev.qixils.crowdcontrol.plugin.paper.PaperCrowdControlPlugin;
import live.crowdcontrol.cc4j.CCPlayer;
import live.crowdcontrol.cc4j.CCTimedEffect;
import live.crowdcontrol.cc4j.CrowdControl;
import live.crowdcontrol.cc4j.websocket.data.CCTimedEffectResponse;
import live.crowdcontrol.cc4j.websocket.data.ResponseStatus;
import live.crowdcontrol.cc4j.websocket.payload.PublicEffectPayload;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Getter
public class FreezeCommand extends PaperCommand implements CCTimedEffect {
	public static final Map<UUID, List<FreezeData>> DATA = new HashMap<>();
	private static final Map<UUID, Map<UUID, FreezeData>> TIMED_EFFECTS = new HashMap<>();

	private final String effectName;
	private final String effectGroup;
	private final List<String> effectGroups;
	private final LocationModifier modifier;
	private final MovementStatusType freezeType;
	private final MovementStatusValue freezeValue;

	public FreezeCommand(PaperCrowdControlPlugin plugin, String effectName, String effectGroup, LocationModifier modifier, MovementStatusType freezeType, MovementStatusValue freezeValue) {
		super(plugin);
		this.effectName = effectName;
		this.effectGroup = effectGroup;
		this.effectGroups = Collections.singletonList(effectGroup);
		this.modifier = modifier;
		this.freezeType = freezeType;
		this.freezeValue = freezeValue;
	}

	public FreezeCommand(PaperCrowdControlPlugin plugin, String effectName, LocationModifier modifier, MovementStatusType freezeType, MovementStatusValue freezeValue) {
		this(plugin, effectName, effectName, modifier, freezeType, freezeValue);
	}

	public FreezeCommand(PaperCrowdControlPlugin plugin, String effectName, String effectGroup, LocationModifier modifier, MovementStatusType freezeType) {
		this(plugin, effectName, effectGroup, modifier, freezeType, MovementStatusValue.DENIED);
	}

	public FreezeCommand(PaperCrowdControlPlugin plugin, String effectName, LocationModifier modifier, MovementStatusType freezeType) {
		this(plugin, effectName, effectName, modifier, freezeType);
	}

	@Override
	public void execute(@NotNull Supplier<@NotNull List<@NotNull Player>> playerSupplier, @NotNull PublicEffectPayload request, @NotNull CCPlayer ccPlayer) {
		ccPlayer.sendResponse(ThreadUtil.waitForSuccess(request, () -> {
			List<Player> players = playerSupplier.get();
			Map<UUID, FreezeData> locations = new HashMap<>();
			players.forEach(player -> {
				UUID uuid = player.getUniqueId();
				FreezeData data = new FreezeData(modifier, player.getLocation());
				locations.put(uuid, data);
				DATA.computeIfAbsent(uuid, $2 -> new ArrayList<>()).add(data);
				MovementStatusCommand.setValue(plugin, player, freezeType, freezeValue);
			});
			TIMED_EFFECTS.put(request.getRequestId(), locations);
			return new CCTimedEffectResponse(request.getRequestId(), ResponseStatus.TIMED_BEGIN, request.getEffect().getDurationMillis());
		}));
	}

	@Override
	public void onEnd(@NotNull PublicEffectPayload request, @NotNull CCPlayer source) {
		Map<UUID, FreezeData> locations = TIMED_EFFECTS.remove(request.getRequestId());
		if (locations == null) return;
		locations.forEach((uuid, data) -> {
			DATA.get(uuid).remove(data);
			Player player = Bukkit.getPlayer(uuid);
			if (player == null)
				return;
			MovementStatusCommand.setValue(plugin, player, freezeType, MovementStatusValue.ALLOWED);
		});
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

	private static Location withRotation(Location posSource, float yaw, float pitch) {
		return new Location(posSource.getWorld(), posSource.x(), posSource.y(), posSource.z(), yaw, pitch);
	}

	private static Location withRotationOf(Location posSource, Location rotSource) {
		return withRotation(posSource, rotSource.getYaw(), rotSource.getPitch());
	}

	private static Location withPositionOf(Location rotSource, Location posSource) {
		return withRotationOf(posSource, rotSource);
	}

	public static FreezeCommand feet(PaperCrowdControlPlugin plugin) {
		return new FreezeCommand(plugin, "freeze", "walk", FreezeCommand::withPositionOf, MovementStatusType.WALK);
	}

	public static FreezeCommand camera(PaperCrowdControlPlugin plugin) {
		return new FreezeCommand(plugin, "camera_lock", "look", FreezeCommand::withRotationOf, MovementStatusType.LOOK);
	}

	public static FreezeCommand skyCamera(PaperCrowdControlPlugin plugin) {
		return new FreezeCommand(plugin, "camera_lock_to_sky", "look", (cur, prev) -> withRotation(cur, cur.getYaw(), -90), MovementStatusType.LOOK, MovementStatusValue.PARTIAL);
	}

	public static FreezeCommand groundCamera(PaperCrowdControlPlugin plugin) {
		return new FreezeCommand(plugin, "camera_lock_to_ground", "look", (cur, prev) -> withRotation(cur, cur.getYaw(), 90), MovementStatusType.LOOK, MovementStatusValue.PARTIAL);
	}

	public static final class Manager implements Listener {
		private final PaperCrowdControlPlugin plugin;

		public Manager(PaperCrowdControlPlugin plugin) {
			this.plugin = plugin;

			Bukkit.getGlobalRegionScheduler().runAtFixedRate(plugin.getPaperPlugin(), $ -> {
				for (Map.Entry<UUID, List<FreezeData>> entry : DATA.entrySet()) {
					UUID uuid = entry.getKey();
					Player player = Bukkit.getPlayer(uuid);
					if (player == null) continue;
					player.getScheduler().run(plugin.getPaperPlugin(), $$ -> {
						Location cur = player.getLocation();
						Location dest = cur.clone();
						for (FreezeData data : entry.getValue())
							dest = data.getDestination(dest);
						if (!Objects.equals(cur.getWorld(), dest.getWorld()))
							return; // failsafe
						if (!dest.equals(cur))
							player.teleportAsync(dest);
						for (FreezeData data : entry.getValue())
							data.previousLocation = dest;
					}, null);
				}
			}, 1, 1);
		}

		@EventHandler
		public void onDeath(PlayerDeathEvent death) {
			CrowdControl cc = plugin.getCrowdControl();
			if (cc == null) return;

			UUID uuid = death.getPlayer().getUniqueId();
			Set<UUID> requestIds = TIMED_EFFECTS.entrySet().stream()
				.filter(entry -> entry.getValue().containsKey(uuid))
				.map(Map.Entry::getKey)
				.collect(Collectors.toSet());
			requestIds.forEach(cc::cancelByRequestId);
		}

		@EventHandler
		public void onJoin(PlayerJoinEvent event) {
			for (MovementStatusType type : MovementStatusType.values())
				MovementStatusCommand.setValue(plugin, event.getPlayer(), type, MovementStatusValue.ALLOWED);
		}
	}
}
