package dev.qixils.crowdcontrol.plugin.fabric.commands;

import dev.qixils.crowdcontrol.TimedEffect;
import dev.qixils.crowdcontrol.plugin.fabric.FabricCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.fabric.TimedImmediateCommand;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import dev.qixils.crowdcontrol.socket.Response.ResultType;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.List;

import static dev.qixils.crowdcontrol.common.command.CommandConstants.POTION_DURATION;

@Getter
public class PotionCommand extends TimedImmediateCommand {
	private final @NotNull MobEffect potionEffectType;
	private final boolean isMinimal;
	private final @NotNull String effectName;
	private final @NotNull Component displayName;

	@SuppressWarnings("ConstantConditions")
	public PotionCommand(@NotNull FabricCrowdControlPlugin plugin, @NotNull MobEffect potionEffectType) {
		super(plugin);
		this.potionEffectType = potionEffectType;
		this.effectName = "potion_" + BuiltInRegistries.MOB_EFFECT.getKey(potionEffectType).getPath();
		this.isMinimal = potionEffectType.isInstantenous();
		this.displayName = Component.translatable("cc.effect.potion.name", potionEffectType.getDisplayName());
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
	public Response.Builder executeImmediately(@NotNull List<@NotNull ServerPlayer> players, @NotNull Request request) {
		if (potionEffectType == MobEffects.JUMP
				&& TimedEffect.isActive("disable_jumping", request.getTargets())) {
			return request.buildResponse()
					.type(ResultType.RETRY)
					.message("Cannot apply jump boost while Disable Jump is active");
		}

		int durationTicks = isMinimal ? 1 : (int) getDuration(request).getSeconds() * 20;

		sync(() -> {
			for (ServerPlayer player : players) {
				MobEffectInstance effect = new MobEffectInstance(potionEffectType, durationTicks);
				MobEffectInstance existingEffect = player.getEffect(potionEffectType);
				if (existingEffect == null) {
					plugin.getSLF4JLogger().debug("Adding new effect");
					player.addEffect(effect);
				} else {
					plugin.getSLF4JLogger().debug("Updating existing effect");
					int newDuration = Math.max(existingEffect.getDuration(), durationTicks);
					int newAmplifier = existingEffect.getAmplifier() + 1;
					if (potionEffectType == MobEffects.LEVITATION && newAmplifier > 127)
						newAmplifier -= 1;
					player.forceAddEffect(new MobEffectInstance(
							potionEffectType,
							newDuration,
							newAmplifier,
							existingEffect.isAmbient(),
							existingEffect.isVisible(),
							existingEffect.showIcon()
					), null);
				}
			}
		});

		return request.buildResponse().type(ResultType.SUCCESS);
	}
}
