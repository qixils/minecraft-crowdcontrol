package dev.qixils.crowdcontrol.plugin.fabric.commands;

import dev.qixils.crowdcontrol.TimedEffect;
import dev.qixils.crowdcontrol.plugin.fabric.ModdedCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.fabric.TimedVoidCommand;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.List;

import static dev.qixils.crowdcontrol.common.command.CommandConstants.*;
import static dev.qixils.crowdcontrol.plugin.fabric.utils.AttributeUtil.addModifier;
import static dev.qixils.crowdcontrol.plugin.fabric.utils.AttributeUtil.removeModifier;

@Getter
public class PlayerSizeCommand extends TimedVoidCommand {
	private final Duration defaultDuration = Duration.ofSeconds(30);
	private final String effectName;

	private final double level;

	public PlayerSizeCommand(ModdedCrowdControlPlugin plugin, String effectName, double level) {
		super(plugin);
		this.effectName = effectName;
		this.level = level;
	}

	@Override
	public void voidExecute(@NotNull List<@NotNull ServerPlayer> players, @NotNull Request request) {
		// atomic reference stuff is dumb
		new TimedEffect.Builder()
			.request(request)
			.effectGroup("gravity") // has some overlapping attributes
			.duration(getDuration(request))
			.startCallback(effect -> {
				for (ServerPlayer player : players) {
					addModifier(player, Attributes.SCALE, SCALE_MODIFIER_UUID, level, AttributeModifier.Operation.ADD_MULTIPLIED_BASE, false);
					addModifier(player, Attributes.STEP_HEIGHT, SCALE_STEP_MODIFIER_UUID, level, AttributeModifier.Operation.ADD_MULTIPLIED_BASE, false);
					addModifier(player, Attributes.JUMP_STRENGTH, SCALE_JUMP_MODIFIER_UUID, level, AttributeModifier.Operation.ADD_MULTIPLIED_BASE, false);
					addModifier(player, Attributes.SAFE_FALL_DISTANCE, FALL_MODIFIER_UUID, level, AttributeModifier.Operation.ADD_MULTIPLIED_BASE, false);
				}
				playerAnnounce(players, request);
				return request.buildResponse().type(Response.ResultType.SUCCESS);
			})
			.completionCallback(effect -> {
				for (ServerPlayer player : players) {
					removeModifier(player, Attributes.SCALE, SCALE_MODIFIER_UUID);
					removeModifier(player, Attributes.STEP_HEIGHT, SCALE_STEP_MODIFIER_UUID);
					removeModifier(player, Attributes.JUMP_STRENGTH, SCALE_JUMP_MODIFIER_UUID);
					removeModifier(player, Attributes.SAFE_FALL_DISTANCE, FALL_MODIFIER_UUID);
				}
			})
			.build().queue();
	}

	public static PlayerSizeCommand increase(ModdedCrowdControlPlugin plugin) {
		return new PlayerSizeCommand(plugin, "player_size_double", 1);
	}

	public static PlayerSizeCommand decrease(ModdedCrowdControlPlugin plugin) {
		return new PlayerSizeCommand(plugin, "player_size_halve", -0.5);
	}
}
