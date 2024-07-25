package dev.qixils.crowdcontrol.plugin.paper.commands;

import dev.qixils.crowdcontrol.TimedEffect;
import dev.qixils.crowdcontrol.plugin.paper.PaperCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.paper.TimedImmediateCommand;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import dev.qixils.crowdcontrol.socket.Response.ResultType;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static dev.qixils.crowdcontrol.common.command.CommandConstants.POTION_DURATION;

@Getter
public class PotionCommand extends TimedImmediateCommand {
	private final @NotNull PotionEffectType potionEffectType;
	private final boolean isMinimal;
	private final @NotNull String effectName;
	private final @NotNull Component displayName;

	public PotionCommand(PaperCrowdControlPlugin plugin, PotionEffectType potionEffectType) {
		super(plugin);
		this.potionEffectType = potionEffectType;
		this.effectName = "potion_" + nameOf(potionEffectType);
		this.isMinimal = potionEffectType.isInstant();
		this.displayName = Component.translatable("cc.effect.potion.name", Component.translatable(potionEffectType));
	}

	private static String nameOf(PotionEffectType type) {
		// TODO: migrate getName
		return switch (type.getName()) {
			case "SLOW" -> "SLOWNESS";
			case "FAST_DIGGING" -> "HASTE";
			case "SLOW_DIGGING" -> "MINING_FATIGUE";
			case "INCREASE_DAMAGE" -> "STRENGTH";
			case "HEAL" -> "HEALING";
			case "HARM" -> "HARMING";
			case "JUMP" -> "JUMP_BOOST";
			case "CONFUSION" -> "NAUSEA";
			case "DAMAGE_RESISTANCE" -> "RESISTANCE";
			case "UNLUCK" -> "BAD_LUCK";
			default -> type.getName();
		};
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

	@Override
	public Response.@NotNull Builder executeImmediately(@NotNull List<@NotNull Player> players, @NotNull Request request) {
		if (potionEffectType == PotionEffectType.JUMP_BOOST
				&& TimedEffect.isActive("disable_jumping", request.getTargets())) {
			return request.buildResponse()
					.type(ResultType.RETRY)
					.message("Cannot apply jump boost while Disable Jump is active");
		}

		Duration duration = getDuration(request);
		int durationTicks = isMinimal ? 1 : (int) duration.getSeconds() * 20;

		// TODO: improve folia...
		for (Player player : players) {
			boolean overridden = false;
			for (PotionEffect existingEffect : new ArrayList<>(player.getActivePotionEffects())) {
				if (existingEffect.getType().equals(potionEffectType)) {
					overridden = true;
					player.getScheduler().run(plugin, $ -> player.removePotionEffect(potionEffectType), null);
					int oldDuration = existingEffect.getDuration();
					int newDuration = oldDuration == -1 ? -1 : Math.max(durationTicks, oldDuration);
					int newAmplifier = existingEffect.getAmplifier() + 1;
					if (potionEffectType == PotionEffectType.LEVITATION && newAmplifier > 127)
						newAmplifier -= 1; // don't mess with gravity effects
					PotionEffect newEffect = potionEffectType.createEffect(newDuration, newAmplifier);
					player.getScheduler().run(plugin, $ -> player.addPotionEffect(newEffect), null);
					break;
				}
			}
			if (!overridden)
				player.getScheduler().run(plugin, $ -> player.addPotionEffect(potionEffectType.createEffect(durationTicks, 0)), null);
		}

		Response.Builder response = request.buildResponse().type(Response.ResultType.SUCCESS);
		if (!isMinimal)
			response.timeRemaining(duration);
		return response;
	}
}
