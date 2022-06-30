package dev.qixils.crowdcontrol.plugin.mojmap.mixin;

import dev.qixils.crowdcontrol.plugin.mojmap.MojmapPlugin;
import dev.qixils.crowdcontrol.plugin.mojmap.event.Jump;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public class PlayerMixin {
	@Inject(method = "jumpFromGround", at = @At("HEAD"), cancellable = true)
	public void jumpFromGround(CallbackInfo ci) {
		if (!MojmapPlugin.isInstanceAvailable()) return;
		Player thiss = (Player) (Object) this;
		Jump jump = new Jump(thiss);
		MojmapPlugin.getInstance().getEventManager().fire(jump);
		if (jump.cancelled())
			ci.cancel();
	}
}
