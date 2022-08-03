package dev.qixils.crowdcontrol.plugin.fabric.mixin;

import dev.qixils.crowdcontrol.plugin.fabric.FabricCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.fabric.event.Jump;
import dev.qixils.crowdcontrol.plugin.fabric.interfaces.PlayerData;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public class PlayerMixin implements PlayerData {

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

	@Nullable
	public String gameModeEffect() {
		return ((Player) (Object) this).getEntityData().get(GAME_MODE_EFFECT);
	}

	@SuppressWarnings("ConstantConditions")
	public void gameModeEffect(@Nullable String value) {
		((Player) (Object) this).getEntityData().set(GAME_MODE_EFFECT, value);
	}
}
