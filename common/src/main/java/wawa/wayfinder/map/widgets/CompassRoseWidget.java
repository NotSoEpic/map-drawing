package wawa.wayfinder.map.widgets;

import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import org.joml.AxisAngle4d;
import org.joml.AxisAngle4f;
import org.joml.Quaternionf;
import org.joml.Vector2d;
import wawa.wayfinder.Helper;
import wawa.wayfinder.WayfinderClient;

public class CompassRoseWidget extends AbstractWidget {
    private final TextureAtlasSprite rose = Minecraft.getInstance().getGuiSprites().getSprite(WayfinderClient.id("compass/rose"));
    private final TextureAtlasSprite pointer = Minecraft.getInstance().getGuiSprites().getSprite(WayfinderClient.id("compass/pointer"));
    int roseSize = 23;

    public CompassRoseWidget(int x, int y) {
        super(x, y, 23, 23, Component.literal("Compass Rose"));
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        final float alpha = Helper.getMouseProximityFade(
                new Vector2d(mouseX, mouseY),
                new Vector2d(this.getX() - width / 2, this.getY() - height / 2)
        );

        guiGraphics.blit(this.getX() - roseSize, this.getY() - roseSize, 0, roseSize, roseSize, this.rose, 1, 1, 1, alpha);

        guiGraphics.pose().pushPose();

        Quaternionf quaternion = Axis.ZP.rotationDegrees(Minecraft.getInstance().gameRenderer.getMainCamera().getYRot() + 180);

        guiGraphics.pose().rotateAround(quaternion, this.getX() - (float) width / 2, this.getY() - (float) height / 2, 0);

        guiGraphics.blit(this.getX() - 15, this.getY() - 28, 0, 7, 4, this.pointer, 1, 1, 1, alpha);

        guiGraphics.pose().popPose();
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

    }
}
