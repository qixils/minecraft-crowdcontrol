package dev.qixils.crowdcontrol.plugin.mojmap.mixin;

import dev.qixils.crowdcontrol.plugin.mojmap.commands.FreezeCommand;
import dev.qixils.crowdcontrol.plugin.mojmap.commands.FreezeCommand.FreezeData;
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
		//noinspection ConstantConditions
		ServerPlayer thiss = (ServerPlayer) (Object) this;
		UUID uuid = thiss.getUUID();

		if (!FreezeCommand.DATA.containsKey(uuid))
			return;

		FreezeData data = FreezeCommand.DATA.get(uuid);
		Location cur = new Location(thiss);
		Location dest = data.getDestination(cur);

		if (!cur.level().equals(dest.level()))
			return;

		if (!cur.equals(dest))
			dest.teleportHere(thiss);

		data.previousLocation = dest;
	}
}
