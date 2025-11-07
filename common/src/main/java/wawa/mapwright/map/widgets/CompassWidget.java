package wawa.mapwright.map.widgets;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.joml.Vector2d;
import wawa.mapwright.Helper;
import wawa.mapwright.MapwrightClient;
import wawa.mapwright.Rendering;

public class CompassWidget extends AbstractWidget {
    private static final int WIDTH = 57;
    private static final int HEIGHT = 56;

    private final static TextureAtlasSprite COMPASS = Minecraft.getInstance().getGuiSprites().getSprite(MapwrightClient.id("compass/compass"));
    private final static ResourceLocation POINTER = MapwrightClient.id("textures/gui/sprites/compass/pointer.png");

	public CompassWidget(final int x, final int y) {
        super(x, y, WIDTH, HEIGHT, Component.literal("Compass"));
    }

    @Override
    protected void renderWidget(final GuiGraphics graphics, final int mouseX, final int mouseY, final float partialTick) {
        final int cx = (int) (this.getX() + this.width / 2.0f);
        final int cy = (int) (this.getY() + this.height / 2.0f);

        final float rot = ((Minecraft.getInstance().player.yRotO + 101.25f) % 360) / 360.0f;
        final int frame = Math.round(rot * 32);
        final float alpha = Helper.getMouseProximityFade(new Vector2d(mouseX, mouseY), new Vector2d(cx, cy), 55);

        graphics.blit(this.getX(), this.getY(), 0, WIDTH, HEIGHT, COMPASS, 1.0f, 1.0f, 1.0f, alpha);
        Rendering.croppedBlit(graphics, POINTER, this.getX(), this.getX() + WIDTH, this.getY() + 3, this.getY() + 3 + HEIGHT, 0, 0, 1, (frame - 1) / 32f, frame / 32f, 1.0f, 1.0f, 1.0f, alpha);
    }

    @Override
    protected void updateWidgetNarration(final NarrationElementOutput narrationElementOutput) {

    }
}
