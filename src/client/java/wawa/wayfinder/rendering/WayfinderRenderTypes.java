package wawa.wayfinder.rendering;

import foundry.veil.api.client.render.rendertype.VeilRenderType;
import foundry.veil.api.client.render.shader.program.ShaderProgram;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import wawa.wayfinder.Wayfinder;
import wawa.wayfinder.WayfinderClient;
import wawa.wayfinder.color.ColorPalette;

public class WayfinderRenderTypes {
	public static RenderType getPaletteSwap(ResourceLocation texture) {
		return VeilRenderType.get(Wayfinder.id("palette_swap"), texture.toString());
	}

	public static void beforeRenderTypeDraw(ShaderProgram shader) {
		if(shader.getName().equals(WayfinderShaders.PALETTE_SWAP)) {
			for (int i = 0; i < ColorPalette.SIZE; i++) {
				String name = "ColorPalette[%s]".formatted(i);
				shader.setVector(name, WayfinderClient.palette.vecList()[i]);
			}
		}
	}
}
