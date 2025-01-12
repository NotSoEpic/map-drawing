package wawa.wayfinder.mixin.client;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.server.packs.resources.ResourceProvider;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import wawa.wayfinder.WayfinderClient;
import wawa.wayfinder.color.ColorPalette;

@Mixin(ShaderInstance.class)
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

	@Inject(method = "<init>", at = @At("TAIL"))
	private void wayfinder$set(ResourceProvider resourceProvider, String name, VertexFormat vertexFormat, CallbackInfo ci) {
		ShaderInstance self = (ShaderInstance) (Object) this;
		this.wayfinder$colorPalette = self.getUniform("ColorPalette");
	}

}
