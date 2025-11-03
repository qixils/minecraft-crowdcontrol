package dev.qixils.crowdcontrol.plugin.fabric.utils;

import dev.qixils.crowdcontrol.plugin.fabric.ModdedCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.fabric.commands.KeepInventoryCommand;
import dev.qixils.crowdcontrol.plugin.fabric.event.Damage;
import dev.qixils.crowdcontrol.plugin.fabric.event.Death;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameRules.BooleanValue;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

public class EntityUtil {
	private EntityUtil() {
	}

	/**
	 * Redirect the keepInventory value
	 *
	 * @param entity Entity trying to respawn
	 * @param keepInventory Global server value for the gamerule
	 * @param key ID of the gamerule being checked
	 * @return resulting, potentially redirected value
	 */
	public static boolean keepInventoryRedirect(Entity entity, boolean keepInventory, GameRules.Key<BooleanValue> key) {
		// double check that this is the keep inventory game rule
		if (key != GameRules.RULE_KEEPINVENTORY)
			return keepInventory;

		// don't redirect if the game rule is enabled
		if (keepInventory)
			return true;

		// return whether the keep inventory command is active for this user
		return KeepInventoryCommand.isKeepingInventory(entity);
	}

	public static void handleDie(LivingEntity entity, final DamageSource cause, final CallbackInfo ci) {
		if (entity.level().isClientSide()) return;
		if (!ModdedCrowdControlPlugin.isInstanceAvailable()) return;
		if (entity.dead) return;
		Death event = new Death(entity, cause);
		event.fire(ModdedCrowdControlPlugin.getInstance());
		if (event.cancelled()) ci.cancel();
	}

	public static void handleDamage(final Entity entity, final DamageSource cause, final float amount, final CallbackInfoReturnable<Boolean> cir) {
		if (entity.level().isClientSide()) return;
		if (!ModdedCrowdControlPlugin.isInstanceAvailable()) return;
		Damage event = new Damage(entity, cause, amount);
		event.fire(ModdedCrowdControlPlugin.getInstance());
		if (event.cancelled()) cir.setReturnValue(false);
	}
}
