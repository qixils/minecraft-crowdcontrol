package dev.qixils.crowdcontrol.plugin.fabric.mixin;

import dev.qixils.crowdcontrol.common.util.GameEvents;
import dev.qixils.crowdcontrol.plugin.fabric.ModdedCrowdControlPlugin;
import net.kyori.adventure.platform.modcommon.MinecraftAudiences;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerAdvancements.class)
public class PlayerAdvancementsMixin {
	@Shadow
	private ServerPlayer player;

	@Inject(method = "lambda$award$0", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/PlayerList;broadcastSystemMessage(Lnet/minecraft/network/chat/Component;Z)V"))
	public void onAnnounceAdvancement(AdvancementHolder advancementHolder, DisplayInfo displayInfo, CallbackInfo ci) {
		if (!ModdedCrowdControlPlugin.isInstanceAvailable()) return;

		ModdedCrowdControlPlugin.getInstance().emitGameEvent(player, GameEvents.advancement(MinecraftAudiences.asAdventure(advancementHolder.id())));
	}
}
