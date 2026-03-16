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

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static dev.qixils.crowdcontrol.common.command.CommandConstants.*;
import static dev.qixils.crowdcontrol.plugin.fabric.utils.AttributeUtil.addModifier;
import static dev.qixils.crowdcontrol.plugin.fabric.utils.AttributeUtil.removeModifier;

@Getter
public class PlayerSizeCommand extends ModdedCommand implements CCTimedEffect {
	private final Map<UUID, Set<UUID>> idMap = new HashMap<>();
	private final String effectName;
	private final double level;

	private final String effectGroup = "gravity";
	private final List<String> effectGroups = Collections.singletonList(effectGroup);

	public PlayerSizeCommand(ModdedCrowdControlPlugin plugin, String effectName, double level) {
		super(plugin);
		this.effectName = effectName;
		this.level = level;
	}

	@Override
	public void execute(@NotNull Supplier<@NotNull List<@NotNull ServerPlayer>> playerSupplier, @NotNull PublicEffectPayload request, @NotNull CCPlayer ccPlayer) {
		ccPlayer.sendResponse(ThreadUtil.waitForSuccess(request, () -> {
			if (isArrayActive(ccPlayer))
				return new CCInstantEffectResponse(request.getRequestId(), ResponseStatus.FAIL_TEMPORARY, "Conflicting effects active");
			List<ServerPlayer> players = playerSupplier.get();
			idMap.put(request.getRequestId(), players.stream().map(ServerPlayer::getUUID).collect(Collectors.toSet()));
			for (ServerPlayer player : players) {
				addModifier(player, Attributes.SCALE, SCALE_MODIFIER_UUID, level, AttributeModifier.Operation.ADD_MULTIPLIED_BASE, false);
				addModifier(player, Attributes.STEP_HEIGHT, SCALE_STEP_MODIFIER_UUID, level, AttributeModifier.Operation.ADD_MULTIPLIED_BASE, false);
				addModifier(player, Attributes.JUMP_STRENGTH, SCALE_JUMP_MODIFIER_UUID, level, AttributeModifier.Operation.ADD_MULTIPLIED_BASE, false);
				addModifier(player, Attributes.SAFE_FALL_DISTANCE, FALL_MODIFIER_UUID, level, AttributeModifier.Operation.ADD_MULTIPLIED_BASE, false);
			}
			return new CCTimedEffectResponse(request.getRequestId(), ResponseStatus.TIMED_BEGIN, request.getEffect().getDurationMillis());
		}));
	}

	@Override
	public void onEnd(@NotNull PublicEffectPayload request, @NotNull CCPlayer source) {
		plugin.toPlayerStream(idMap.remove(request.getRequestId())).forEach(player -> {
			removeModifier(player, Attributes.SCALE, SCALE_MODIFIER_UUID);
			removeModifier(player, Attributes.STEP_HEIGHT, SCALE_STEP_MODIFIER_UUID);
			removeModifier(player, Attributes.JUMP_STRENGTH, SCALE_JUMP_MODIFIER_UUID);
			removeModifier(player, Attributes.SAFE_FALL_DISTANCE, FALL_MODIFIER_UUID);
		});
	}

	public static PlayerSizeCommand increase(ModdedCrowdControlPlugin plugin) {
		return new PlayerSizeCommand(plugin, "player_size_double", 1);
	}

	public static PlayerSizeCommand decrease(ModdedCrowdControlPlugin plugin) {
		return new PlayerSizeCommand(plugin, "player_size_halve", -0.5);
	}
}
