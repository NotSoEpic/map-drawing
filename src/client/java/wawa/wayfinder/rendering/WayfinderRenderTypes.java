package wawa.wayfinder.rendering;

import net.minecraft.Util;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.function.Function;

public class WayfinderRenderTypes {

	private static final Function<ResourceLocation, RenderType> PALETTE_SWAP = Util.memoize((id) -> RenderType.create(
			"texture_palette_swap",
			DefaultVertexFormat.POSITION_TEX_COLOR,
			VertexFormat.Mode.QUADS,
			786432,
			RenderType.CompositeState.builder()
					.setTextureState(new RenderStateShard.TextureStateShard(id, false, false))
					.setShaderState(RenderStateShard.POSITION_TEX_SHADER)
					.setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
					.setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST)
					.setWriteMaskState(RenderStateShard.COLOR_WRITE)
					.createCompositeState(false)));

	public static RenderType getPaletteSwap(ResourceLocation texture) {
		return PALETTE_SWAP.apply(texture);
	}
}
