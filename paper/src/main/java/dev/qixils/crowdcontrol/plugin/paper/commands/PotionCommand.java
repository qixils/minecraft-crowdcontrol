package dev.qixils.crowdcontrol.plugin.paper.commands;

import dev.qixils.crowdcontrol.TimedEffect;
import dev.qixils.crowdcontrol.plugin.paper.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.paper.PaperCrowdControlPlugin;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import dev.qixils.crowdcontrol.socket.Response.ResultType;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static dev.qixils.crowdcontrol.common.command.CommandConstants.POTION_SECONDS;

@Getter
public class PotionCommand extends ImmediateCommand {
	private static final int MAX_DURATION = 20 * POTION_SECONDS;
	private final PotionEffectType potionEffectType;
	private final int duration;
	private final String effectName;
	private final Component displayName;

	public PotionCommand(PaperCrowdControlPlugin plugin, PotionEffectType potionEffectType) {
		super(plugin);
		this.potionEffectType = potionEffectType;
		this.effectName = "potion_" + nameOf(potionEffectType);

		boolean isMinimal = potionEffectType.isInstant();
		this.duration = isMinimal ? 1 : MAX_DURATION;

		Component displayName = Component.translatable("cc.effect.potion.name", Component.translatable(potionEffectType));
		if (!isMinimal)
			displayName = displayName.append(Component.text(" (" + POTION_SECONDS + ")"));
		this.displayName = displayName;
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

	@Override
	public Response.@NotNull Builder executeImmediately(@NotNull List<@NotNull Player> players, @NotNull Request request) {
		if (potionEffectType == PotionEffectType.JUMP
				&& TimedEffect.isActive("disable_jumping", request.getTargets())) {
			return request.buildResponse()
					.type(ResultType.RETRY)
					.message("Cannot apply jump boost while Disable Jump is active");
		}

		sync(() -> {
			for (Player player : players) {
				boolean overridden = false;
				for (PotionEffect existingEffect : new ArrayList<>(player.getActivePotionEffects())) {
					if (existingEffect.getType().equals(potionEffectType)) {
						overridden = true;
						player.removePotionEffect(potionEffectType);
						int newDuration = Math.max(duration, existingEffect.getDuration());
						int newAmplifier = existingEffect.getAmplifier() + 1;
						if (potionEffectType == PotionEffectType.LEVITATION && newAmplifier > 127)
							newAmplifier -= 1; // don't mess with gravity effects
						PotionEffect newEffect = potionEffectType.createEffect(newDuration, newAmplifier);
						player.addPotionEffect(newEffect);
						break;
					}
				}
				if (!overridden)
					player.addPotionEffect(potionEffectType.createEffect(duration, 0));
			}
		});
		return request.buildResponse().type(Response.ResultType.SUCCESS);
	}
}
