package dev.qixils.crowdcontrol.plugin.fabric.mixin;

import dev.qixils.crowdcontrol.plugin.fabric.ModdedCrowdControlPlugin;
import net.kyori.adventure.text.renderer.TranslatableComponentRenderer;
import net.kyori.adventure.translation.GlobalTranslator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Locale;

@Mixin(value = GlobalTranslator.class, remap = false)
public interface GlobalTranslatorMixin {

	@Inject(method = "renderer", at = @At("HEAD"), cancellable = true)
	private static void onGetRender(CallbackInfoReturnable<TranslatableComponentRenderer<Locale>> cir) {
		// TODO: this does break any other registered translators but uhm...... i dont expect there to be any
		if (ModdedCrowdControlPlugin.isInstanceAvailable()) {
			cir.setReturnValue(ModdedCrowdControlPlugin.getInstance().getTranslator());
		}
	}
}
