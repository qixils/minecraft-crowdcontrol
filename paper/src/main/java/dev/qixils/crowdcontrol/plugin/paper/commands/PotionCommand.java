package dev.qixils.crowdcontrol.plugin.paper.commands;

import dev.qixils.crowdcontrol.common.util.ThreadUtil;
import dev.qixils.crowdcontrol.plugin.paper.PaperCommand;
import dev.qixils.crowdcontrol.plugin.paper.PaperCrowdControlPlugin;
import live.crowdcontrol.cc4j.CCPlayer;
import live.crowdcontrol.cc4j.CCTimedEffect;
import live.crowdcontrol.cc4j.websocket.data.CCInstantEffectResponse;
import live.crowdcontrol.cc4j.websocket.data.CCTimedEffectResponse;
import live.crowdcontrol.cc4j.websocket.data.ResponseStatus;
import live.crowdcontrol.cc4j.websocket.http.CustomEffectDuration;
import live.crowdcontrol.cc4j.websocket.payload.CCName;
import live.crowdcontrol.cc4j.websocket.payload.PublicEffectPayload;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import static dev.qixils.crowdcontrol.common.command.CommandConstants.POTION_SECONDS;
import static dev.qixils.crowdcontrol.common.command.CommandConstants.asMinimalSafeString;

@Getter
public class PotionCommand extends PaperCommand implements CCTimedEffect {
	private final @NotNull PotionEffectType potionEffectType;
	private final boolean isMinimal;
	private final @NotNull String effectName;
	private final @NotNull Component displayName;
	private final CCName extensionName;
	private final String image = "potion_speed";
	private final int price = 50;
	private final byte priority = 0;
	private final List<String> categories = Collections.singletonList("Potion Effects");
	private final CustomEffectDuration extensionDuration = new CustomEffectDuration(POTION_SECONDS);

	public PotionCommand(PaperCrowdControlPlugin plugin, PotionEffectType potionEffectType) {
		super(plugin);
		this.potionEffectType = potionEffectType;
		this.effectName = "potion_" + nameOf(potionEffectType);
		this.isMinimal = potionEffectType.isInstant();
		TranslatableComponent _displayName = Component.translatable("cc.effect.potion.name", Component.translatable(potionEffectType));
		this.displayName = _displayName;
		this.extensionName = new CCName(plugin.getTextUtil().asPlain(_displayName.key("cc.effect.potion.extension")));
	}

	private static String nameOf(PotionEffectType type) {
		// TODO: migrate getName
		String name = type.getName();
		return switch (name) {
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
			default -> asMinimalSafeString(type.key());
		};
	}

	@Override
	public void execute(@NotNull Supplier<@NotNull List<@NotNull Player>> playerSupplier, @NotNull PublicEffectPayload request, @NotNull CCPlayer ccPlayer) {
		ccPlayer.sendResponse(ThreadUtil.waitForSuccess(request, () -> {
			if (potionEffectType == PotionEffectType.JUMP_BOOST && isActive(ccPlayer, "disable_jumping")) {
				return new CCInstantEffectResponse(request.getRequestId(), ResponseStatus.FAIL_TEMPORARY, "Cannot apply jump boost while Disable Jump is active");
			}

			List<Player> players = playerSupplier.get();

			Duration duration = Duration.ofSeconds(request.getEffect().getDurationMillis());
			int durationTicks = isMinimal ? 1 : (int) duration.getSeconds() * 20;

			// TODO: improve folia...
			for (Player player : players) {
				boolean overridden = false;
				for (PotionEffect existingEffect : new ArrayList<>(player.getActivePotionEffects())) {
					if (existingEffect.getType().equals(potionEffectType)) {
						overridden = true;
						player.getScheduler().run(plugin.getPaperPlugin(), $ -> player.removePotionEffect(potionEffectType), null);
						int oldDuration = existingEffect.getDuration();
						int newDuration = oldDuration == -1 ? -1 : Math.max(durationTicks, oldDuration);
						int newAmplifier = existingEffect.getAmplifier() + 1;
						if (potionEffectType == PotionEffectType.LEVITATION && newAmplifier > 127)
							newAmplifier -= 1; // don't mess with gravity effects
						PotionEffect newEffect = potionEffectType.createEffect(newDuration, newAmplifier);
						player.getScheduler().run(plugin.getPaperPlugin(), $ -> player.addPotionEffect(newEffect), null);
						break;
					}
				}
				if (!overridden)
					player.getScheduler().run(plugin.getPaperPlugin(), $ -> player.addPotionEffect(potionEffectType.createEffect(durationTicks, 0)), null);
			}

			return request.getEffect().getDurationMillis() > 0
				? new CCTimedEffectResponse(request.getRequestId(), ResponseStatus.TIMED_BEGIN, duration.toMillis())
				: new CCInstantEffectResponse(request.getRequestId(), ResponseStatus.SUCCESS);
		}));
	}
}
