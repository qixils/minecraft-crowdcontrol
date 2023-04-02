package dev.qixils.crowdcontrol.plugin.fabric.utils;

import dev.qixils.crowdcontrol.plugin.fabric.FabricCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.fabric.commands.KeepInventoryCommand;
import dev.qixils.crowdcontrol.plugin.fabric.event.Damage;
import dev.qixils.crowdcontrol.plugin.fabric.event.Death;
import dev.qixils.crowdcontrol.plugin.fabric.mixin.LivingEntityAccessor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.world.GameRules;
import net.minecraft.world.GameRules.BooleanRule;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

public class EntityUtil {
	private EntityUtil() {
	}

	public static boolean keepInventoryRedirect(Entity entity, GameRules gameRules, GameRules.Key<BooleanRule> key) {
		// get server's default value
		boolean keepInventory = gameRules.getBoolean(key);

		// double check that this is the keep inventory game rule
		if (key != GameRules.KEEP_INVENTORY)
			return keepInventory;

		// don't redirect if the game rule is enabled
		if (keepInventory)
			return true;

		// return whether the keep inventory command is active for this user
		return KeepInventoryCommand.isKeepingInventory(entity);
	}

	public static void handleDie(LivingEntity entity, final DamageSource cause, final CallbackInfo ci) {
		if (entity.world.isClient) return;
		if (!FabricCrowdControlPlugin.isInstanceAvailable()) return;
		if (((LivingEntityAccessor) entity).getDead()) return;
		Death event = new Death(entity, cause);
		event.fire(FabricCrowdControlPlugin.getInstance());
		if (event.cancelled()) ci.cancel();
	}

	public static void handleDamage(final Entity entity, final DamageSource cause, final float amount, final CallbackInfoReturnable<Boolean> cir) {
		if (entity.world.isClient) return;
		if (!FabricCrowdControlPlugin.isInstanceAvailable()) return;
		Damage event = new Damage(entity, cause, amount);
		event.fire(FabricCrowdControlPlugin.getInstance());
		if (event.cancelled()) cir.setReturnValue(false);
	}
}
