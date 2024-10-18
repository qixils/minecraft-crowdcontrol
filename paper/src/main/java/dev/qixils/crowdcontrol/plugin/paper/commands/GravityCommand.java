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
public class GravityCommand extends PaperCommand implements CCTimedEffect {
	private final Duration defaultDuration = POTION_DURATION;
	private final String effectName;
	private final String effectGroup = "gravity";
	private final List<String> effectGroups = Collections.singletonList(effectGroup);

	private final Map<UUID, List<UUID>> playerMap = new HashMap<>();
	private final double gravityLevel;
	private final double fallLevel;
	private final double fallDmgLevel;

	private GravityCommand(PaperCrowdControlPlugin plugin, String effectName, double gravityLevel, double fallLevel, double fallDmgLevel) {
		super(plugin);
		this.effectName = effectName;

		this.gravityLevel = gravityLevel;
		this.fallLevel = fallLevel;
		this.fallDmgLevel = fallDmgLevel;
	}

	@Override
	public void execute(@NotNull Supplier<@NotNull List<@NotNull Player>> playerSupplier, @NotNull PublicEffectPayload request, @NotNull CCPlayer ccPlayer) {
		ccPlayer.sendResponse(ThreadUtil.waitForSuccess(() -> {
			List<Player> players = playerSupplier.get();

			if (isActive(ccPlayer, "freeze", effectGroup))
				return new CCInstantEffectResponse(request.getRequestId(), ResponseStatus.FAIL_TEMPORARY, "Conflicting effects active");

			playerMap.put(request.getRequestId(), players.stream().map(Player::getUniqueId).toList());

			players.forEach(player -> player.getScheduler().run(plugin.getPaperPlugin(), $ -> {
				addModifier(player, Attribute.GENERIC_GRAVITY, GRAVITY_MODIFIER_UUID, GRAVITY_MODIFIER_NAME, gravityLevel, AttributeModifier.Operation.ADD_SCALAR, false);
				addModifier(player, Attribute.GENERIC_SAFE_FALL_DISTANCE, FALL_MODIFIER_UUID, FALL_MODIFIER_NAME, fallLevel, AttributeModifier.Operation.ADD_SCALAR, false);
				addModifier(player, Attribute.GENERIC_FALL_DAMAGE_MULTIPLIER, FALL_DMG_MODIFIER_UUID, FALL_DMG_MODIFIER_NAME, fallDmgLevel, AttributeModifier.Operation.ADD_NUMBER, false);
			}, null));

			return new CCTimedEffectResponse(request.getRequestId(), ResponseStatus.TIMED_BEGIN, request.getEffect().getDuration() * 1000L);
		}));
	}

	@Override
	public void onEnd(@NotNull PublicEffectPayload request, @NotNull CCPlayer source) {
		List<Player> players = toPlayers(playerMap.remove(request.getRequestId()));
		players.forEach(player -> player.getScheduler().run(plugin.getPaperPlugin(), $ -> {
			removeModifier(player, Attribute.GENERIC_GRAVITY, GRAVITY_MODIFIER_UUID);
			removeModifier(player, Attribute.GENERIC_SAFE_FALL_DISTANCE, FALL_MODIFIER_UUID);
		}, null));
	}

	@NotNull
	public static GravityCommand zero(PaperCrowdControlPlugin plugin) {
		return new GravityCommand(plugin, "zero_gravity", -1, 0, 0);
	}

	@NotNull
	public static GravityCommand low(PaperCrowdControlPlugin plugin) {
		return new GravityCommand(plugin, "low_gravity", -0.5, 1, -0.5);
	}

	@NotNull
	public static GravityCommand high(PaperCrowdControlPlugin plugin) {
		return new GravityCommand(plugin, "high_gravity", 1, -0.5, 1);
	}

	@NotNull
	public static GravityCommand maximum(PaperCrowdControlPlugin plugin) {
		return new GravityCommand(plugin, "maximum_gravity", 3, -1, 3);
	}
}
