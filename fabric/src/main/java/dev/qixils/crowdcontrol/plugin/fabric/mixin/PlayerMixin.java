package dev.qixils.crowdcontrol.plugin.fabric.mixin;

import dev.qixils.crowdcontrol.plugin.fabric.event.Jump;
import dev.qixils.crowdcontrol.plugin.fabric.utils.EntityUtil;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.GameRules;
import net.minecraft.world.GameRules.BooleanRule;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public abstract class PlayerMixin extends LivingEntity {

	// dummy constructor
	protected PlayerMixin(EntityType<? extends LivingEntity> entityType, World level) {
		super(entityType, level);
	}

	@Inject(method = "jump", at = @At("HEAD"), cancellable = true)
	public void jumpFromGround(CallbackInfo ci) {
		Jump event = new Jump((PlayerEntity) (Object) this, this.world.isClient);
		event.fire();
		if (event.cancelled())
			ci.cancel();
	}

	private boolean keepInventoryRedirect(GameRules gameRules, GameRules.Key<BooleanRule> key) {
		return EntityUtil.keepInventoryRedirect(this, gameRules, key);
	}

	@Redirect(
			method = "dropInventory",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/world/GameRules;getBoolean(Lnet/minecraft/world/GameRules$Key;)Z")
	)
	private boolean equipmentKeepInventoryRedirect(GameRules gameRules, GameRules.Key<BooleanRule> key) {
		return keepInventoryRedirect(gameRules, key);
	}

	@Redirect(
			method = "getXpToDrop",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/world/GameRules;getBoolean(Lnet/minecraft/world/GameRules$Key;)Z")
	)
	private boolean experienceKeepInventoryRedirect(GameRules gameRules, GameRules.Key<BooleanRule> key) {
		return keepInventoryRedirect(gameRules, key);
	}
}
