package wawa.mapwright.map.widgets;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Vector2d;
import wawa.mapwright.MapwrightClient;

public class CompassWidget extends AbstractWidget {
    private static final int WIDTH = 57;
    private static final int HEIGHT = 52;

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

		final float xOffsetTarget;
		if(dist < WIDTH * 0.8f) {
            xOffsetTarget = WIDTH + WIDTH / 3f;
        } else {
            xOffsetTarget = 0.0f;
        }

        final float rot = ((Minecraft.getInstance().player.yRotO + 90) % 360) / 360.0f;
        final int frame = Math.round(rot * 32);

        graphics.blit((int) (this.getX() + this.xOffset), this.getY(), 0, WIDTH, HEIGHT, this.COMPASS, 1.0f, 1.0f, 1.0f, 1.0f);
        graphics.blit(this.POINTER, (int) (this.getX() + this.xOffset), this.getY(), 0.0f, HEIGHT * frame, WIDTH, HEIGHT, WIDTH, HEIGHT * 32);

        this.xOffset = Mth.clampedLerp(this.xOffset, xOffsetTarget, partialTick * 0.2f);
    }

    @Override
    protected void updateWidgetNarration(final NarrationElementOutput narrationElementOutput) {

    }
}
