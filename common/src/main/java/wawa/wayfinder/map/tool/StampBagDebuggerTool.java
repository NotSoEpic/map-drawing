package wawa.wayfinder.map.tool;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.PoseStack;
import foundry.veil.api.client.render.rendertype.VeilRenderType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import wawa.wayfinder.Rendering;
import wawa.wayfinder.WayfinderClient;
import wawa.wayfinder.data.PageManager;
import wawa.wayfinder.map.stamp_bag.StampInformation;
import wawa.wayfinder.map.stamp_bag.StampTexture;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class StampBagDebuggerTool extends Tool {

    private final List<InfoAndID> idList = new ArrayList<>();
    private int pointer;

    public static final ResourceLocation backgroundID = WayfinderClient.id("stamp_browser_background");

    @Override
    public void onSelect() {
        ArrayList<StampInformation> collection = new ArrayList<>();
        WayfinderClient.STAMP_HANDLER.requestAllStamps(collection, false);
        for (int i = 0; i < collection.size(); i++) {
            StampInformation si = collection.get(i);

            si.getTextureManager().addUser();
            idList.add(new InfoAndID(WayfinderClient.id("stamp_debugger_" + i), si, new AtomicBoolean()));
        }
    }

    @Override
    public void onDeselect() {
        for (InfoAndID id : idList) {
            Minecraft.getInstance().getTextureManager().release(id.id);
            id.info.getTextureManager().removeUser();
            id.textureRegistered.set(false);
        }

        idList.clear();
        pointer = 0;
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
            NativeImage texture = infoAndID.info.getTextureManager().getTexture();
            if (texture == null) {
                return;
            }

            if (!infoAndID.textureRegistered.get()) {
                infoAndID.textureRegistered.set(true);
                Minecraft.getInstance().getTextureManager().register(infoAndID.id, infoAndID.info().getTextureManager());
            }

            final RenderType renderType = VeilRenderType.get(Rendering.RenderTypes.PALETTE_SWAP, infoAndID.id);
            if (renderType == null) {
                return;
            }


            StampTexture renderable = infoAndID.info.getTextureManager();
            double x = worldX + xOff - (double) (renderable.getTexture().getWidth() / 2);
            double y = worldY + yOff - (double) (renderable.getTexture().getHeight() / 2);

            PoseStack ps = graphics.pose();
            ps.pushPose();
            ps.translate(x, y, 0);
            graphics.blitSprite(backgroundID, -2, -32, Math.max(renderable.getTexture().getWidth() + 4, 98), renderable.getTexture().getHeight() + 34);
            Rendering.renderTypeBlit(graphics, renderType, 0, 0, 0, 0f, 0f,
                    renderable.getTexture().getWidth(), renderable.getTexture().getHeight(), renderable.getTexture().getWidth(), renderable.getTexture().getHeight(), 1);

            graphics.drawString(Minecraft.getInstance().font, infoAndID.info.getCustomName(), 0, -4, Color.GREEN.getRGB());
            graphics.drawString(Minecraft.getInstance().font, infoAndID.info.getFileName(), 0, -16, Color.GREEN.getRGB());
            graphics.drawString(Minecraft.getInstance().font, "is favorited: " + infoAndID.info.isFavorited(), 0, -26, Color.GREEN.getRGB());
            ps.popPose();
        }
    }

    public record InfoAndID(ResourceLocation id, StampInformation info, AtomicBoolean textureRegistered) {
    }
}
