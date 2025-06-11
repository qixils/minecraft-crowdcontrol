package dev.qixils.crowdcontrol.plugin.fabric.commands;

import dev.qixils.crowdcontrol.TriState;
import dev.qixils.crowdcontrol.common.util.ThreadUtil;
import dev.qixils.crowdcontrol.plugin.fabric.ModdedCommand;
import dev.qixils.crowdcontrol.plugin.fabric.ModdedCrowdControlPlugin;
import live.crowdcontrol.cc4j.CCPlayer;
import live.crowdcontrol.cc4j.IUserRecord;
import live.crowdcontrol.cc4j.websocket.data.CCInstantEffectResponse;
import live.crowdcontrol.cc4j.websocket.data.ResponseStatus;
import live.crowdcontrol.cc4j.websocket.payload.PublicEffectPayload;
import lombok.Getter;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.sound.Sound;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.GameRules;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Supplier;

import static dev.qixils.crowdcontrol.common.command.CommandConstants.KEEP_INVENTORY_MESSAGE;
import static dev.qixils.crowdcontrol.common.command.CommandConstants.LOSE_INVENTORY_MESSAGE;
import static dev.qixils.crowdcontrol.common.util.sound.Sounds.KEEP_INVENTORY_ALERT;
import static dev.qixils.crowdcontrol.common.util.sound.Sounds.LOSE_INVENTORY_ALERT;

@Getter
public class KeepInventoryCommand extends ModdedCommand {
	private static final Set<UUID> keepingInventory = Collections.synchronizedSet(new HashSet<>(1));
	public static boolean globalKeepInventory = false;
	private final boolean enable;
	private final String effectName;

	public KeepInventoryCommand(ModdedCrowdControlPlugin plugin, boolean enable) {
		super(plugin);
		this.enable = enable;
		this.effectName = "keep_inventory_" + (enable ? "on" : "off");
	}

	public static boolean isKeepingInventory(UUID player) {
		return globalKeepInventory || keepingInventory.contains(player);
	}

	public static boolean isKeepingInventory(Entity player) {
		return isKeepingInventory(player.getUUID());
	}

	private void alert(List<ServerPlayer> players) {
		Audience audience = plugin.playerMapper().asAudience(players);
		audience.sendActionBar(enable ? KEEP_INVENTORY_MESSAGE : LOSE_INVENTORY_MESSAGE);
		audience.playSound((enable ? KEEP_INVENTORY_ALERT : LOSE_INVENTORY_ALERT).get(), Sound.Emitter.self());
	}

	@Override
	public void execute(@NotNull Supplier<List<ServerPlayer>> playerSupplier, @NotNull PublicEffectPayload request, @NotNull CCPlayer ccPlayer) {
		ccPlayer.sendResponse(ThreadUtil.waitForSuccess(request, () -> {
			List<ServerPlayer> players = playerSupplier.get();
			if (plugin.isGlobal()) {
				if (globalKeepInventory == enable) {
					return new CCInstantEffectResponse(request.getRequestId(), ResponseStatus.FAIL_TEMPORARY, "Keep Inventory is already " + (enable ? "enabled" : "disabled"));
				}
				globalKeepInventory = enable;
				alert(players);
				plugin.updateConditionalEffectVisibility();
				return new CCInstantEffectResponse(request.getRequestId(), ResponseStatus.SUCCESS);
			}

			List<UUID> uuids = new ArrayList<>(players.size());
			for (ServerPlayer player : players)
				uuids.add(player.getUUID());

			if (enable) {
				if (keepingInventory.addAll(uuids)) {
					alert(players);
					players.forEach(plugin::updateConditionalEffectVisibility);
					return new CCInstantEffectResponse(request.getRequestId(), ResponseStatus.SUCCESS);
				} else
					return new CCInstantEffectResponse(request.getRequestId(), ResponseStatus.FAIL_TEMPORARY, "Streamer(s) already have Keep Inventory enabled");
			} else {
				if (keepingInventory.removeAll(uuids)) {
					alert(players);
					players.forEach(plugin::updateConditionalEffectVisibility);
					return new CCInstantEffectResponse(request.getRequestId(), ResponseStatus.SUCCESS);
				} else
					return new CCInstantEffectResponse(request.getRequestId(), ResponseStatus.FAIL_TEMPORARY, "Streamer(s) already have Keep Inventory disabled");
			}
		}));
	}

	@Override
	public TriState isVisible(@NotNull IUserRecord user, @NotNull List<ServerPlayer> potentialPlayers) {
		// Cannot use inventory effects while /gamerule keepInventory true
		return potentialPlayers.stream()
			.anyMatch(player -> player.serverLevel().getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY))
			? TriState.FALSE
			: TriState.TRUE;
	}

	@Override
	public TriState isSelectable(@NotNull IUserRecord user, @NotNull List<ServerPlayer> potentialPlayers) {
		if (plugin.isGlobal())
			return globalKeepInventory == enable ? TriState.FALSE : TriState.TRUE;

		TriState state = potentialPlayers.stream()
			.map(player -> TriState.fromBoolean(enable != isKeepingInventory(player.getUUID())))
			.reduce((prev, next) -> {
				if (prev != next) return TriState.UNKNOWN;
				return prev;
			})
			.orElse(TriState.UNKNOWN);

		if (state == TriState.FALSE) return state;
		return TriState.TRUE; // fixing UNKNOWN essentially
	}

	// management of this command is handled by mixins
}
