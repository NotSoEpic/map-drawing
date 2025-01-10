package beeisyou.mapdrawing.rendering;

import beeisyou.mapdrawing.MapDrawing;
import net.minecraft.client.gl.Defines;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.gl.ShaderProgramDefinition;
import net.minecraft.client.gl.ShaderProgramKey;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;

public class MapDrawingShaders {
	public static final Identifier PALETTE_SWAP_PATH = MapDrawing.id("core/texture_palette_swap");

	public static final RenderPhase.ShaderProgram PALETTE_SWAP =
			new RenderPhase.ShaderProgram(new ShaderProgramKey(PALETTE_SWAP_PATH, VertexFormats.POSITION_TEXTURE_COLOR, Defines.EMPTY));
}
