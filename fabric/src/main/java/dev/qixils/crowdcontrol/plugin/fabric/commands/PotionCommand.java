package dev.qixils.crowdcontrol.plugin.fabric.commands;

import dev.qixils.crowdcontrol.TimedEffect;
import dev.qixils.crowdcontrol.plugin.fabric.FabricCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.fabric.TimedImmediateCommand;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import dev.qixils.crowdcontrol.socket.Response.ResultType;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.List;

import static dev.qixils.crowdcontrol.common.command.CommandConstants.POTION_DURATION;

@Getter
public class PotionCommand extends TimedImmediateCommand {
	private final @NotNull StatusEffect potionEffectType;
	private final boolean isMinimal;
	private final @NotNull String effectName;
	private final @NotNull Component displayName;

	@SuppressWarnings("ConstantConditions")
	public PotionCommand(@NotNull FabricCrowdControlPlugin plugin, @NotNull StatusEffect potionEffectType) {
		super(plugin);
		this.potionEffectType = potionEffectType;
		this.effectName = "potion_" + Registries.STATUS_EFFECT.getId(potionEffectType).getPath();
		this.isMinimal = potionEffectType.isInstant();
		this.displayName = Component.translatable("cc.effect.potion.name", potionEffectType.getName());
	}

	public @NotNull Duration getDefaultDuration() {
		return POTION_DURATION;
	}

	@Override
	public @NotNull Duration getDuration(@NotNull Request request) {
		if (isMinimal)
			return Duration.ZERO;
		return super.getDuration(request);
	}

	@NotNull
	@Override
	public Response.Builder executeImmediately(@NotNull List<@NotNull ServerPlayerEntity> players, @NotNull Request request) {
		if (potionEffectType == StatusEffects.JUMP_BOOST
				&& TimedEffect.isActive("disable_jumping", request.getTargets())) {
			return request.buildResponse()
					.type(ResultType.RETRY)
					.message("Cannot apply jump boost while Disable Jump is active");
		}

		boolean viable = false;
		StatusEffectInstance dummyEffect = new StatusEffectInstance(potionEffectType);
		for (ServerPlayerEntity player : players) {
			if (player.canHaveStatusEffect(dummyEffect)) {
				viable = true;
				break;
			}
		}
		if (!viable)
			return request.buildResponse()
					.type(ResultType.FAILURE)
					.message("Cannot apply potion effect to any players");

		int durationTicks = isMinimal ? 1 : (int) getDuration(request).getSeconds() * 20;

		sync(() -> {
			for (ServerPlayerEntity player : players) {
				StatusEffectInstance effect = new StatusEffectInstance(potionEffectType, durationTicks);
				StatusEffectInstance existingEffect = player.getStatusEffect(potionEffectType);
				if (existingEffect == null) {
					plugin.getSLF4JLogger().debug("Adding new effect");
					player.addStatusEffect(effect);
				} else {
					plugin.getSLF4JLogger().debug("Updating existing effect");
					int oldDuration = existingEffect.getDuration();
					int newDuration = oldDuration == -1 ? -1 : Math.max(durationTicks, oldDuration);
					int newAmplifier = existingEffect.getAmplifier() + 1;
					if (potionEffectType == StatusEffects.LEVITATION && newAmplifier > 127)
						newAmplifier -= 1;
					player.setStatusEffect(new StatusEffectInstance(
							potionEffectType,
							newDuration,
							newAmplifier,
							existingEffect.isAmbient(),
							existingEffect.shouldShowParticles(),
							existingEffect.shouldShowIcon()
					), null);
				}
			}
		});

		return request.buildResponse().type(ResultType.SUCCESS);
	}
}
