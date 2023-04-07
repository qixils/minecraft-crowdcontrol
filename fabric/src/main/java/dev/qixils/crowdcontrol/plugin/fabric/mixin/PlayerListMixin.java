package dev.qixils.crowdcontrol.plugin.fabric.mixin;

import dev.qixils.crowdcontrol.plugin.fabric.FabricCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.fabric.event.Join;
import dev.qixils.crowdcontrol.plugin.fabric.event.Leave;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerManager.class)
public class PlayerListMixin {
	@Inject(method = "onPlayerConnect", at = @At(value = "TAIL"))
	private void impl$onInitPlayer_join(final ClientConnection networkManager, final ServerPlayerEntity mcPlayer, final CallbackInfo ci) {
		if (!FabricCrowdControlPlugin.isInstanceAvailable()) return;
		FabricCrowdControlPlugin.getInstance().getEventManager().fire(new Join(mcPlayer));
	}

	@Inject(method = "remove", at = @At(value = "HEAD"))
	private void impl$onRemovePlayer_leave(final ServerPlayerEntity mcPlayer, final CallbackInfo ci) {
		if (!FabricCrowdControlPlugin.isInstanceAvailable()) return;
		FabricCrowdControlPlugin.getInstance().getEventManager().fire(new Leave(mcPlayer));
	}
}
