package wawa.wayfinder.map.tool;

import foundry.veil.api.client.render.rendertype.VeilRenderType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import wawa.wayfinder.Rendering;
import wawa.wayfinder.WayfinderClient;
import wawa.wayfinder.data.PageManager;
import wawa.wayfinder.map.stamp_bag.StampInformation;

import java.awt.*;
import java.util.*;
import java.util.List;

public class StampBagDebuggerTool extends Tool {

    private final List<InfoAndID> idList = new ArrayList<>();
    private int pointer;

    @Override
    public void onSelect() {
        ArrayList<StampInformation> collection = new ArrayList<>();
        WayfinderClient.STAMP_HANDLER.requestAllStamps(collection);
        for (int i = 0; i < collection.size(); i++) {
            StampInformation si = collection.get(i);
            idList.add(new InfoAndID(WayfinderClient.id("stamp_debugger_" + i), si));
        }
    }

    @Override
    public void onDeselect() {
        for (InfoAndID infoAndID : idList) {
            Minecraft.getInstance().getTextureManager().release(infoAndID.id);
            infoAndID.info.setRequestedImage(null);
        }

        idList.clear();
    }

    @Override
    public void controlScroll(PageManager activePage, double mouseX, double mouseY, double scrollY) {
        pointer += (int) scrollY;
        pointer = Mth.clamp(pointer, 0, idList.size() - 1);
    }

    @Override
    public void renderWorld(GuiGraphics graphics, int worldX, int worldY, double xOff, double yOff) {
        if (!idList.isEmpty()) {
            InfoAndID infoAndID = idList.get(pointer);
            if (infoAndID.info.getRequestedImage() == null) {
                return;
            }

            if (infoAndID.texture == null) {
                infoAndID.texture = new DynamicTexture(infoAndID.info.getRequestedImage());
                Minecraft.getInstance().getTextureManager().register(infoAndID.id, infoAndID.texture);
            }


            final RenderType renderType = VeilRenderType.get(Rendering.RenderTypes.PALETTE_SWAP, infoAndID.id);
            if (renderType == null) {
                return;
            }

            DynamicTexture renderable = infoAndID.texture;
            double x = worldX + xOff - (double) (renderable.getPixels().getWidth() / 2);
            double y = worldY + yOff - (double) (renderable.getPixels().getHeight() / 2);
            Rendering.renderTypeBlit(graphics, renderType, x, y, 0, 0f, 0f,
                    renderable.getPixels().getWidth(), renderable.getPixels().getHeight(), renderable.getPixels().getWidth(), renderable.getPixels().getHeight(), 1);

            graphics.drawString(Minecraft.getInstance().font, infoAndID.info.getCustomName(), (int) x, (int) y - 4, Color.GREEN.getRGB());
            graphics.drawString(Minecraft.getInstance().font, infoAndID.info.getFileName(), (int) x, (int) y - 16, Color.GREEN.getRGB());
            graphics.drawString(Minecraft.getInstance().font, "is favorited: " + infoAndID.info.isFavorited(), (int) x, (int) y - 26, Color.GREEN.getRGB());

        }
    }

    public static final class InfoAndID {
        private final ResourceLocation id;
        private final StampInformation info;
        public DynamicTexture texture = null;

        public InfoAndID(ResourceLocation id, StampInformation info) {
            this.id = id;
            this.info = info;
        }
    }
}
