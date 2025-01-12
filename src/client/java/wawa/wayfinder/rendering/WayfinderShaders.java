package wawa.wayfinder.rendering;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import wawa.wayfinder.Wayfinder;

public class WayfinderShaders {
	public static final ResourceLocation PALETTE_SWAP_PATH = Wayfinder.id("core/texture_palette_swap");

//	public static final RenderStateShard.ShaderStateShard PALETTE_SWAP =
//			new RenderStateShard.ShaderStateShard(() -> new ShaderInstance(PALETTE_SWAP_PATH, DefaultVertexFormat.POSITION_TEX_COLOR));

}
