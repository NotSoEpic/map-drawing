package wawa.mapwright.map.widgets;

import foundry.veil.api.client.render.rendertype.VeilRenderType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Vector2d;
import org.lwjgl.system.windows.POINT;
import wawa.mapwright.Helper;
import wawa.mapwright.MapwrightClient;
import wawa.mapwright.Rendering;

public class CompassWidget extends AbstractWidget {
    private static final int WIDTH = 57;
    private static final int HEIGHT = 56;

    private final TextureAtlasSprite COMPASS = Minecraft.getInstance().getGuiSprites().getSprite(MapwrightClient.id("compass/compass"));
    private final ResourceLocation POINTER = MapwrightClient.id("textures/gui/sprites/compass/pointer.png");

    private float xOffset = 0.0f;

	public CompassWidget(final int x, final int y) {
        super(x, y, WIDTH, HEIGHT, Component.literal("Compass"));
    }

    @Override
    protected void renderWidget(final GuiGraphics graphics, final int mouseX, final int mouseY, final float partialTick) {
        final int cx = (int) (this.getX() + this.width / 2.0f);
        final int cy = (int) (this.getY() + this.height / 2.0f);
        final double dist = Vector2d.distance(mouseX, mouseY, cx, cy);

		final float alphaTarget;
		if(dist < 55) {
            alpha = Mth.clamp(1.5f * (float) ((dist - 20) / 35) - 0.5f, 0.11f, 1);
        } else {
            alpha = 1;
        }

        final float rot = ((Minecraft.getInstance().player.yRotO + 90) % 360) / 360.0f;
        final int frame = Math.round(rot * 32);

        graphics.blit((int) (this.getX() + this.xOffset), this.getY(), 0, WIDTH, HEIGHT, this.COMPASS, 1.0f, 1.0f, 1.0f, alpha);
        Rendering.croppedBlit(graphics, POINTER, (int) (this.getX() + this.xOffset), (int) (this.getX() + this.xOffset + WIDTH), this.getY() + 3, this.getY() + 3 + HEIGHT, 0, 0, 1, (frame - 1) / 32f, frame / 32f, 1.0f, 1.0f, 1.0f, alpha);
    }

    @Override
    protected void updateWidgetNarration(final NarrationElementOutput narrationElementOutput) {

    }
}
