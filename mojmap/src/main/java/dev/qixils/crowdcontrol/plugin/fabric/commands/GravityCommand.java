package dev.qixils.crowdcontrol.plugin.fabric.commands;

import dev.qixils.crowdcontrol.common.util.ThreadUtil;
import dev.qixils.crowdcontrol.plugin.fabric.ModdedCommand;
import dev.qixils.crowdcontrol.plugin.fabric.ModdedCrowdControlPlugin;
import live.crowdcontrol.cc4j.CCPlayer;
import live.crowdcontrol.cc4j.CCTimedEffect;
import live.crowdcontrol.cc4j.websocket.data.CCInstantEffectResponse;
import live.crowdcontrol.cc4j.websocket.data.CCTimedEffectResponse;
import live.crowdcontrol.cc4j.websocket.data.ResponseStatus;
import live.crowdcontrol.cc4j.websocket.payload.PublicEffectPayload;
import lombok.Getter;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.*;
import java.util.function.Supplier;

import static dev.qixils.crowdcontrol.common.command.CommandConstants.*;
import static dev.qixils.crowdcontrol.plugin.fabric.utils.AttributeUtil.addModifier;
import static dev.qixils.crowdcontrol.plugin.fabric.utils.AttributeUtil.removeModifier;

@Getter
public class GravityCommand extends ModdedCommand implements CCTimedEffect {
	private final Duration defaultDuration = POTION_DURATION;
	private final String effectName;

	private final double gravityLevel;
	private final double fallLevel;
	private final double fallDmgLevel;

	private final List<String> effectGroups = Arrays.asList("gravity", "walk");
	private final Map<UUID, List<UUID>> idMap = new HashMap<>();

	private GravityCommand(ModdedCrowdControlPlugin plugin, String effectName, double gravityLevel, double fallLevel, double fallDmgLevel) {
		super(plugin);
		this.effectName = effectName;

		this.gravityLevel = gravityLevel;
		this.fallLevel = fallLevel;
		this.fallDmgLevel = fallDmgLevel;
	}

	@Override
	public void execute(@NotNull Supplier<@NotNull List<@NotNull ServerPlayer>> playerSupplier, @NotNull PublicEffectPayload request, @NotNull CCPlayer ccPlayer) {
		ccPlayer.sendResponse(ThreadUtil.waitForSuccess(request, () -> {
			if (isActive(ccPlayer, getEffectArray())) {
				return new CCInstantEffectResponse(request.getRequestId(), ResponseStatus.FAIL_TEMPORARY, "Cannot change gravity while frozen");
			}
			List<ServerPlayer> players = playerSupplier.get();
			idMap.put(request.getRequestId(), players.stream().map(ServerPlayer::getUUID).toList());
			for (ServerPlayer player : players) {
				addModifier(player, Attributes.GRAVITY, GRAVITY_MODIFIER_UUID, gravityLevel, AttributeModifier.Operation.ADD_MULTIPLIED_BASE, false);
				addModifier(player, Attributes.SAFE_FALL_DISTANCE, FALL_MODIFIER_UUID, fallLevel, AttributeModifier.Operation.ADD_MULTIPLIED_BASE, false);
				addModifier(player, Attributes.FALL_DAMAGE_MULTIPLIER, FALL_DMG_MODIFIER_UUID, fallDmgLevel, AttributeModifier.Operation.ADD_VALUE, false);
			}
			return new CCTimedEffectResponse(request.getRequestId(), ResponseStatus.TIMED_BEGIN, request.getEffect().getDuration() * 1000L);
		}));
	}

	@Override
	public void onEnd(@NotNull PublicEffectPayload request, @NotNull CCPlayer source) {
		for (ServerPlayer player : plugin.toPlayerList(idMap.remove(request.getRequestId()))) {
			removeModifier(player, Attributes.GRAVITY, GRAVITY_MODIFIER_UUID);
			removeModifier(player, Attributes.SAFE_FALL_DISTANCE, FALL_MODIFIER_UUID);
		}
	}

	@NotNull
	public static GravityCommand zero(ModdedCrowdControlPlugin plugin) {
		return new GravityCommand(plugin, "zero_gravity", -1, 0, 0);
	}

	@NotNull
	public static GravityCommand low(ModdedCrowdControlPlugin plugin) {
		return new GravityCommand(plugin, "low_gravity", -0.5, 1, -0.5);
	}

	@NotNull
	public static GravityCommand high(ModdedCrowdControlPlugin plugin) {
		return new GravityCommand(plugin, "high_gravity", 1, -0.5, 1);
	}

	@NotNull
	public static GravityCommand maximum(ModdedCrowdControlPlugin plugin) {
		return new GravityCommand(plugin, "maximum_gravity", 3, -1, 3);
	}
}
