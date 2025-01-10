package beeisyou.mapdrawing.mixin.client;

import beeisyou.mapdrawing.MapDrawingClient;
import beeisyou.mapdrawing.color.ColorPalette;
import net.minecraft.client.gl.GlUniform;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.gl.ShaderProgramDefinition;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.util.Window;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ShaderProgram.class)
public class ShaderProgramMixin {

	@Unique
	public GlUniform map_drawing$colorPalette;

	@Inject(method = "initializeUniforms", at = @At("HEAD"))
	private void map_drawing$initializeUniforms(VertexFormat.DrawMode drawMode, Matrix4f viewMatrix, Matrix4f projectionMatrix, Window window, CallbackInfo ci) {
		if(this.map_drawing$colorPalette != null) {
			ColorPalette palette = MapDrawingClient.palette;
			if(palette != null && palette.buffer() != null) {
				this.map_drawing$colorPalette.set(palette.buffer().array());
			}
		}
	}

	@Inject(method = "set", at = @At("TAIL"))
	private void map_drawing$set(List<ShaderProgramDefinition.Uniform> uniforms, List<ShaderProgramDefinition.Sampler> samplers, CallbackInfo ci) {
		ShaderProgram self = (ShaderProgram) (Object) this;
		this.map_drawing$colorPalette = self.getUniform("ColorPalette");
	}

}
