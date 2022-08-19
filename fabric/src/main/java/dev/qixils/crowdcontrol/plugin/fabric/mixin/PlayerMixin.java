package dev.qixils.crowdcontrol.plugin.fabric.mixin;

import dev.qixils.crowdcontrol.plugin.fabric.FabricCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.fabric.event.Jump;
import dev.qixils.crowdcontrol.plugin.fabric.interfaces.PlayerData;
import dev.qixils.crowdcontrol.plugin.fabric.utils.EntityUtil;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameRules.BooleanValue;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class PlayerMixin implements PlayerData {

	private static final EntityDataAccessor<String> GAME_MODE_EFFECT = SynchedEntityData.defineId(Player.class, EntityDataSerializers.STRING);

	@Inject(method = "jumpFromGround", at = @At("HEAD"), cancellable = true)
	public void jumpFromGround(CallbackInfo ci) {
		if (!FabricCrowdControlPlugin.isInstanceAvailable()) return;
		Player thiss = (Player) (Object) this;
		Jump jump = new Jump(thiss);
		FabricCrowdControlPlugin.getInstance().getEventManager().fire(jump);
		if (jump.cancelled())
			ci.cancel();
	}

	@Inject(method = "defineSynchedData", at = @At("TAIL"))
	private void defineSynchedData(CallbackInfo ci) {
		((Player) (Object) this).getEntityData().define(GAME_MODE_EFFECT, null);
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

	@Nullable
	public String gameModeEffect() {
		return ((Player) (Object) this).getEntityData().get(GAME_MODE_EFFECT);
	}

	@SuppressWarnings("ConstantConditions")
	public void gameModeEffect(@Nullable String value) {
		((Player) (Object) this).getEntityData().set(GAME_MODE_EFFECT, value);
	}
}
