package wawa.wayfinder;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.client.render.rendertype.VeilRenderType;
import foundry.veil.api.client.render.shader.program.ShaderProgram;
import foundry.veil.api.client.render.shader.uniform.ShaderUniform;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.metadata.gui.GuiSpriteScaling;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.joml.Matrix4f;
import org.joml.Vector2dc;

import java.io.IOException;
import java.io.InputStream;

public class Rendering {

	public static class RenderTypes {
		public static final ResourceLocation PALETTE_SWAP = WayfinderClient.id("palette_swap");
		public static final ResourceLocation UV_REMAP = WayfinderClient.id("uv_remap");
		public static final ResourceLocation BACKGROUND = WayfinderClient.id("background");
	}

	public static class Textures {
		public static final ResourceLocation PALETTE = WayfinderClient.id("textures/gui/palette.png");
		public static final ResourceLocation HEAD_ICON = WayfinderClient.id("textures/gui/head_icon.png");
		private static final ResourceLocation BACKGROUND = WayfinderClient.id("background");
		private static final ResourceLocation BACKGROUND_FULL = WayfinderClient.id("textures/gui/sprites/background.png");
	}

	public static class Shaders {
		public static final ResourceLocation PALETTE_SWAP = WayfinderClient.id("palette_swap");
		public static final ResourceLocation UV_REMAP = WayfinderClient.id("uv_remap");
		public static final ResourceLocation BACKGROUND = WayfinderClient.id("background");
	}

	public static void renderPlayerIcon(final GuiGraphics graphics, final double x, final double y, final LocalPlayer player) {
		final ResourceLocation skinTexture = player.getSkin().texture();

		final RenderType renderType = VeilRenderType.get(RenderTypes.UV_REMAP, skinTexture, Textures.HEAD_ICON);
		if(renderType == null) return;
		final ShaderUniform xOffset = VeilRenderSystem.setShader(Shaders.UV_REMAP).getOrCreateUniform("XOffset");

		final float rot = ((player.yRotO + 90) % 360) / 360.0f;
		final int frame = Math.round(rot * 16);

		xOffset.setFloat(0.0f);
		Rendering.renderTypeBlit(graphics, renderType, x, y, 0, 0.0f, 16.0f * frame, 16, 16, 16, 256);

		xOffset.setFloat(0.5f);
		Rendering.renderTypeBlit(graphics, renderType, x, y, 0, 0.0f, 16.0f * frame, 16, 16, 16, 256);
	}

	public static NativeImage getPaletteTexture() {
		final ResourceManager resourceManager = Minecraft.getInstance().getResourceManager();
		NativeImage image = null;

		try {
			final Resource resource = resourceManager.getResourceOrThrow(Textures.PALETTE);

			try(final InputStream stream = resource.open()) {
				image = NativeImage.read(stream);
			}

		} catch (final IOException ignored) {}

		return image;
	}

	public static void renderTypeBlit(final GuiGraphics guiGraphics, final RenderType renderType, final double x, final double y, final int blitOffset, final float uOffset, final float vOffset, final int uWidth, final int vHeight, final int textureWidth, final int textureHeight) {
		renderTypeBlit(guiGraphics, renderType, x, x + uWidth, y, y + vHeight, blitOffset, uWidth, vHeight, uOffset, vOffset, textureWidth, textureHeight);
	}

	public static void renderTypeBlit(final GuiGraphics guiGraphics, final RenderType renderType, final double x1, final double x2, final double y1, final double y2, final int blitOffset, final int uWidth, final int vHeight, final float uOffset, final float vOffset, final int textureWidth, final int textureHeight) {
		renderTypeBlit(guiGraphics, renderType, x1, x2, y1, y2, blitOffset, (uOffset + 0.0F) / (float)textureWidth, (uOffset + (float)uWidth) / (float)textureWidth, (vOffset + 0.0F) / (float)textureHeight, (vOffset + (float)vHeight) / (float)textureHeight);
	}

	public static void renderTypeBlit(final GuiGraphics guiGraphics, final RenderType renderType, final double x1, final double x2, final double y1, final double y2, final int blitOffset, final float minU, final float maxU, final float minV, final float maxV) {
		final Matrix4f matrix4f = guiGraphics.pose().last().pose();
		final BufferBuilder bufferBuilder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
		bufferBuilder.addVertex(matrix4f, (float)x1, (float)y1, (float)blitOffset).setUv(minU, minV);
		bufferBuilder.addVertex(matrix4f, (float)x1, (float)y2, (float)blitOffset).setUv(minU, maxV);
		bufferBuilder.addVertex(matrix4f, (float)x2, (float)y2, (float)blitOffset).setUv(maxU, maxV);
		bufferBuilder.addVertex(matrix4f, (float)x2, (float)y1, (float)blitOffset).setUv(maxU, minV);
		renderType.draw(bufferBuilder.buildOrThrow());
	}

	public static void renderTypeBlitUV1(final GuiGraphics guiGraphics, final RenderType renderType,
										 final int x, final int y, final int width, final int height,
										 final int textureWidth, final int textureHeight, final int blitOffset,
										 final float u, final float v) {
		final Matrix4f matrix4f = guiGraphics.pose().last().pose();
		final BufferBuilder bufferBuilder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_LIGHTMAP_COLOR);
		bufferBuilder.addVertex(matrix4f, (float)x, (float)y, (float)blitOffset)
				.setUv(u / textureWidth, v / textureHeight)
				.setUv2(x, y).setColor(-1);

		bufferBuilder.addVertex(matrix4f, (float)x, (float)y + height, (float)blitOffset)
				.setUv(u / textureWidth, (v + height) / textureHeight)
				.setUv2(x, y + height).setColor(-1);

		bufferBuilder.addVertex(matrix4f, (float)x + width, (float)y + height, (float)blitOffset)
				.setUv((u + width) / textureWidth, (v + height) / textureHeight)
				.setUv2(x + width, y + height).setColor(-1);

		bufferBuilder.addVertex(matrix4f, (float)x + width, (float)y, (float)blitOffset)
				.setUv((u + width) / textureWidth, v / textureHeight)
				.setUv2(x + width, y).setColor(-1);

		renderType.draw(bufferBuilder.buildOrThrow());
	}

	public static void renderTypeBlitUV1Tile(final GuiGraphics guiGraphics, final RenderType renderType,
											 final int x, final int y, final int width, final int height,
											 final int sliceWidth, final int sliceHeight,
											 final int textureWidth, final int textureHeight, final int blitOffset,
											 final float u, final float v) {
		for(int i = 0; i < width; i += sliceWidth) {
			int j = Math.min(sliceWidth, width - i);

			for(int k = 0; k < height; k += sliceHeight) {
				int l = Math.min(sliceHeight, height - k);
				renderTypeBlitUV1(guiGraphics, renderType,
						x+i, y+k, j, l, textureWidth, textureHeight, blitOffset, u, v);
			}
		}
	}

	public static void renderMapNineslice(final GuiGraphics guiGraphics, final int x, final int y, final int width, final int height, final int blitOffset, final Vector2dc backgroundTranslation, final float scale) {
		final TextureAtlasSprite textureatlassprite = Minecraft.getInstance().getGuiSprites().getSprite(Textures.BACKGROUND);
		final GuiSpriteScaling guispritescaling = Minecraft.getInstance().getGuiSprites().getSpriteScaling(textureatlassprite);
		if (guispritescaling instanceof final GuiSpriteScaling.NineSlice nineslice) {
			final RenderType renderType = VeilRenderType.get(RenderTypes.BACKGROUND, Textures.BACKGROUND_FULL);
			if (renderType == null) return;
			final ShaderProgram backgroundProgram = VeilRenderSystem.setShader(Shaders.BACKGROUND);
			if (backgroundProgram == null) return;
			backgroundProgram.getOrCreateUniform("ScreenCenter").setVectorI(x + width / 2, y + height / 2);
			backgroundProgram.getOrCreateUniform("Translation").setVector((float) backgroundTranslation.x(), (float) backgroundTranslation.y());
//			backgroundProgram.getOrCreateUniform("Scale").setFloat(scale);

			// this is awful...................
			final int leftWidth = nineslice.border().left();
			final int rightWidth = nineslice.border().right();
			final int centerWidth = width - leftWidth - rightWidth;
			final int topHeight = nineslice.border().top();
			final int bottomHeight = nineslice.border().bottom();
			final int centerHeight = height - topHeight - bottomHeight;

			final int rightX = x + width - rightWidth;
			final int bottomY = y + height - bottomHeight;

			final int centerSliceWidth = nineslice.width() - leftWidth - rightWidth;
			final int centerSliceHeight = nineslice.height() - topHeight - bottomHeight;

			final int sliceU = nineslice.width() - rightWidth;
			final int sliceV = nineslice.height() - bottomHeight;


			renderTypeBlitUV1(guiGraphics, renderType, x, y,
					leftWidth, topHeight,
					nineslice.width(), nineslice.height(), blitOffset,
					0, 0);
			renderTypeBlitUV1Tile(guiGraphics, renderType, x + leftWidth, y,
					centerWidth, topHeight,
					centerSliceWidth, topHeight,
					nineslice.width(), nineslice.height(), blitOffset,
					leftWidth, 0);
			renderTypeBlitUV1(guiGraphics, renderType, rightX, y,
					rightWidth, topHeight,
					nineslice.width(), nineslice.height(), blitOffset,
					sliceU, 0);

			renderTypeBlitUV1Tile(guiGraphics, renderType, x, y + topHeight,
					leftWidth, centerHeight,
					leftWidth, centerSliceHeight,
					nineslice.width(), nineslice.height(), blitOffset,
					0, topHeight);
			renderTypeBlitUV1Tile(guiGraphics, renderType, x + leftWidth, y + topHeight,
					centerWidth, centerHeight,
					centerSliceWidth, centerSliceHeight,
					nineslice.width(), nineslice.height(), blitOffset,
					leftWidth, topHeight);
			renderTypeBlitUV1Tile(guiGraphics, renderType, rightX, y + topHeight,
					rightWidth, centerHeight,
					rightWidth, centerSliceHeight,
					nineslice.width(), nineslice.height(), blitOffset,
					sliceU, topHeight);

			renderTypeBlitUV1(guiGraphics, renderType, x, bottomY,
					leftWidth, bottomHeight,
					nineslice.width(), nineslice.height(), blitOffset,
					0, sliceV);
			renderTypeBlitUV1Tile(guiGraphics, renderType, x + leftWidth, bottomY,
					centerWidth, bottomHeight,
					centerSliceWidth, bottomHeight,
					nineslice.width(), nineslice.height(), blitOffset,
					leftWidth, sliceV);
			renderTypeBlitUV1(guiGraphics, renderType, rightX, bottomY,
					rightWidth, bottomHeight,
					nineslice.width(), nineslice.height(), blitOffset,
					sliceU, sliceV);
		}
	}
}
