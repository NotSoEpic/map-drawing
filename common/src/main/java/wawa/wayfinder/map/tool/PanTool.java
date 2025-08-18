package wawa.wayfinder.map.tool;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import wawa.wayfinder.WayfinderClient;

public class PanTool extends Tool {
    public static PanTool INSTANCE = new PanTool();
    private static final ResourceLocation ICON = WayfinderClient.id("tool/paw");

    private PanTool() {}

    @Override
    public void renderScreen(final GuiGraphics graphics, final double mouseX, final double mouseY) {
        graphics.blitSprite(ICON, (int)mouseX - 8, (int)mouseY - 8, 16, 16);
    }
}
