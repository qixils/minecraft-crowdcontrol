package dev.qixils.crowdcontrol.plugin.fabric.mixin;

import dev.qixils.crowdcontrol.plugin.fabric.ModdedCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.fabric.event.Join;
import dev.qixils.crowdcontrol.plugin.fabric.event.Leave;
import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerList.class)
public class PlayerListMixin {
	@Inject(method = "placeNewPlayer", at = @At(value = "RETURN"))
	private void impl$onInitPlayer_join(Connection connection, ServerPlayer serverPlayer, CommonListenerCookie commonListenerCookie, CallbackInfo ci) {
		if (!ModdedCrowdControlPlugin.isInstanceAvailable()) return;
		ModdedCrowdControlPlugin.getInstance().getEventManager().fire(new Join(serverPlayer));
	}

	@Inject(method = "remove", at = @At(value = "HEAD"))
	private void impl$onRemovePlayer_leave(final ServerPlayer mcPlayer, final CallbackInfo ci) {
		if (!ModdedCrowdControlPlugin.isInstanceAvailable()) return;
		ModdedCrowdControlPlugin.getInstance().getEventManager().fire(new Leave(mcPlayer));
	}
}
