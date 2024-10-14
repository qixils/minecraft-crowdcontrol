package dev.qixils.crowdcontrol.plugin.paper.commands;

import dev.qixils.crowdcontrol.TimedEffect;
import dev.qixils.crowdcontrol.plugin.paper.PaperCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.paper.VoidCommand;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.List;

import static dev.qixils.crowdcontrol.common.command.CommandConstants.*;
import static dev.qixils.crowdcontrol.plugin.paper.utils.AttributeUtil.addModifier;
import static dev.qixils.crowdcontrol.plugin.paper.utils.AttributeUtil.removeModifier;

@Getter
public class PlayerSizeCommand extends VoidCommand implements TimedCommand<Player> {
	private final Duration defaultDuration = Duration.ofSeconds(30);
	private final String effectName;

	private final double level;

	public PlayerSizeCommand(PaperCrowdControlPlugin plugin, String effectName, double level) {
		super(plugin);
		this.effectName = effectName;
		this.level = level;
	}

	@Override
	public void voidExecute(@NotNull List<@NotNull Player> players, @NotNull Request request) {
		// atomic reference stuff is dumb
		new TimedEffect.Builder()
			.request(request)
			.effectGroup("gravity") // has some overlapping attributes
			.duration(getDuration(request))
			.startCallback(effect -> {
				for (Player player : players) {
					player.getScheduler().run(plugin, $ -> {
						addModifier(player, Attribute.GENERIC_SCALE, SCALE_MODIFIER_UUID, SCALE_MODIFIER_NAME, level, AttributeModifier.Operation.ADD_SCALAR, false);
						addModifier(player, Attribute.GENERIC_STEP_HEIGHT, SCALE_STEP_MODIFIER_UUID, SCALE_STEP_MODIFIER_NAME, level, AttributeModifier.Operation.ADD_SCALAR, false);
						addModifier(player, Attribute.GENERIC_JUMP_STRENGTH, SCALE_JUMP_MODIFIER_UUID, SCALE_JUMP_MODIFIER_NAME, level, AttributeModifier.Operation.ADD_SCALAR, false);
						addModifier(player, Attribute.GENERIC_SAFE_FALL_DISTANCE, FALL_MODIFIER_UUID, FALL_MODIFIER_NAME, level, AttributeModifier.Operation.ADD_SCALAR, false);
					}, null);
				}
				playerAnnounce(players, request);
				return request.buildResponse().type(Response.ResultType.SUCCESS);
			})
			.completionCallback(effect -> {
				for (Player player : players) {
					player.getScheduler().run(plugin, $ -> {
						removeModifier(player, Attribute.GENERIC_SCALE, SCALE_MODIFIER_UUID);
						removeModifier(player, Attribute.GENERIC_STEP_HEIGHT, SCALE_STEP_MODIFIER_UUID);
						removeModifier(player, Attribute.GENERIC_JUMP_STRENGTH, SCALE_JUMP_MODIFIER_UUID);
						removeModifier(player, Attribute.GENERIC_SAFE_FALL_DISTANCE, FALL_MODIFIER_UUID);
					}, null);
				}
			})
			.build().queue();
	}

	public static PlayerSizeCommand increase(PaperCrowdControlPlugin plugin) {
		return new PlayerSizeCommand(plugin, "player_size_double", 1);
	}

	public static PlayerSizeCommand decrease(PaperCrowdControlPlugin plugin) {
		return new PlayerSizeCommand(plugin, "player_size_halve", -0.5);
	}
}
