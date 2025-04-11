package dev.qixils.crowdcontrol.plugin.fabric.commands;

import dev.qixils.crowdcontrol.common.EventListener;
import dev.qixils.crowdcontrol.common.components.MovementStatusType;
import dev.qixils.crowdcontrol.common.components.MovementStatusValue;
import dev.qixils.crowdcontrol.common.util.ThreadUtil;
import dev.qixils.crowdcontrol.plugin.fabric.ModdedCommand;
import dev.qixils.crowdcontrol.plugin.fabric.ModdedCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.fabric.event.Death;
import dev.qixils.crowdcontrol.plugin.fabric.event.Listener;
import dev.qixils.crowdcontrol.plugin.fabric.utils.Location;
import live.crowdcontrol.cc4j.CCPlayer;
import live.crowdcontrol.cc4j.CCTimedEffect;
import live.crowdcontrol.cc4j.CrowdControl;
import live.crowdcontrol.cc4j.websocket.data.CCInstantEffectResponse;
import live.crowdcontrol.cc4j.websocket.data.CCTimedEffectResponse;
import live.crowdcontrol.cc4j.websocket.data.ResponseStatus;
import live.crowdcontrol.cc4j.websocket.payload.PublicEffectPayload;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Getter
public final class FreezeCommand extends ModdedCommand implements CCTimedEffect {
	public static final Map<UUID, List<FreezeData>> DATA = new HashMap<>();
	private static final Map<UUID, Map<UUID, FreezeData>> TIMED_EFFECTS = new HashMap<>();

	private final String effectName;
	private final String effectGroup;
	private final List<String> effectGroups;
	private final LocationModifier modifier;
	private final MovementStatusType freezeType;
	private final MovementStatusValue freezeValue;

	public FreezeCommand(ModdedCrowdControlPlugin plugin, String effectName, String effectGroup, LocationModifier modifier, MovementStatusType freezeType, MovementStatusValue freezeValue) {
		super(plugin);
		this.effectName = effectName;
		this.effectGroup = effectGroup;
		this.effectGroups = Collections.singletonList(effectGroup);
		this.modifier = modifier;
		this.freezeType = freezeType;
		this.freezeValue = freezeValue;
	}

	public FreezeCommand(ModdedCrowdControlPlugin plugin, String effectName, LocationModifier modifier, MovementStatusType freezeType, MovementStatusValue freezeValue) {
		this(plugin, effectName, effectName, modifier, freezeType, freezeValue);
	}

	public FreezeCommand(ModdedCrowdControlPlugin plugin, String effectName, String effectGroup, LocationModifier modifier, MovementStatusType freezeType) {
		this(plugin, effectName, effectGroup, modifier, freezeType, MovementStatusValue.DENIED);
	}

	public FreezeCommand(ModdedCrowdControlPlugin plugin, String effectName, LocationModifier modifier, MovementStatusType freezeType) {
		this(plugin, effectName, effectName, modifier, freezeType);
	}

	@Override
	public void execute(@NotNull Supplier<@NotNull List<@NotNull ServerPlayer>> playerSupplier, @NotNull PublicEffectPayload request, @NotNull CCPlayer ccPlayer) {
		ccPlayer.sendResponse(ThreadUtil.waitForSuccess(() -> {
			if (isActive(ccPlayer, getEffectArray()))
				return new CCInstantEffectResponse(request.getRequestId(), ResponseStatus.FAIL_TEMPORARY, "Conflicting effects active");
			List<ServerPlayer> players = playerSupplier.get();
			Map<UUID, FreezeData> locations = new HashMap<>();
			players.forEach(player -> {
				UUID uuid = player.getUUID();
				FreezeData data = new FreezeData(modifier, new Location(player));
				locations.put(uuid, data);
				DATA.computeIfAbsent(uuid, $2 -> new ArrayList<>()).add(data);
				player.cc$setMovementStatus(freezeType, freezeValue);
			});
			TIMED_EFFECTS.put(request.getRequestId(), locations);
			return new CCTimedEffectResponse(request.getRequestId(), ResponseStatus.TIMED_BEGIN, request.getEffect().getDuration() * 1000L);
		}));
	}

	@Override
	public void onEnd(@NotNull PublicEffectPayload request, @NotNull CCPlayer source) {
		Map<UUID, FreezeData> locations = TIMED_EFFECTS.remove(request.getRequestId());
		if (locations == null) return;
		MinecraftServer server = getPlugin().getServer();
		if (server == null)
			return;
		locations.forEach((uuid, data) -> {
			DATA.get(uuid).remove(data);
			ServerPlayer player = server.getPlayerList().getPlayer(uuid);
			if (player == null)
				return;
			player.cc$setMovementStatus(freezeType, MovementStatusValue.ALLOWED);
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

	public static FreezeCommand feet(ModdedCrowdControlPlugin plugin) {
		// TODO: smoother client-side freeze (stop mid-air jitter)
		return new FreezeCommand(plugin, "freeze", "walk", (newLocation, previousLocation) -> previousLocation.withRotationOf(newLocation), MovementStatusType.WALK);
	}

	public static FreezeCommand camera(ModdedCrowdControlPlugin plugin) {
		return new FreezeCommand(plugin, "camera_lock", "look", Location::withRotationOf, MovementStatusType.LOOK); // (cur, prev) -> cur.withRotationOf(prev)
	}

	public static FreezeCommand skyCamera(ModdedCrowdControlPlugin plugin) {
		return new FreezeCommand(plugin, "camera_lock_to_sky", "look", (cur, prev) -> cur.withRotation(cur.yaw(), -90), MovementStatusType.LOOK, MovementStatusValue.PARTIAL);
	}

	public static FreezeCommand groundCamera(ModdedCrowdControlPlugin plugin) {
		return new FreezeCommand(plugin, "camera_lock_to_ground", "look", (cur, prev) -> cur.withRotation(cur.yaw(), 90), MovementStatusType.LOOK, MovementStatusValue.PARTIAL);
	}

	@EventListener
	@RequiredArgsConstructor
	public static final class Manager {
		private final ModdedCrowdControlPlugin plugin;

		@Listener
		public void onDeath(Death death) {
			CrowdControl cc = plugin.getCrowdControl();
			if (cc == null) return;

			UUID uuid = death.entity().getUUID();
			Set<UUID> requestIds = TIMED_EFFECTS.entrySet().stream()
				.filter(entry -> entry.getValue().containsKey(uuid))
				.map(Map.Entry::getKey)
				.collect(Collectors.toSet());
			requestIds.forEach(cc::cancelByRequestId);
		}
	}
}
