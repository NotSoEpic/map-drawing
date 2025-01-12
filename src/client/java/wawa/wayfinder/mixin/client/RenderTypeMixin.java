package wawa.wayfinder.mixin.client;

import com.mojang.blaze3d.vertex.MeshData;
import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.client.render.shader.program.ShaderProgram;
import net.minecraft.client.renderer.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import wawa.wayfinder.rendering.WayfinderRenderTypes;

@Mixin(RenderType.class)
public class RenderTypeMixin {


	@Inject(method = "draw", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/RenderType;setupRenderState()V", shift = At.Shift.AFTER))
	private void wayfinder$draw(MeshData meshData, CallbackInfo ci) {
		ShaderProgram shader = VeilRenderSystem.getShader();
		if(shader == null) {
			return;
		}

		WayfinderRenderTypes.beforeRenderTypeDraw(shader);
	}
}
