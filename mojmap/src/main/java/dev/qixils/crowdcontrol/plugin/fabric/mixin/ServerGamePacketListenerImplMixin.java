package dev.qixils.crowdcontrol.plugin.fabric.mixin;

import dev.qixils.crowdcontrol.plugin.fabric.commands.LootboxCommand;
import dev.qixils.crowdcontrol.plugin.fabric.event.Chat;
import net.minecraft.network.chat.LastSeenMessages;
import net.minecraft.network.protocol.game.ServerboundChatPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(ServerGamePacketListenerImpl.class)
public class ServerGamePacketListenerImplMixin {

	@Shadow
	public ServerPlayer player;

	@Inject(method = "handleContainerClose", at = @At("HEAD"))
	public void onHandleContainerClose(ServerboundContainerClosePacket serverboundContainerClosePacket, CallbackInfo ci) {
		LootboxCommand.onInventoryClose(((ServerGamePacketListenerImpl) (Object) this).getPlayer());
	}

	@Inject(method = {"lambda$handleChat$10", "method_44900"}, at = @At("HEAD"), cancellable = true)
	public void onChat(ServerboundChatPacket packet, Optional<LastSeenMessages> optional, CallbackInfo ci) {
		var event = new Chat(player, packet.message());
		event.fire();
		if (event.cancelled()) {
			ci.cancel();
		}
	}
}
