package dev.qixils.crowdcontrol.plugin.fabric.commands;

import dev.qixils.crowdcontrol.TimedEffect;
import dev.qixils.crowdcontrol.plugin.fabric.FabricCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.fabric.TimedVoidCommand;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static dev.qixils.crowdcontrol.TimedEffect.isActive;
import static dev.qixils.crowdcontrol.common.command.CommandConstants.POTION_DURATION;

@Getter
public class GravityCommand extends TimedVoidCommand {
	private final UUID GRAVITY_MODIFIER_UUID = new UUID(723038618076398311L, -6545840742910585990L);
	private final String GRAVITY_MODIFIER_NAME = "gravity-cc";
	private final Duration defaultDuration = POTION_DURATION;
	private final String effectName;
	private final double level;

	private GravityCommand(FabricCrowdControlPlugin plugin, String effectName, double level) {
		super(plugin);
		this.effectName = effectName;
		this.level = level;
	}

	@Override
	public void voidExecute(@NotNull List<@NotNull ServerPlayer> ignored, @NotNull Request request) {
		if (isActive("walk", request)) {
			request.buildResponse().type(Response.ResultType.RETRY).message("Cannot change gravity while frozen").send();
			return;
		}
		AtomicReference<List<ServerPlayer>> players = new AtomicReference<>(new ArrayList<>());
		new TimedEffect.Builder()
				.request(request)
				.effectGroup("gravity")
				.duration(getDuration(request))
				.startCallback(effect -> {
					players.set(plugin.getPlayers(request));
					for (Player player : players.get()) {
						AttributeInstance maxHealthAttr = player.getAttribute(Attributes.GRAVITY);
						if (maxHealthAttr == null) {
							getPlugin().getSLF4JLogger().warn("Player missing GRAVITY attribute");
							continue;
						}
						maxHealthAttr.addPermanentModifier(new AttributeModifier(
							GRAVITY_MODIFIER_UUID,
							GRAVITY_MODIFIER_NAME,
							level,
							AttributeModifier.Operation.ADD_MULTIPLIED_BASE
						));
					}
					playerAnnounce(players.get(), request);
					return request.buildResponse().type(Response.ResultType.SUCCESS).message("SUCCESS");
				})
				.completionCallback(effect -> {
					for (Player player : players.get()) {
						AttributeInstance maxHealthAttr = player.getAttribute(Attributes.GRAVITY);
						if (maxHealthAttr == null) continue;
						for (AttributeModifier attributeModifier : maxHealthAttr.getModifiers()) {
							if (attributeModifier.getId() == GRAVITY_MODIFIER_UUID) {
								maxHealthAttr.removePermanentModifier(GRAVITY_MODIFIER_UUID);
							}
						}
					}
				})
				.build().queue();
	}

	@NotNull
	public static GravityCommand zero(FabricCrowdControlPlugin plugin) {
		return new GravityCommand(plugin, "zero_gravity", -1);
	}

	@NotNull
	public static GravityCommand low(FabricCrowdControlPlugin plugin) {
		return new GravityCommand(plugin, "low_gravity", -0.5);
	}

	@NotNull
	public static GravityCommand high(FabricCrowdControlPlugin plugin) {
		return new GravityCommand(plugin, "high_gravity", 0.5);
	}

	@NotNull
	public static GravityCommand maximum(FabricCrowdControlPlugin plugin) {
		return new GravityCommand(plugin, "maximum_gravity", 3);
	}
}
