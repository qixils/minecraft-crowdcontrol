package dev.qixils.crowdcontrol.plugin.fabric.mixin;

import dev.qixils.crowdcontrol.plugin.fabric.commands.LootboxCommand;
import dev.qixils.crowdcontrol.plugin.fabric.event.Chat;
import net.minecraft.network.protocol.game.ServerboundChatPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public class ServerGamePacketListenerImplMixin {

	@Shadow
	public ServerPlayer player;

	@Inject(method = "handleContainerClose", at = @At("HEAD"))
	public void onHandleContainerClose(ServerboundContainerClosePacket serverboundContainerClosePacket, CallbackInfo ci) {
		LootboxCommand.onInventoryClose(((ServerGamePacketListenerImpl) (Object) this).player);
	}

	@Inject(method = "handleChat", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/PlayerList;broadcastMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/ChatType;Ljava/util/UUID;)V"), cancellable = true)
	public void onChat(ServerboundChatPacket packet, CallbackInfo ci) {
		var event = new Chat(player, packet.getMessage());
		event.fire();
		if (event.cancelled()) {
			ci.cancel();
		}
	}
}
