package wawa.wayfinder;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.joml.Matrix4f;

import java.io.IOException;
import java.io.InputStream;

public class Rendering {
	public static final ResourceLocation PALETTE_SWAP_RENDER_TYPE = WayfinderClient.id("palette_swap");
	public static final ResourceLocation PALETTE_TEXTURE = WayfinderClient.id("textures/gui/palette.png");

	public static NativeImage getPaletteTexture() {
		ResourceManager resourceManager = Minecraft.getInstance().getResourceManager();
		NativeImage image = null;

		try {
			Resource resource = resourceManager.getResourceOrThrow(PALETTE_TEXTURE);

			try(InputStream stream = resource.open()) {
				image = NativeImage.read(stream);
			}

		} catch (IOException ignored) {}

		return image;
	}

	public static void renderTypeBlit(GuiGraphics guiGraphics, RenderType renderType, int x, int y, int blitOffset, float uOffset, float vOffset, int uWidth, int vHeight, int textureWidth, int textureHeight) {
		renderTypeBlit(guiGraphics, renderType, x, x + uWidth, y, y + vHeight, blitOffset, uWidth, vHeight, uOffset, vOffset, textureWidth, textureHeight);
	}

	public static void renderTypeBlit(GuiGraphics guiGraphics, RenderType renderType, int x1, int x2, int y1, int y2, int blitOffset, int uWidth, int vHeight, float uOffset, float vOffset, int textureWidth, int textureHeight) {
		renderTypeBlit(guiGraphics, renderType, x1, x2, y1, y2, blitOffset, (uOffset + 0.0F) / (float)textureWidth, (uOffset + (float)uWidth) / (float)textureWidth, (vOffset + 0.0F) / (float)textureHeight, (vOffset + (float)vHeight) / (float)textureHeight);
	}

	public static void renderTypeBlit(GuiGraphics guiGraphics, RenderType renderType, int x1, int x2, int y1, int y2, int blitOffset, float minU, float maxU, float minV, float maxV) {
		Matrix4f matrix4f = guiGraphics.pose().last().pose();
		BufferBuilder bufferBuilder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
		bufferBuilder.addVertex(matrix4f, (float)x1, (float)y1, (float)blitOffset).setUv(minU, minV);
		bufferBuilder.addVertex(matrix4f, (float)x1, (float)y2, (float)blitOffset).setUv(minU, maxV);
		bufferBuilder.addVertex(matrix4f, (float)x2, (float)y2, (float)blitOffset).setUv(maxU, maxV);
		bufferBuilder.addVertex(matrix4f, (float)x2, (float)y1, (float)blitOffset).setUv(maxU, minV);
		renderType.draw(bufferBuilder.buildOrThrow());
	}
}
