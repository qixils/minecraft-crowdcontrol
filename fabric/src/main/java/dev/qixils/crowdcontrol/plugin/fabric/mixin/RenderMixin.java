package dev.qixils.crowdcontrol.plugin.fabric.mixin;

import dev.qixils.crowdcontrol.plugin.fabric.FabricCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.fabric.client.FabricPlatformClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public abstract class RenderMixin extends DrawableHelper {
	@Shadow @Final private MinecraftClient client;

	@Shadow
	public abstract TextRenderer getTextRenderer();

	@Inject(method = "render", at = @At("RETURN"))
	private void render(MatrixStack matrixStack, float partialTick, CallbackInfo info) {
		if (client.options.hudHidden) return;
		if (client.options.debugEnabled) return;
		if (!FabricCrowdControlPlugin.CLIENT_INITIALIZED) return;
		FabricPlatformClient.get().proposalHandler.overlay.render(matrixStack, getTextRenderer(), client);
	}
}
