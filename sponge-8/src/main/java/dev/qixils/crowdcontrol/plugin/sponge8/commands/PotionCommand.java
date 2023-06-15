package dev.qixils.crowdcontrol.plugin.sponge8.commands;

import dev.qixils.crowdcontrol.TimedEffect;
import dev.qixils.crowdcontrol.plugin.sponge8.SpongeCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.sponge8.TimedImmediateCommand;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import dev.qixils.crowdcontrol.socket.Response.ResultType;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.effect.potion.PotionEffect;
import org.spongepowered.api.effect.potion.PotionEffectType;
import org.spongepowered.api.effect.potion.PotionEffectTypes;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.util.Ticks;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static dev.qixils.crowdcontrol.common.command.CommandConstants.POTION_DURATION;

@Getter
public class PotionCommand extends TimedImmediateCommand {
	private final @NotNull PotionEffectType potionEffectType;
	private final boolean isMinimal;
	private final @NotNull String effectName;
	private final @NotNull Component displayName;

	public PotionCommand(SpongeCrowdControlPlugin plugin, PotionEffectType potionEffectType) {
		super(plugin);
		this.potionEffectType = potionEffectType;
		this.effectName = "potion_" + potionEffectType.key(RegistryTypes.POTION_EFFECT_TYPE).value();
		this.isMinimal = potionEffectType.isInstant();
		this.displayName = Component.translatable("cc.effect.potion.name", potionEffectType);
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
		if (potionEffectType.equals(PotionEffectTypes.JUMP_BOOST.get())
				&& TimedEffect.isActive("disable_jumping", request.getTargets())) {
			return request.buildResponse()
					.type(ResultType.RETRY)
					.message("Cannot apply jump boost while Disable Jump is active");
		}

		Duration duration = getDuration(request);
		long durationLong = duration.getSeconds() * 20;
		Ticks durationTicks = Ticks.of(isMinimal ? 1 : durationLong);

		PotionEffect.Builder builder = PotionEffect.builder()
				.potionType(potionEffectType)
				.duration(durationTicks);

		Response.Builder response = request.buildResponse().type(ResultType.FAILURE).message("Player already has this effect");

		for (ServerPlayer player : players) {
			PotionEffect effect = builder.build();
			List<PotionEffect> effects = new ArrayList<>(player.get(Keys.POTION_EFFECTS).orElseGet(Collections::emptyList));
			effects = new ArrayList<>(effects);

			boolean overridden = false;
			for (int i = 0; i < effects.size(); i++) {
				PotionEffect existingEffect = effects.get(i);
				if (!potionEffectType.equals(existingEffect.type()))
					continue;

				plugin.getSLF4JLogger().debug("Updating existing effect");
				overridden = true;
				if (true) {
					// working around sponge bug. TODO: remove when fixed
					break;
				}

				long oldDuration = existingEffect.duration().ticks();
				long newDuration = oldDuration == -1 ? -1 : Math.max(durationLong, oldDuration);
				int newAmplifier = existingEffect.amplifier() + 1;
				if (potionEffectType.equals(PotionEffectTypes.LEVITATION.get()) && newAmplifier > 127)
					newAmplifier -= 1; // don't mess with gravity effects

				PotionEffect.Builder newEffect = PotionEffect.builder();
				try {
					newEffect.from(existingEffect);
				} catch (AbstractMethodError ignored) {
					newEffect.potionType(existingEffect.type())
							.ambient(existingEffect.isAmbient())
							.showParticles(existingEffect.showsParticles())
							.duration(Ticks.of(newDuration))
							.amplifier(newAmplifier);
					// showIcon is not set because it's what causes the AbstractMethodError in the first place
				}
				effects.set(i, newEffect.build());
				response.type(ResultType.SUCCESS).message("SUCCESS");
				break;
			}

			if (!overridden) {
				plugin.getSLF4JLogger().debug("Adding new effect");
				effects.add(effect);
				response.type(ResultType.SUCCESS).message("SUCCESS");
			}

			player.offer(Keys.POTION_EFFECTS, effects);
		}

		if (!isMinimal && response.type() == ResultType.SUCCESS)
			response.timeRemaining(duration);
		return response;
	}
}
