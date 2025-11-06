package dev.qixils.crowdcontrol.plugin.paper.commands;

import dev.qixils.crowdcontrol.common.util.ThreadUtil;
import dev.qixils.crowdcontrol.plugin.paper.PaperCommand;
import dev.qixils.crowdcontrol.plugin.paper.PaperCrowdControlPlugin;
import live.crowdcontrol.cc4j.CCPlayer;
import live.crowdcontrol.cc4j.CCTimedEffect;
import live.crowdcontrol.cc4j.websocket.data.CCInstantEffectResponse;
import live.crowdcontrol.cc4j.websocket.data.CCTimedEffectResponse;
import live.crowdcontrol.cc4j.websocket.data.ResponseStatus;
import live.crowdcontrol.cc4j.websocket.payload.PublicEffectPayload;
import lombok.Getter;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.*;
import java.util.function.Supplier;

import static dev.qixils.crowdcontrol.common.command.CommandConstants.*;
import static dev.qixils.crowdcontrol.plugin.paper.utils.AttributeUtil.addModifier;
import static dev.qixils.crowdcontrol.plugin.paper.utils.AttributeUtil.removeModifier;
import static dev.qixils.crowdcontrol.plugin.paper.utils.PaperUtil.toPlayers;

@Getter
public class PlayerSizeCommand extends PaperCommand implements CCTimedEffect {
	private final Duration defaultDuration = Duration.ofSeconds(30);
	private final String effectName;

	private final double level;

	private final Map<UUID, List<UUID>> idMap = new HashMap<>();
	private final String effectGroup = "player_size";
	private final List<String> effectGroups = Collections.singletonList(effectGroup);

	public PlayerSizeCommand(PaperCrowdControlPlugin plugin, String effectName, double level) {
		super(plugin);
		this.effectName = effectName;
		this.level = level;
	}

	@Override
	public void execute(@NotNull Supplier<List<Player>> playerSupplier, @NotNull PublicEffectPayload request, @NotNull CCPlayer ccPlayer) {
		ccPlayer.sendResponse(ThreadUtil.waitForSuccess(request, () -> {
			if (isActive(ccPlayer, "freeze", effectGroup))
				return new CCInstantEffectResponse(request.getRequestId(), ResponseStatus.FAIL_TEMPORARY, "Conflicting effects active");
			List<Player> players = playerSupplier.get();
			for (Player player : players) {
				player.getScheduler().run(plugin.getPaperPlugin(), $ -> {
					addModifier(player, Attribute.SCALE, SCALE_MODIFIER_UUID, SCALE_MODIFIER_NAME, level, AttributeModifier.Operation.ADD_SCALAR, false);
					addModifier(player, Attribute.STEP_HEIGHT, SCALE_STEP_MODIFIER_UUID, SCALE_STEP_MODIFIER_NAME, level, AttributeModifier.Operation.ADD_SCALAR, false);
					addModifier(player, Attribute.JUMP_STRENGTH, SCALE_JUMP_MODIFIER_UUID, SCALE_JUMP_MODIFIER_NAME, level, AttributeModifier.Operation.ADD_SCALAR, false);
					addModifier(player, Attribute.SAFE_FALL_DISTANCE, FALL_MODIFIER_UUID, FALL_MODIFIER_NAME, level, AttributeModifier.Operation.ADD_SCALAR, false);
				}, null);
			}
			idMap.put(request.getRequestId(), players.stream().map(Player::getUniqueId).toList());
			return new CCTimedEffectResponse(request.getRequestId(), ResponseStatus.TIMED_BEGIN, request.getEffect().getDurationMillis());
		}));
	}

	@Override
	public void onEnd(@NotNull PublicEffectPayload request, @NotNull CCPlayer source) {
		for (Player player : toPlayers(idMap.remove(request.getRequestId()))) {
			player.getScheduler().run(plugin.getPaperPlugin(), $ -> {
				removeModifier(player, Attribute.SCALE, SCALE_MODIFIER_UUID);
				removeModifier(player, Attribute.STEP_HEIGHT, SCALE_STEP_MODIFIER_UUID);
				removeModifier(player, Attribute.JUMP_STRENGTH, SCALE_JUMP_MODIFIER_UUID);
				removeModifier(player, Attribute.SAFE_FALL_DISTANCE, FALL_MODIFIER_UUID);
			}, null);
		}
	}

	public static PlayerSizeCommand increase(PaperCrowdControlPlugin plugin) {
		return new PlayerSizeCommand(plugin, "player_size_double", 1);
	}

	public static PlayerSizeCommand decrease(PaperCrowdControlPlugin plugin) {
		return new PlayerSizeCommand(plugin, "player_size_halve", -0.5);
	}
}
