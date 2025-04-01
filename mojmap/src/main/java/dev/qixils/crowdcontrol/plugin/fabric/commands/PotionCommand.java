package dev.qixils.crowdcontrol.plugin.fabric.commands;

import dev.qixils.crowdcontrol.common.util.ThreadUtil;
import dev.qixils.crowdcontrol.plugin.fabric.ModdedCommand;
import dev.qixils.crowdcontrol.plugin.fabric.ModdedCrowdControlPlugin;
import live.crowdcontrol.cc4j.CCPlayer;
import live.crowdcontrol.cc4j.CCTimedEffect;
import live.crowdcontrol.cc4j.websocket.data.CCInstantEffectResponse;
import live.crowdcontrol.cc4j.websocket.data.CCTimedEffectResponse;
import live.crowdcontrol.cc4j.websocket.data.ResponseStatus;
import live.crowdcontrol.cc4j.websocket.payload.PublicEffectPayload;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.List;
import java.util.function.Supplier;

@Getter
public class PotionCommand extends ModdedCommand implements CCTimedEffect {
	private final @NotNull Holder<MobEffect> potionEffectType;
	private final boolean isMinimal;
	private final @NotNull String effectName;
	private final @NotNull Component displayName;

	@SuppressWarnings("ConstantConditions")
	public PotionCommand(@NotNull ModdedCrowdControlPlugin plugin, @NotNull Holder<MobEffect> potionEffectType) {
		super(plugin);
		this.potionEffectType = potionEffectType;
		this.effectName = "potion_" + potionEffectType.unwrapKey().orElseThrow().location().getPath();
		this.isMinimal = potionEffectType.value().isInstantenous();
		this.displayName = Component.translatable("cc.effect.potion.name", plugin.toAdventure(potionEffectType.value().getDisplayName()));
	}

	@Override
	public void execute(@NotNull Supplier<@NotNull List<@NotNull ServerPlayer>> playerSupplier, @NotNull PublicEffectPayload request, @NotNull CCPlayer ccPlayer) {
		ccPlayer.sendResponse(ThreadUtil.waitForSuccess(() -> {
			if (potionEffectType == MobEffects.JUMP_BOOST && isActive(ccPlayer, "disable_jumping"))
				return new CCInstantEffectResponse(request.getRequestId(), ResponseStatus.FAIL_TEMPORARY, "Cannot apply jump boost while Disable Jump is active");

			Duration duration = Duration.ofSeconds(request.getEffect().getDuration());
			int durationTicks = isMinimal ? 1 : (int) duration.getSeconds() * 20;

			sync(() -> {
				for (ServerPlayer player : playerSupplier.get()) {
					MobEffectInstance effect = new MobEffectInstance(potionEffectType, durationTicks);
					MobEffectInstance existingEffect = player.getEffect(potionEffectType);
					if (existingEffect == null) {
						plugin.getSLF4JLogger().debug("Adding new effect");
						player.addEffect(effect);
					} else {
						plugin.getSLF4JLogger().debug("Updating existing effect");
						int oldDuration = existingEffect.getDuration();
						int newDuration = oldDuration == -1 ? -1 : Math.max(durationTicks, oldDuration);
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

			return isMinimal
				? new CCInstantEffectResponse(request.getRequestId(), ResponseStatus.SUCCESS)
				: new CCTimedEffectResponse(request.getRequestId(), ResponseStatus.TIMED_BEGIN, request.getEffect().getDuration() * 1000L);
		}));
	}
}
