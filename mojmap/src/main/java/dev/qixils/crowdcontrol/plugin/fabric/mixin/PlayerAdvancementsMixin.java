package dev.qixils.crowdcontrol.plugin.fabric.mixin;

import dev.qixils.crowdcontrol.common.util.GameEvents;
import dev.qixils.crowdcontrol.plugin.fabric.ModdedCrowdControlPlugin;
import net.kyori.adventure.platform.fabric.FabricAudiences;
import net.minecraft.advancements.Advancement;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerAdvancements.class)
public class PlayerAdvancementsMixin {
	@Shadow
	private ServerPlayer player;

	@Inject(method = "award", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/PlayerList;broadcastMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/ChatType;Ljava/util/UUID;)V"))
	public void onAnnounceAdvancement(Advancement advancement, String string, CallbackInfoReturnable<Boolean> cir) {
		if (!ModdedCrowdControlPlugin.isInstanceAvailable()) return;

		ModdedCrowdControlPlugin.getInstance().emitGameEvent(player, GameEvents.advancement(FabricAudiences.toAdventure(advancement.getId())));
	}
}
