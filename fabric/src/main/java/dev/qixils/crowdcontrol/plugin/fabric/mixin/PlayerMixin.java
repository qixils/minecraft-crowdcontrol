package dev.qixils.crowdcontrol.plugin.fabric.mixin;

import dev.qixils.crowdcontrol.plugin.fabric.FabricCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.fabric.event.Jump;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public class PlayerMixin {
	@Inject(method = "jumpFromGround", at = @At("HEAD"), cancellable = true)
	public void jumpFromGround(CallbackInfo ci) {
		if (!FabricCrowdControlPlugin.isInstanceAvailable()) return;
		Player thiss = (Player) (Object) this;
		Jump jump = new Jump(thiss);
		FabricCrowdControlPlugin.getInstance().getEventManager().fire(jump);
		if (jump.cancelled())
			ci.cancel();
	}
}
