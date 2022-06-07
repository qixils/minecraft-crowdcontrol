package dev.qixils.crowdcontrol.plugin.mojmap.mixin;

import dev.qixils.crowdcontrol.plugin.mojmap.MojmapPlugin;
import dev.qixils.crowdcontrol.plugin.mojmap.event.Join;
import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerList.class)
public class PlayerListMixin {
	@Inject(method = "placeNewPlayer", at = @At(value = "RETURN"))
	private void impl$onInitPlayer_join(final Connection networkManager, final ServerPlayer mcPlayer, final CallbackInfo ci) {
		if (!MojmapPlugin.isInstanceAvailable()) return;
		MojmapPlugin.getInstance().getEventManager().fire(new Join(mcPlayer));
	}
}
