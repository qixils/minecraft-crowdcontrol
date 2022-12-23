package dev.qixils.crowdcontrol.plugin.fabric.mixin;

import dev.qixils.crowdcontrol.plugin.fabric.interfaces.Components;
import dev.qixils.crowdcontrol.plugin.fabric.interfaces.MovementStatus;
import dev.qixils.crowdcontrol.plugin.fabric.utils.EntityUtil;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameRules.BooleanValue;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class PlayerMixin extends LivingEntity {

	// dummy constructor
	protected PlayerMixin(EntityType<? extends LivingEntity> entityType, Level level) {
		super(entityType, level);
	}

	@Inject(method = "jumpFromGround", at = @At("HEAD"), cancellable = true)
	public void jumpFromGround(CallbackInfo ci) {
		if (Components.MOVEMENT_STATUS.get(this).isProhibited(MovementStatus.Type.JUMP)) {
			ci.cancel();
			if (!this.level.isClientSide) {
				//noinspection DataFlowIssue
				((ServerPlayer) (Object) this).connection.teleport(this.getX(), this.getY(), this.getZ(), this.getYRot(), this.getXRot());
			}
		}
	}

	private boolean keepInventoryRedirect(GameRules gameRules, GameRules.Key<BooleanValue> key) {
		return EntityUtil.keepInventoryRedirect(this, gameRules, key);
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
