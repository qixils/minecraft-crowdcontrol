package dev.qixils.crowdcontrol.plugin.sponge7.commands;

import dev.qixils.crowdcontrol.TimedEffect;
import dev.qixils.crowdcontrol.plugin.sponge7.SpongeCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.sponge7.TimedImmediateCommand;
import dev.qixils.crowdcontrol.plugin.sponge7.utils.SpongeTextUtil;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import dev.qixils.crowdcontrol.socket.Response.ResultType;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.effect.potion.PotionEffect;
import org.spongepowered.api.effect.potion.PotionEffectType;
import org.spongepowered.api.effect.potion.PotionEffectTypes;
import org.spongepowered.api.entity.living.player.Player;

import java.time.Duration;
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
		this.effectName = "potion_" + SpongeTextUtil.csIdOf(potionEffectType);
		this.isMinimal = potionEffectType.isInstant();
		this.displayName = Component.translatable("cc.effect.potion.name", Component.translatable(potionEffectType.getTranslation().getId()));
	}

	@Override
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
	public Response.Builder executeImmediately(@NotNull List<@NotNull Player> players, @NotNull Request request) {
		if (potionEffectType.equals(PotionEffectTypes.JUMP_BOOST)
				&& TimedEffect.isActive("disable_jumping", request.getTargets())) {
			return request.buildResponse()
					.type(ResultType.RETRY)
					.message("Cannot apply jump boost while Disable Jump is active");
		}

		int duration = isMinimal ? 1 : (int) getDuration(request).getSeconds() * 20;

		PotionEffect.Builder builder = PotionEffect.builder()
				.potionType(potionEffectType)
				.duration(duration);

		for (Player player : players) {
			PotionEffect effect = builder.build();
			player.transform(Keys.POTION_EFFECTS, effects -> {
				if (effects == null)
					return Collections.singletonList(effect);

				boolean overridden = false;
				for (int i = 0; i < effects.size(); i++) {
					PotionEffect existingEffect = effects.get(i);
					if (existingEffect.getType().equals(potionEffectType)) {
						plugin.getSLF4JLogger().debug("Updating existing effect");
						overridden = true;

						int newDuration = Math.max(duration, existingEffect.getDuration());
						int newAmplifier = existingEffect.getAmplifier() + 1;
						if (potionEffectType.equals(PotionEffectTypes.LEVITATION) && newAmplifier > 127)
							newAmplifier -= 1; // don't mess with gravity effects

						effects.set(i, PotionEffect.builder()
								.from(existingEffect)
								.duration(newDuration)
								.amplifier(newAmplifier)
								.build());
						break;
					}
				}

				if (!overridden) {
					plugin.getSLF4JLogger().debug("Adding new effect");
					effects.add(effect);
				}

				return effects;
			});
		}

		return request.buildResponse().type(ResultType.SUCCESS);
	}
}
