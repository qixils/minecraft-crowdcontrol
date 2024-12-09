package dev.qixils.crowdcontrol.plugin.fabric.mixin;

import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.systems.RenderSystem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// TODO: sources broke so i could not find where this is used now. might be a more optimal place for this
//  OH AND the dirty flag is lost
@Mixin(Uniform.class)
public abstract class UniformMixin {
	@Shadow
	public abstract String getName();

	@Shadow
	public abstract void set(float f);

	@Inject(method = "upload", at = @At("HEAD"))
	public void upload(CallbackInfo ci) {
		if ("GameTime".equals(getName()))
			set(RenderSystem.getShaderGameTime());
	}
}