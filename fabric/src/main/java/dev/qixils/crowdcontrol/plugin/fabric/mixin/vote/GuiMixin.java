package dev.qixils.crowdcontrol.plugin.fabric.mixin.vote;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.qixils.crowdcontrol.plugin.fabric.client.vote.ProposalHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class GuiMixin {
	@Shadow
	@Final
	private Minecraft minecraft;

	@Shadow
	public abstract Font getFont();

	@Inject(method = "render", at = @At("RETURN"))
	private void render(PoseStack poseStack, float partialTick, CallbackInfo info) {
		if (minecraft.options.hideGui) return;
		if (minecraft.options.renderDebug) return;
		ProposalHandler.getOptional().ifPresent(handler -> handler.overlay.render(poseStack, getFont(), minecraft));
	}
}
