package dev.qixils.crowdcontrol.plugin.mojmap.mixin;

import dev.qixils.crowdcontrol.plugin.mojmap.commands.FreezeCommand;
import dev.qixils.crowdcontrol.plugin.mojmap.utils.Location;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@SuppressWarnings("resource")
@Mixin(ServerPlayer.class)
public class ServerPlayerMixin {
	@Inject(method = "tick", at = @At("HEAD"))
	void onTick(CallbackInfo ci) {
		ServerPlayer thiss = (ServerPlayer) (Object) this;
		UUID uuid = thiss.getUUID();
		if (!FreezeCommand.FROZEN_PLAYERS.containsKey(uuid)) return;
		Location curLocation = new Location(thiss);
		Location destLocation = FreezeCommand.FROZEN_PLAYERS.get(uuid).withRotation(curLocation.yaw(), curLocation.pitch());
		if (!curLocation.level().equals(destLocation.level())) return;
		if (curLocation.x() != destLocation.x() || curLocation.y() != destLocation.y() || curLocation.z() != destLocation.z()) {
			destLocation.teleportHere(thiss);
		}
	}
}
