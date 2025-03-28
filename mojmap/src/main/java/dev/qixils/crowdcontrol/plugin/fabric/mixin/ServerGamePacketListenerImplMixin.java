package dev.qixils.crowdcontrol.plugin.fabric.mixin;

import dev.qixils.crowdcontrol.plugin.fabric.commands.LootboxCommand;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public class ServerGamePacketListenerImplMixin {

	@Inject(method = "handleContainerClose", at = @At("HEAD"))
	public void onHandleContainerClose(ServerboundContainerClosePacket serverboundContainerClosePacket, CallbackInfo ci) {
		LootboxCommand.onInventoryClose(((ServerGamePacketListenerImpl) (Object) this).getPlayer());
	}
}
