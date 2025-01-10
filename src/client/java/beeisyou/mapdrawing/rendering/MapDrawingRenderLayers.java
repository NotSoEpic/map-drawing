package beeisyou.mapdrawing.rendering;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;
import net.minecraft.util.TriState;
import net.minecraft.util.Util;

import java.util.function.Function;

public class MapDrawingRenderLayers {

	private static final Function<Identifier, RenderLayer> PALETTE_SWAP = Util.memoize((id) -> RenderLayer.of(
			"texture_palette_swap",
			VertexFormats.POSITION_TEXTURE_COLOR,
			VertexFormat.DrawMode.QUADS,
			786432,
			RenderLayer.MultiPhaseParameters.builder()
					.texture(new RenderPhase.Texture(id, TriState.DEFAULT, false))
					.program(MapDrawingShaders.PALETTE_SWAP)
					.transparency(RenderPhase.TRANSLUCENT_TRANSPARENCY)
					.depthTest(RenderPhase.LEQUAL_DEPTH_TEST)
					.writeMaskState(RenderPhase.COLOR_MASK)
					.build(false)));

	public static RenderLayer getPaletteSwap(Identifier texture) {
		return PALETTE_SWAP.apply(texture);
	}
}
