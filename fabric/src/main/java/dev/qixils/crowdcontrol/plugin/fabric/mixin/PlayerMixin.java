package dev.qixils.crowdcontrol.plugin.fabric.mixin;

import dev.qixils.crowdcontrol.plugin.fabric.FabricCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.fabric.event.Jump;
import dev.qixils.crowdcontrol.plugin.fabric.utils.EntityUtil;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameRules.BooleanValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class PlayerMixin {

	@Inject(method = "jumpFromGround", at = @At("HEAD"), cancellable = true)
	public void jumpFromGround(CallbackInfo ci) {
		if (!FabricCrowdControlPlugin.isInstanceAvailable()) return;
		Player thiss = (Player) (Object) this;
		Jump jump = new Jump(thiss);
		FabricCrowdControlPlugin.getInstance().getEventManager().fire(jump);
		if (jump.cancelled())
			ci.cancel(); // TODO: test in multiplayer?
	}

	private boolean keepInventoryRedirect(GameRules gameRules, GameRules.Key<BooleanValue> key) {
		return EntityUtil.keepInventoryRedirect((Entity) (Object) this, gameRules, key);
	}

	@Redirect(
			method = "dropEquipment()V",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/GameRules;getBoolean(Lnet/minecraft/world/level/GameRules$Key;)Z")
	)
	private boolean equipmentKeepInventoryRedirect(GameRules gameRules, GameRules.Key<BooleanValue> key) {
		return keepInventoryRedirect(gameRules, key);
	}

	@Redirect(
			method = "getExperienceReward()I",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/GameRules;getBoolean(Lnet/minecraft/world/level/GameRules$Key;)Z")
	)
	private boolean experienceKeepInventoryRedirect(GameRules gameRules, GameRules.Key<BooleanValue> key) {
		return keepInventoryRedirect(gameRules, key);
	}
}
