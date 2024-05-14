package dev.qixils.crowdcontrol.plugin.fabric.commands;

import dev.qixils.crowdcontrol.TimedEffect;
import dev.qixils.crowdcontrol.plugin.fabric.FabricCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.fabric.TimedVoidCommand;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static dev.qixils.crowdcontrol.TimedEffect.isActive;
import static dev.qixils.crowdcontrol.common.command.CommandConstants.*;

@Getter
public class GravityCommand extends TimedVoidCommand {
	private final double gravityLevel;
	private final double fallLevel;
	private final double fallDmgLevel;

	private final Duration defaultDuration = POTION_DURATION;
	private final String effectName;

	private GravityCommand(FabricCrowdControlPlugin plugin, String effectName, double gravityLevel, double fallLevel, double fallDmgLevel) {
		super(plugin);
		this.effectName = effectName;

		this.gravityLevel = gravityLevel;
		this.fallLevel = fallLevel;
		this.fallDmgLevel = fallDmgLevel;
	}

	private static void removeModifier(AttributeInstance attr, UUID uuid) {
        if (attr == null) return;
        for (AttributeModifier attributeModifier : attr.getModifiers()) {
            if (attributeModifier.id().equals(uuid)) {
                attr.removePermanentModifier(uuid);
                break; // avoid CME or whatever it's called
            }
        }
    }

	private static void removeModifier(ServerPlayer player, Holder<Attribute> attribute, UUID uuid) {
		removeModifier(player.getAttribute(attribute), uuid);
	}

	private void addModifier(ServerPlayer player, Holder<Attribute> attributeHolder, UUID uuid, String name, double level, AttributeModifier.Operation op) {
		AttributeInstance attr = player.getAttribute(attributeHolder);
		if (attr == null) {
			getPlugin().getSLF4JLogger().warn("Player missing {} attribute", attributeHolder.unwrapKey().orElse(null));
			return;
		}

		removeModifier(attr, uuid);
		if (level == 0) return;

		attr.addPermanentModifier(new AttributeModifier(
			uuid,
			name,
			level,
			op
		));
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
					for (ServerPlayer player : players.get()) {
						addModifier(player, Attributes.GRAVITY, GRAVITY_MODIFIER_UUID, GRAVITY_MODIFIER_NAME, gravityLevel, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
						addModifier(player, Attributes.SAFE_FALL_DISTANCE, FALL_MODIFIER_UUID, FALL_MODIFIER_NAME, fallLevel, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
						addModifier(player, Attributes.FALL_DAMAGE_MULTIPLIER, FALL_DMG_MODIFIER_UUID, FALL_DMG_MODIFIER_NAME, fallDmgLevel, AttributeModifier.Operation.ADD_VALUE);
					}
					playerAnnounce(players.get(), request);
					return request.buildResponse().type(Response.ResultType.SUCCESS).message("SUCCESS");
				})
				.completionCallback(effect -> {
					for (ServerPlayer player : players.get()) {
						removeModifier(player, Attributes.GRAVITY, GRAVITY_MODIFIER_UUID);
						removeModifier(player, Attributes.SAFE_FALL_DISTANCE, FALL_MODIFIER_UUID);
					}
				})
				.build().queue();
	}

	@NotNull
	public static GravityCommand zero(FabricCrowdControlPlugin plugin) {
		return new GravityCommand(plugin, "zero_gravity", -1, 0, 0);
	}

	@NotNull
	public static GravityCommand low(FabricCrowdControlPlugin plugin) {
		return new GravityCommand(plugin, "low_gravity", -0.5, 1, -0.5);
	}

	@NotNull
	public static GravityCommand high(FabricCrowdControlPlugin plugin) {
		return new GravityCommand(plugin, "high_gravity", 1, -0.5, 1);
	}

	@NotNull
	public static GravityCommand maximum(FabricCrowdControlPlugin plugin) {
		return new GravityCommand(plugin, "maximum_gravity", 3, -1, 3);
	}
}
