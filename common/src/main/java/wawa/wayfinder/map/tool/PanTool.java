package wawa.wayfinder.map.tool;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec2;
import wawa.wayfinder.Helper;
import wawa.wayfinder.WayfinderClient;

public class PanTool extends Tool {
    public static PanTool INSTANCE = new PanTool();
    private static final ResourceLocation ICON = WayfinderClient.id("tool/paw");

    private PanTool() {}

    @Override
    public void renderScreen(final GuiGraphics graphics, final double mouseX, final double mouseY) {
        final Vec2 mouse = Helper.preciseMousePos();
        graphics.pose().pushPose();
        graphics.pose().translate(mouse.x % 1, mouse.y % 1, 0);
        graphics.blitSprite(ICON, (int)mouse.x - 8, (int)mouse.y - 8, 16, 16);
        graphics.pose().popPose();
    }
}
