package wawa.wayfinder.mixin.client;

import wawa.wayfinder.WayfinderClient;
import wawa.wayfinder.color.ColorPalette;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.List;
import net.minecraft.client.renderer.CompiledShaderProgram;
import net.minecraft.client.renderer.ShaderProgramConfig;

@Mixin(CompiledShaderProgram.class)
public class ShaderProgramMixin {

	@Unique
	public Uniform wayfinder$colorPalette;

	@Inject(method = "setDefaultUniforms", at = @At("HEAD"))
	private void wayfinder$initializeUniforms(VertexFormat.Mode drawMode, Matrix4f viewMatrix, Matrix4f projectionMatrix, Window window, CallbackInfo ci) {
		if(this.wayfinder$colorPalette != null) {
			ColorPalette palette = WayfinderClient.palette;
			if(palette != null && palette.buffer() != null) {
				this.wayfinder$colorPalette.set(palette.buffer().array());
			}
		}
	}

	@Inject(method = "setupUniforms", at = @At("TAIL"))
	private void wayfinder$set(List<ShaderProgramConfig.Uniform> uniforms, List<ShaderProgramConfig.Sampler> samplers, CallbackInfo ci) {
		CompiledShaderProgram self = (CompiledShaderProgram) (Object) this;
		this.wayfinder$colorPalette = self.getUniform("ColorPalette");
	}

}
