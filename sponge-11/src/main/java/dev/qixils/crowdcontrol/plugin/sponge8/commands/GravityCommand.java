package dev.qixils.crowdcontrol.plugin.sponge8.commands;

import dev.qixils.crowdcontrol.TimedEffect;
import dev.qixils.crowdcontrol.plugin.sponge8.SpongeCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.sponge8.TimedVoidCommand;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.entity.attribute.AttributeOperations;
import org.spongepowered.api.entity.attribute.type.AttributeTypes;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static dev.qixils.crowdcontrol.TimedEffect.isActive;
import static dev.qixils.crowdcontrol.common.command.CommandConstants.*;
import static dev.qixils.crowdcontrol.plugin.sponge8.utils.AttributeUtil.addModifier;
import static dev.qixils.crowdcontrol.plugin.sponge8.utils.AttributeUtil.removeModifier;

@Getter
public class GravityCommand extends TimedVoidCommand {
	private final Duration defaultDuration = POTION_DURATION;
	private final String effectName;

	private final double gravityLevel;
	private final double fallLevel;
	private final double fallDmgLevel;

	private GravityCommand(SpongeCrowdControlPlugin plugin, String effectName, double gravityLevel, double fallLevel, double fallDmgLevel) {
		super(plugin);
		this.effectName = effectName;

		this.gravityLevel = gravityLevel;
		this.fallLevel = fallLevel;
		this.fallDmgLevel = fallDmgLevel;
	}

	@Override
	public void voidExecute(@NotNull List<@NotNull ServerPlayer> ignored, @NotNull Request request) {
		if (isActive("walk", request)) {
			request.buildResponse().type(Response.ResultType.RETRY).message("Cannot alter gravity while frozen").send();
			return;
		}
		AtomicReference<List<ServerPlayer>> players = new AtomicReference<>(new ArrayList<>());
		new TimedEffect.Builder()
			.request(request)
			.effectGroup("gravity")
			.duration(getDuration(request))
			.startCallback(effect -> {
				players.set(plugin.getPlayers(request));
				for (ServerPlayer player : players.get()) {
					addModifier(player, AttributeTypes.GENERIC_GRAVITY.get(), GRAVITY_MODIFIER_UUID, GRAVITY_MODIFIER_NAME, gravityLevel, AttributeOperations.MULTIPLY_BASE.get());
					addModifier(player, AttributeTypes.GENERIC_SAFE_FALL_DISTANCE.get(), FALL_MODIFIER_UUID, FALL_MODIFIER_NAME, fallLevel, AttributeOperations.MULTIPLY_BASE.get());
					addModifier(player, AttributeTypes.GENERIC_FALL_DAMAGE_MULTIPLIER.get(), FALL_DMG_MODIFIER_UUID, FALL_DMG_MODIFIER_NAME, fallDmgLevel, AttributeOperations.ADDITION.get());
				}
				playerAnnounce(players.get(), request);
				return request.buildResponse().type(Response.ResultType.SUCCESS).message("SUCCESS");
			})
			.completionCallback(effect -> {
				for (ServerPlayer player : players.get()) {
					removeModifier(player, AttributeTypes.GENERIC_GRAVITY.get(), GRAVITY_MODIFIER_UUID);
					removeModifier(player, AttributeTypes.GENERIC_SAFE_FALL_DISTANCE.get(), FALL_MODIFIER_UUID);
				}
			})
			.build().queue();
	}

	@NotNull
	public static GravityCommand zero(SpongeCrowdControlPlugin plugin) {
		return new GravityCommand(plugin, "zero_gravity", -1, 0, 0);
	}

	@NotNull
	public static GravityCommand low(SpongeCrowdControlPlugin plugin) {
		return new GravityCommand(plugin, "low_gravity", -0.5, 1, -0.5);
	}

	@NotNull
	public static GravityCommand high(SpongeCrowdControlPlugin plugin) {
		return new GravityCommand(plugin, "high_gravity", 1, -0.5, 1);
	}

	@NotNull
	public static GravityCommand maximum(SpongeCrowdControlPlugin plugin) {
		return new GravityCommand(plugin, "maximum_gravity", 3, -1, 3);
	}
}
