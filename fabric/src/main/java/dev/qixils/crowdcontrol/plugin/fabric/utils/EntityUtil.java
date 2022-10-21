package dev.qixils.crowdcontrol.plugin.fabric.utils;

import dev.qixils.crowdcontrol.plugin.fabric.FabricCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.fabric.commands.KeepInventoryCommand;
import dev.qixils.crowdcontrol.plugin.fabric.event.Death;
import dev.qixils.crowdcontrol.plugin.fabric.mixin.LivingEntityAccessor;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameRules.BooleanValue;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

public class EntityUtil {
	private EntityUtil() {
	}

	public static boolean keepInventoryRedirect(Entity entity, GameRules gameRules, GameRules.Key<BooleanValue> key) {
		// get server's default value
		boolean keepInventory = gameRules.getBoolean(key);

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
		if (!FabricCrowdControlPlugin.isInstanceAvailable()) return;
		if (((LivingEntityAccessor) entity).getDead()) return;
		Death event = new Death(entity, cause);
		FabricCrowdControlPlugin.getInstance().getEventManager().fire(event);
		if (event.cancelled()) ci.cancel();
	}
}