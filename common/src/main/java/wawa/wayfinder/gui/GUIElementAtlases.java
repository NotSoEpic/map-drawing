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
            STAMP_BAG_SAVE = create(STAMP_BAG_SAVE_LOC, 0, 0, 128, 25, STAMP_BAG_SAVE_SCREEN_DIMENSIONS),
            STAMP_BAG_CLOSED = create(STAMP_BAG_SAVE_LOC, 96, 80, 16, 16, STAMP_BAG_SAVE_SCREEN_DIMENSIONS),
            STAMP_BAG_OPEN = create(STAMP_BAG_SAVE_LOC, 112, 80, 16, 16, STAMP_BAG_SAVE_SCREEN_DIMENSIONS),
            STAMP_BAG_SAVE_CONFIRM = create(STAMP_BAG_SAVE_LOC, 96, 48, 16, 16, STAMP_BAG_SAVE_SCREEN_DIMENSIONS),
            STAMP_BAG_SAVE_CANCEL = create(STAMP_BAG_SAVE_LOC, 112, 48, 16, 16, STAMP_BAG_SAVE_SCREEN_DIMENSIONS),

            STAMP_BAG_BROWSE_ENTRY = create(STAMP_BAG_SAVE_LOC, 0, 32, 96, 96, STAMP_BAG_SAVE_SCREEN_DIMENSIONS),
            STAMP_BAG_BROWSE_NEXT = create(STAMP_BAG_SAVE_LOC, 112, 32, 16, 16, STAMP_BAG_SAVE_SCREEN_DIMENSIONS),
            STAMP_BAG_BROWSE_PREVIOUS = create(STAMP_BAG_SAVE_LOC, 96, 32, 16, 16, STAMP_BAG_SAVE_SCREEN_DIMENSIONS),
            STAMP_BAG_BROWSE_FAVORITE = create(STAMP_BAG_SAVE_LOC, 96, 64, 16, 16, STAMP_BAG_SAVE_SCREEN_DIMENSIONS),
            STAMP_BAG_BROWSE_TRASH = create(STAMP_BAG_SAVE_LOC, 112, 64, 16, 16, STAMP_BAG_SAVE_SCREEN_DIMENSIONS);

    public static GUIElementAtlases create(ResourceLocation loc, int startX, int startY, int width, int height, int dimension) {
        return new GUIElementAtlases(loc, startX, startY, width, height, dimension);
    }

    public void render(GuiGraphics guiGraphics, int x, int y) {
        guiGraphics.blit(loc, x, y, startX, startY, width(), height(), stampBagSaveScreenDimensions, stampBagSaveScreenDimensions);
    }
}
