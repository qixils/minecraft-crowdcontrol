package dev.qixils.crowdcontrol.plugin.paper.commands;

import dev.qixils.crowdcontrol.TimedEffect;
import dev.qixils.crowdcontrol.common.util.TextUtil;
import dev.qixils.crowdcontrol.plugin.paper.PaperCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.paper.TimedImmediateCommand;
import dev.qixils.crowdcontrol.plugin.paper.utils.ReflectionUtil;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import dev.qixils.crowdcontrol.socket.Response.ResultType;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
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
		Component potionName = ReflectionUtil.<String>invokeMethod(potionEffectType, "translationKey")
				.<Component>map(Component::translatable)
				.or(() -> ReflectionUtil.<NamespacedKey>invokeMethod(potionEffectType, "getKey")
						.<Component>map(key -> Component.translatable(TextUtil.translationKey("effect", key))))
				.orElse(Component.text(TextUtil.titleCase(potionEffectType.getName()))); // TODO: i can do a better fallback than this but it'll be annoying
		this.displayName = Component.translatable("cc.effect.potion.name", potionName);
	}

	private static String nameOf(PotionEffectType type) {
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
		if (potionEffectType == PotionEffectType.JUMP
				&& TimedEffect.isActive("disable_jumping", request.getTargets())) {
			return request.buildResponse()
					.type(ResultType.RETRY)
					.message("Cannot apply jump boost while Disable Jump is active");
		}

		int durationTicks = isMinimal ? 1 : (int) getDuration(request).getSeconds() * 20;

		sync(() -> {
			for (Player player : players) {
				boolean overridden = false;
				for (PotionEffect existingEffect : new ArrayList<>(player.getActivePotionEffects())) {
					if (existingEffect.getType().equals(potionEffectType)) {
						overridden = true;
						player.removePotionEffect(potionEffectType);
						int newDuration = Math.max(durationTicks, existingEffect.getDuration());
						int newAmplifier = existingEffect.getAmplifier() + 1;
						if (potionEffectType == PotionEffectType.LEVITATION && newAmplifier > 127)
							newAmplifier -= 1; // don't mess with gravity effects
						PotionEffect newEffect = potionEffectType.createEffect(newDuration, newAmplifier);
						player.addPotionEffect(newEffect);
						break;
					}
				}
				if (!overridden)
					player.addPotionEffect(potionEffectType.createEffect(durationTicks, 0));
			}
		});
		return request.buildResponse().type(Response.ResultType.SUCCESS);
	}
}
