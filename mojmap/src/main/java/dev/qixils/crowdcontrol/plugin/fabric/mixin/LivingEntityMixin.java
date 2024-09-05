package dev.qixils.crowdcontrol.plugin.fabric.mixin;

import dev.qixils.crowdcontrol.plugin.fabric.interfaces.Components;
import dev.qixils.crowdcontrol.plugin.fabric.interfaces.OriginalDisplayName;
import dev.qixils.crowdcontrol.plugin.fabric.interfaces.ViewerMob;
import dev.qixils.crowdcontrol.plugin.fabric.utils.EntityUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity implements ViewerMob, OriginalDisplayName {

	public LivingEntityMixin(EntityType<?> entityType, Level level) {
		super(entityType, level);
	}

	@Unique
	boolean isViewerSpawned;
	@Unique
	Component originalDisplayName;

	@Override
	public boolean cc$isViewerSpawned() {
		return isViewerSpawned;
	}

	@Override
	public void cc$setViewerSpawned(boolean isViewerSpawned) {
		this.isViewerSpawned = isViewerSpawned;
	}

	@Override
	public @Nullable Component cc$getOriginalDisplayName() {
		return originalDisplayName;
	}

	@Override
	public void cc$setOriginalDisplayName(@Nullable Component originalDisplayName) {
		this.originalDisplayName = originalDisplayName;
	}

	@Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
	void onReadAdditionalSaveData(CompoundTag tag, CallbackInfo ci) {
		if (tag.contains(Components.VIEWER_MOB))
			isViewerSpawned = tag.getBoolean(Components.VIEWER_MOB);
		if (tag.contains(Components.ORIGINAL_DISPLAY_NAME))
			originalDisplayName = Component.Serializer.fromJson(tag.getString(Components.ORIGINAL_DISPLAY_NAME), registryAccess());
	}

	@Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
	void onAddAdditionalSaveData(CompoundTag tag, CallbackInfo ci) {
		if (isViewerSpawned)
			tag.putBoolean(Components.VIEWER_MOB, true);
		if (originalDisplayName != null)
			tag.putString(Components.ORIGINAL_DISPLAY_NAME, Component.Serializer.toJson(originalDisplayName, registryAccess()));
	}

	@Inject(method = "die", at = @At("HEAD"), cancellable = true)
	private void callDeathEvent(final DamageSource cause, final CallbackInfo ci) {
		EntityUtil.handleDie((LivingEntity) (Object) this, cause, ci);
	}
}
