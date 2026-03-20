package dev.qixils.crowdcontrol.plugin.fabric.commands;

import dev.qixils.crowdcontrol.common.command.CommandConstants;
import dev.qixils.crowdcontrol.common.util.ThreadUtil;
import dev.qixils.crowdcontrol.plugin.fabric.ModdedCommand;
import dev.qixils.crowdcontrol.plugin.fabric.ModdedCrowdControlPlugin;
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
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

import static dev.qixils.crowdcontrol.common.command.CommandConstants.POTION_SECONDS;

@Getter
public class PotionCommand extends ModdedCommand implements CCTimedEffect {
	private final @NotNull MobEffect potionEffectType;
	private final boolean isMinimal;
	private final @NotNull String effectName;
	private final @NotNull Component displayName;
	private final CCName extensionName;
	private final String image = "potion_speed";
	private final int price = 50;
	private final byte priority = 0;
	private final boolean exclusive;
	private final List<String> categories = Collections.singletonList("Potion Effects");
	private final CustomEffectDuration extensionDuration = new CustomEffectDuration(POTION_SECONDS);

	@SuppressWarnings("ConstantConditions")
	public PotionCommand(@NotNull ModdedCrowdControlPlugin plugin, @NotNull MobEffect potionEffectType) {
		super(plugin);
		this.potionEffectType = potionEffectType;
		ResourceLocation loc = Objects.requireNonNull(Registry.MOB_EFFECT.getKey(potionEffectType));
		this.effectName = "potion_" + CommandConstants.asMinimalSafeString(loc);
		this.isMinimal = potionEffectType.isInstantenous();
		TranslatableComponent _displayName = Component.translatable("cc.effect.potion.name", plugin.toAdventure(potionEffectType.getDisplayName()));
		this.displayName = _displayName;
		this.extensionName = new CCName(plugin.getTextUtil().asPlain(_displayName.key("cc.effect.potion.extension")));
		this.exclusive = potionEffectType == MobEffects.BLINDNESS
			|| potionEffectType == MobEffects.FIRE_RESISTANCE
			|| potionEffectType == MobEffects.GLOWING
			|| potionEffectType == MobEffects.INVISIBILITY
			|| potionEffectType == MobEffects.CONFUSION
			|| potionEffectType == MobEffects.NIGHT_VISION
			|| potionEffectType == MobEffects.SLOW_FALLING
			|| potionEffectType == MobEffects.WATER_BREATHING
			|| potionEffectType == MobEffects.ABSORPTION;
	}

	@Override
	public void execute(@NotNull Supplier<@NotNull List<@NotNull ServerPlayer>> playerSupplier, @NotNull PublicEffectPayload request, @NotNull CCPlayer ccPlayer) {
		ccPlayer.sendResponse(ThreadUtil.waitForSuccess(request, () -> {
			if (potionEffectType == MobEffects.JUMP && isActive(ccPlayer, "disable_jumping"))
				return new CCInstantEffectResponse(request.getRequestId(), ResponseStatus.FAIL_TEMPORARY, "Cannot apply jump boost while Disable Jump is active");

			Duration duration = Duration.ofMillis(request.getEffect().getDurationMillis());
			int durationTicks = isMinimal ? 1 : (int) duration.getSeconds() * 20;

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
					));
				}
			}

			return isMinimal
				? new CCInstantEffectResponse(request.getRequestId(), ResponseStatus.SUCCESS)
				: new CCTimedEffectResponse(request.getRequestId(), ResponseStatus.TIMED_BEGIN, request.getEffect().getDurationMillis());
		}, plugin.getSyncExecutor()));
	}
}
