package dev.qixils.crowdcontrol.plugin.fabric.commands;

import dev.qixils.crowdcontrol.TimedEffect;
import dev.qixils.crowdcontrol.plugin.fabric.FabricCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.fabric.ImmediateCommand;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import dev.qixils.crowdcontrol.socket.Response.ResultType;
import lombok.Getter;
import net.minecraft.core.Registry;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static dev.qixils.crowdcontrol.common.command.CommandConstants.POTION_SECONDS;

@Getter
public class PotionCommand extends ImmediateCommand {
	private static final int TICKS = 20 * POTION_SECONDS;
	private final MobEffect potionEffectType;
	private final int duration;
	private final String effectName;
	private final String displayName;

	@SuppressWarnings("ConstantConditions")
	public PotionCommand(FabricCrowdControlPlugin plugin, MobEffect potionEffectType) {
		super(plugin);
		this.potionEffectType = potionEffectType;
		boolean isMinimal = potionEffectType.isInstantenous();
		duration = isMinimal ? 1 : TICKS;
		this.effectName = "potion_" + Registry.MOB_EFFECT.getKey(potionEffectType).getPath();
		this.displayName = "Apply " + plugin.getTextUtil().asPlain(potionEffectType.getDisplayName()) + " Potion Effect (" + POTION_SECONDS + "s)";
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

		sync(() -> {
			for (ServerPlayer player : players) {
				MobEffectInstance effect = new MobEffectInstance(potionEffectType, duration);
				MobEffectInstance existingEffect = player.getEffect(potionEffectType);
				if (existingEffect == null) {
					plugin.getSLF4JLogger().debug("Adding new effect");
					player.addEffect(effect);
				} else {
					plugin.getSLF4JLogger().debug("Updating existing effect");
					player.forceAddEffect(new MobEffectInstance(
							potionEffectType,
							Math.max(existingEffect.getDuration(), duration),
							existingEffect.getAmplifier() + 1,
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
