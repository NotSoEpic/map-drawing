package wawa.mapwright;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.client.render.rendertype.VeilRenderType;
import foundry.veil.api.client.render.shader.uniform.ShaderUniform;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.joml.Matrix4f;
import org.joml.Vector2d;
import org.joml.Vector2dc;
import org.joml.Vector4dc;

import java.io.IOException;
import java.io.InputStream;

public class Rendering {

	public static class RenderTypes {
		public static final ResourceLocation PALETTE_SWAP = MapwrightClient.id("palette_swap");
		public static final ResourceLocation UV_REMAP = MapwrightClient.id("uv_remap");
		public static final ResourceLocation BACKGROUND = MapwrightClient.id("background");
	}

	public static class Textures {
		public static final ResourceLocation PALETTE = MapwrightClient.id("textures/gui/palette.png");
		public static final ResourceLocation HEAD_ICON = MapwrightClient.id("textures/gui/head_icon.png");
		public static final ResourceLocation BACKGROUND = MapwrightClient.id("background");
		public static final ResourceLocation BACKGROUND_FULL = MapwrightClient.id("textures/gui/sprites/background.png");
	}

	public static class Shaders {
		public static final ResourceLocation PALETTE_SWAP = MapwrightClient.id("palette_swap");
		public static final ResourceLocation UV_REMAP = MapwrightClient.id("uv_remap");
		public static final ResourceLocation BACKGROUND = MapwrightClient.id("background");
	}

	public static void renderHead(final GuiGraphics guiGraphics, final Vector2dc playerPosition, final Vector2dc mouseScreen, final double xOff, final double yOff, final float scale, final Vector4dc worldBounds) {
		final Vector2d pos = new Vector2d(playerPosition).add(xOff, yOff).mul(scale); // world position to screen position
		final float alpha = Helper.getMouseProximityFade(mouseScreen, pos);
		Helper.clampWithin(pos, worldBounds);
		Rendering.renderPlayerIcon(guiGraphics, pos.x - 8, pos.y - 8, Minecraft.getInstance().player, alpha);
	}

	public static void renderPlayerIcon(final GuiGraphics graphics, final double x, final double y, final LocalPlayer player, final float alpha) {
		final ResourceLocation skinTexture = player.getSkin().texture();

		final RenderType renderType = VeilRenderType.get(RenderTypes.UV_REMAP, skinTexture, Textures.HEAD_ICON);
		if(renderType == null) return;
		final ShaderUniform xOffset = VeilRenderSystem.setShader(Shaders.UV_REMAP).getOrCreateUniform("XOffset");

		final float rot = ((player.yRotO + 90) % 360) / 360.0f;
		final int frame = Math.round(rot * 16);

		xOffset.setFloat(0.0f);
		Rendering.renderTypeBlit(graphics, renderType, x, y, 0, 0.0f, 16.0f * frame, 16, 16, 16, 256, alpha);

		xOffset.setFloat(0.5f);
		Rendering.renderTypeBlit(graphics, renderType, x, y, 0, 0.0f, 16.0f * frame, 16, 16, 16, 256, alpha);
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

	public static void renderTypeBlit(final GuiGraphics guiGraphics, final RenderType renderType, final double x, final double y, final int blitOffset, final float uOffset, final float vOffset, final int uWidth, final int vHeight, final int textureWidth, final int textureHeight, final float alpha) {
		renderTypeBlit(guiGraphics, renderType, x, x + uWidth, y, y + vHeight, blitOffset, uWidth, vHeight, uOffset, vOffset, textureWidth, textureHeight, alpha);
	}

	public static void renderTypeBlit(final GuiGraphics guiGraphics, final RenderType renderType, final double x1, final double x2, final double y1, final double y2, final int blitOffset, final int uWidth, final int vHeight, final float uOffset, final float vOffset, final int textureWidth, final int textureHeight, final float alpha) {
		renderTypeBlit(guiGraphics, renderType, x1, x2, y1, y2, blitOffset, (uOffset + 0.0F) / (float)textureWidth, (uOffset + (float)uWidth) / (float)textureWidth, (vOffset + 0.0F) / (float)textureHeight, (vOffset + (float)vHeight) / (float)textureHeight, alpha);
	}

	public static void renderTypeBlit(final GuiGraphics guiGraphics, final RenderType renderType, final double x1, final double x2, final double y1, final double y2, final int blitOffset, final float minU, final float maxU, final float minV, final float maxV, final float alpha) {
		final Matrix4f matrix4f = guiGraphics.pose().last().pose();
		final BufferBuilder bufferBuilder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
		bufferBuilder.addVertex(matrix4f, (float)x1, (float)y1, (float)blitOffset).setUv(minU, minV).setColor(1, 1, 1, alpha);
		bufferBuilder.addVertex(matrix4f, (float)x1, (float)y2, (float)blitOffset).setUv(minU, maxV).setColor(1, 1, 1, alpha);
		bufferBuilder.addVertex(matrix4f, (float)x2, (float)y2, (float)blitOffset).setUv(maxU, maxV).setColor(1, 1, 1, alpha);
		bufferBuilder.addVertex(matrix4f, (float)x2, (float)y1, (float)blitOffset).setUv(maxU, minV).setColor(1, 1, 1, alpha);
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
}
