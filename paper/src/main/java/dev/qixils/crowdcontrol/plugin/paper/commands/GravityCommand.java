package dev.qixils.crowdcontrol.plugin.paper.commands;

import dev.qixils.crowdcontrol.TimedEffect;
import dev.qixils.crowdcontrol.plugin.paper.PaperCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.paper.TimedVoidCommand;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static dev.qixils.crowdcontrol.TimedEffect.isActive;
import static dev.qixils.crowdcontrol.common.command.CommandConstants.*;
import static dev.qixils.crowdcontrol.plugin.paper.utils.AttributeUtil.addModifier;
import static dev.qixils.crowdcontrol.plugin.paper.utils.AttributeUtil.removeModifier;

@Getter
public class GravityCommand extends TimedVoidCommand {
	private final Duration defaultDuration = POTION_DURATION;
	private final String effectName;

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
	public void voidExecute(@NotNull List<@NotNull Player> ignored, @NotNull Request request) {
		if (isActive("walk", request)) {
			request.buildResponse().type(Response.ResultType.RETRY).message("Cannot alter gravity while frozen").send();
			return;
		}
		AtomicReference<List<Player>> players = new AtomicReference<>(new ArrayList<>());
		new TimedEffect.Builder()
				.request(request)
				.effectGroup("gravity")
				.duration(getDuration(request))
				.startCallback(effect -> {
					players.set(plugin.getPlayers(request));
					players.get().forEach(player -> player.getScheduler().run(plugin, $ -> {
						addModifier(player, Attribute.GENERIC_GRAVITY, GRAVITY_MODIFIER_UUID, GRAVITY_MODIFIER_NAME, gravityLevel, AttributeModifier.Operation.ADD_SCALAR, false);
						addModifier(player, Attribute.GENERIC_SAFE_FALL_DISTANCE, FALL_MODIFIER_UUID, FALL_MODIFIER_NAME, fallLevel, AttributeModifier.Operation.ADD_SCALAR, false);
						addModifier(player, Attribute.GENERIC_FALL_DAMAGE_MULTIPLIER, FALL_DMG_MODIFIER_UUID, FALL_DMG_MODIFIER_NAME, fallDmgLevel, AttributeModifier.Operation.ADD_NUMBER, false);
					}, null));
					playerAnnounce(players.get(), request);
					return request.buildResponse().type(Response.ResultType.SUCCESS).message("SUCCESS");
				})
				.completionCallback(effect -> players.get().forEach(player -> player.getScheduler().run(plugin, $ -> {
					removeModifier(player, Attribute.GENERIC_GRAVITY, GRAVITY_MODIFIER_UUID);
					removeModifier(player, Attribute.GENERIC_SAFE_FALL_DISTANCE, FALL_MODIFIER_UUID);
				}, null)))
				.build().queue();
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
