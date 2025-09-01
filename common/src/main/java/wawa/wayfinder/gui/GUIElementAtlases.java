package wawa.wayfinder.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import wawa.wayfinder.WayfinderClient;

public record GUIElementAtlases(
        ResourceLocation loc,
        int startX,
        int startY,
        int width,
        int height,
        int stampBagSaveScreenDimensions) {

    private static final ResourceLocation STAMP_BAG_SAVE_LOC = WayfinderClient.id("textures/gui/sprites/stamp_bag_screen.png");
    private static final int STAMP_BAG_SAVE_SCREEN_DIMENSIONS = 128;

    public static final GUIElementAtlases
            STAMP_BAG_BACKGROUND = create(STAMP_BAG_SAVE_LOC, 0, 0, 128, 25, STAMP_BAG_SAVE_SCREEN_DIMENSIONS),
            STAMP_BAG_CLOSED = create(STAMP_BAG_SAVE_LOC, 0, 32, 16, 16, STAMP_BAG_SAVE_SCREEN_DIMENSIONS),
            STAMP_BAG_OPEN = create(STAMP_BAG_SAVE_LOC, 16, 32, 16, 16, STAMP_BAG_SAVE_SCREEN_DIMENSIONS),
            STAMP_BAG_SAVE_CONFIRM = create(STAMP_BAG_SAVE_LOC, 32, 32, 16, 16, STAMP_BAG_SAVE_SCREEN_DIMENSIONS),
            STAMP_BAG_SAVE_CANCEL = create(STAMP_BAG_SAVE_LOC, 48, 32, 16, 16, STAMP_BAG_SAVE_SCREEN_DIMENSIONS);

    public static GUIElementAtlases create(ResourceLocation loc, int startX, int startY, int width, int height, int dimension) {
        return new GUIElementAtlases(loc, startX, startY, width, height, dimension);
    }

    public void render(GuiGraphics guiGraphics, int x, int y) {
        guiGraphics.blit(loc, x, y, startX, startY, width(), height(), stampBagSaveScreenDimensions, stampBagSaveScreenDimensions);
    }
}
