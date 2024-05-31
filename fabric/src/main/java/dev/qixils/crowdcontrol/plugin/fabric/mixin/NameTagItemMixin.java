package dev.qixils.crowdcontrol.plugin.fabric.mixin;

import dev.qixils.crowdcontrol.plugin.fabric.interfaces.Components;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.NameTagItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NameTagItem.class)
public class NameTagItemMixin {
	@Inject(method = "interactLivingEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getDisplayName()Lnet/minecraft/network/chat/Component;"))
	private void injected(ItemStack itemStack, Player player, LivingEntity livingEntity, InteractionHand interactionHand, CallbackInfo ci) {
		Components.VIEWER_MOB.get(livingEntity).setViewerSpawned(false);
	}
}
