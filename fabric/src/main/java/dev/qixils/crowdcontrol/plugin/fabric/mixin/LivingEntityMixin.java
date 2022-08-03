package dev.qixils.crowdcontrol.plugin.fabric.mixin;

import dev.qixils.crowdcontrol.plugin.fabric.FabricCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.fabric.event.Death;
import dev.qixils.crowdcontrol.plugin.fabric.interfaces.LivingEntityData;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(LivingEntity.class)
public class LivingEntityMixin implements LivingEntityData {

	private static final EntityDataAccessor<Optional<Component>> ORIGINAL_DISPLAY_NAME = SynchedEntityData.defineId(LivingEntity.class, EntityDataSerializers.OPTIONAL_COMPONENT);
	private static final EntityDataAccessor<Boolean> VIEWER_SPAWNED = SynchedEntityData.defineId(LivingEntity.class, EntityDataSerializers.BOOLEAN);

	@Shadow protected boolean dead;

	@Inject(method = "die", at = @At("HEAD"), cancellable = true)
	private void callDeathEvent(final DamageSource cause, final CallbackInfo ci) {
		if (!FabricCrowdControlPlugin.isInstanceAvailable()) return;
		if (this.dead) return;
		Death event = new Death((LivingEntity) (Object) this);
		FabricCrowdControlPlugin.getInstance().getEventManager().fire(event);
		if (event.cancelled()) ci.cancel();
	}

	@Inject(method = "defineSynchedData", at = @At("TAIL"))
	private void defineSynchedData(CallbackInfo ci) {
		((LivingEntity) (Object) this).getEntityData().define(ORIGINAL_DISPLAY_NAME, Optional.empty());
		((LivingEntity) (Object) this).getEntityData().define(VIEWER_SPAWNED, false);
	}

	public Optional<Component> originalDisplayName() {
		return ((LivingEntity) (Object) this).getEntityData().get(ORIGINAL_DISPLAY_NAME);
	}

	public void originalDisplayName(@Nullable Component value) {
		originalDisplayName(Optional.ofNullable(value));
	}

	public void originalDisplayName(Optional<Component> value) {
		((LivingEntity) (Object) this).getEntityData().set(ORIGINAL_DISPLAY_NAME, value);
	}

	public boolean viewerSpawned() {
		return ((LivingEntity) (Object) this).getEntityData().get(VIEWER_SPAWNED);
	}

	public void viewerSpawned(boolean value) {
		((LivingEntity) (Object) this).getEntityData().set(VIEWER_SPAWNED, value);
	}
}
