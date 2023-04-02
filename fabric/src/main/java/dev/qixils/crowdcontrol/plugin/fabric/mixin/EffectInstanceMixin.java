package dev.qixils.crowdcontrol.plugin.fabric.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gl.EffectShaderProgram;
import net.minecraft.client.gl.GlUniform;
import net.minecraft.client.gl.JsonEffectShaderProgram;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(JsonEffectShaderProgram.class)
public abstract class EffectInstanceMixin implements EffectShaderProgram, AutoCloseable {
	@Shadow
	private boolean uniformStateDirty;

	@Redirect(method = "enable", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gl/GlUniform;upload()V"))
	private void onUploadUniform(GlUniform uniform) {
		if ("GameTime".equals(uniform.getName())) {
			uniform.set(RenderSystem.getShaderGameTime());
		}
		uniform.upload();
	}

	@Inject(method = "enable", at = @At("TAIL"))
	private void onApply(CallbackInfo ci) {
		// setting uniforms marks the shader as dirty
		// this reverses that
		this.uniformStateDirty = false;
	}
}
