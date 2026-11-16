package dev.qixils.crowdcontrol.plugin.fabric.mixin;

import com.llamalad7.mixinextras.sugar.Cancellable;
import com.llamalad7.mixinextras.sugar.Local;
import dev.qixils.crowdcontrol.plugin.fabric.interfaces.Components;
import dev.qixils.crowdcontrol.plugin.fabric.interfaces.OriginalDisplayName;
import dev.qixils.crowdcontrol.plugin.fabric.interfaces.ViewerMob;
import dev.qixils.crowdcontrol.plugin.fabric.utils.EntityUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity implements ViewerMob, OriginalDisplayName {

	public LivingEntityMixin(EntityType<?> entityType, Level level) {
		super(entityType, level);
	}

	@Unique
	boolean cc$isViewerSpawned;
	@Unique
	Component cc$originalDisplayName;

	@Override
	public boolean cc$isViewerSpawned() {
		return cc$isViewerSpawned;
	}

	@Override
	public void cc$setViewerSpawned(boolean isViewerSpawned) {
		this.cc$isViewerSpawned = isViewerSpawned;
	}

	@Override
	public @Nullable Component cc$getOriginalDisplayName() {
		return cc$originalDisplayName;
	}

	@Override
	public void cc$setOriginalDisplayName(@Nullable Component originalDisplayName) {
		this.cc$originalDisplayName = originalDisplayName;
	}

	@Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
	void onReadAdditionalSaveData(CompoundTag tag, CallbackInfo ci) {
		cc$isViewerSpawned = tag.getBooleanOr(Components.VIEWER_MOB, false);
		cc$originalDisplayName = tag
			.read(Components.ORIGINAL_DISPLAY_NAME, ComponentSerialization.CODEC)
			.orElse(null);
	}

	@Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
	void onAddAdditionalSaveData(CompoundTag tag, CallbackInfo ci) {
		if (cc$isViewerSpawned)
			tag.putBoolean(Components.VIEWER_MOB, true);
		if (cc$originalDisplayName != null)
			tag.store(Components.ORIGINAL_DISPLAY_NAME, ComponentSerialization.CODEC, cc$originalDisplayName);
	}

	@Inject(method = "die", at = @At("HEAD"), cancellable = true)
	private void callDeathEvent(final DamageSource cause, final CallbackInfo ci) {
		EntityUtil.handleDie((LivingEntity) (Object) this, cause, ci);
	}

	@ModifyVariable(method = "hurtServer", at = @At("HEAD"), ordinal = 0, argsOnly = true)
	private float callDamageEvent(float amount, @Local(argsOnly = true) DamageSource source, @Cancellable CallbackInfoReturnable<Boolean> cir) {
		return EntityUtil.handleDamage(this, source, amount, cir);
	}

	// player stuff

	@Inject(method = "jumpFromGround", at = @At("HEAD"), cancellable = true)
	public void jumpFromGround(CallbackInfo ci) {
	}
}
