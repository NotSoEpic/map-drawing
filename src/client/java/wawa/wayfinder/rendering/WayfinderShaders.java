package wawa.wayfinder.rendering;

import wawa.wayfinder.Wayfinder;
import net.minecraft.client.gl.Defines;
import net.minecraft.client.gl.ShaderProgramKey;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;

public class WayfinderShaders {
	public static final Identifier PALETTE_SWAP_PATH = Wayfinder.id("core/texture_palette_swap");

	public static final RenderPhase.ShaderProgram PALETTE_SWAP =
			new RenderPhase.ShaderProgram(new ShaderProgramKey(PALETTE_SWAP_PATH, VertexFormats.POSITION_TEXTURE_COLOR, Defines.EMPTY));
}
