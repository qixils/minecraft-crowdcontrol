package dev.qixils.crowdcontrol.plugin.fabric.mixin;

import com.mojang.blaze3d.shaders.Effect;
import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.EffectInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EffectInstance.class)
public abstract class EffectInstanceMixin implements Effect, AutoCloseable {
	@Shadow
	private boolean dirty;

	@Redirect(method = "apply", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/shaders/Uniform;upload()V"))
	private void onUploadUniform(Uniform uniform) {
		if ("GameTime".equals(uniform.getName())) {
			uniform.set(RenderSystem.getShaderGameTime());
		}
		uniform.upload();
	}

	@Inject(method = "apply", at = @At("TAIL"))
	private void onApply(CallbackInfo ci) {
		// setting uniforms marks the shader as dirty
		// this reverses that
		this.dirty = false;
	}
}
