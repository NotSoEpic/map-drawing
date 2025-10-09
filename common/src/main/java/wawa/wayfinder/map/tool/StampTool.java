package wawa.wayfinder.map.tool;

import com.mojang.blaze3d.platform.NativeImage;
import foundry.veil.api.client.render.rendertype.VeilRenderType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.RoundingMode;
import org.joml.Vector2d;
import org.joml.Vector2i;
import org.joml.Vector2ic;
import wawa.wayfinder.Rendering;
import wawa.wayfinder.WayfinderClient;
import wawa.wayfinder.data.PageManager;
import wawa.wayfinder.map.stamp_bag.StampInformation;
import wawa.wayfinder.map.widgets.MapWidget;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class StampTool extends Tool {
    public static StampTool INSTANCE = new StampTool();
    private static final ResourceLocation TEXTURE = WayfinderClient.id("tool/stamp/stamp");
    private final List<InfoAndID> idList = new ArrayList<>();
    private int pointer;

    public StampTool() {
    }

    @Override
    public void mouseDown(PageManager activePage, MapWidget.MouseType mouseType, Vector2d world) {
        if (mouseType == MapWidget.MouseType.LEFT) {
            if (!idList.isEmpty()) {
                InfoAndID infoAndID = idList.get(pointer);
                NativeImage texture = infoAndID.info.getTextureManager().getTexture();
                if (texture == null) {
                    return;
                }

                world = world.add(0, 8);
                final Vector2ic end = new Vector2i(world, RoundingMode.FLOOR);

                activePage.putRegion(end.x() - texture.getWidth() / 2, end.y() - texture.getHeight() / 2, texture.getWidth(), texture.getHeight(),
                        (dx, dy, old) -> {
                            int pixelColor = texture.getPixelRGBA(dx, dy);
                            if (pixelColor == 0) {
                                return activePage.getPixelARGB((end.x() - texture.getWidth() / 2) + dx, (end.y() - texture.getHeight() / 2) + dy);
                            } else {
                                return texture.getPixelRGBA(dx, dy);
                            }
                        }
                );
            }
        }
    }

    public void setActiveStamp(StampInformation stamp) {
        for (InfoAndID id : idList) {
            if (id.info == stamp) {
                pointer = id.order;
                return;
            }
        }
    }

    @Override
    public void onSelect() {
        ArrayList<StampInformation> collection = new ArrayList<>();
        WayfinderClient.STAMP_HANDLER.requestAllStamps(collection, false);
        for (int i = 0; i < collection.size(); i++) {
            StampInformation si = collection.get(i);

            si.getTextureManager().addUser();
            idList.add(new InfoAndID(WayfinderClient.id("stamp_tool_" + i), si, new AtomicBoolean(), i));
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
    public void renderScreen(GuiGraphics graphics, double mouseX, double mouseY) {
        graphics.blitSprite(TEXTURE, (int) mouseX - 8, (int) mouseY - 8, 16, 16);
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

            worldY = worldY + 8;

            Rendering.renderTypeBlit(graphics, renderType, worldX + xOff - (double) (texture.getWidth() / 2), worldY + yOff - (double) (texture.getHeight() / 2), 0, 0f, 0f,
                    texture.getWidth(), texture.getHeight(), texture.getWidth(), texture.getHeight(), 1);

            graphics.renderOutline((int) (worldX + xOff - (double) (texture.getWidth() / 2)), (int) (worldY + yOff - (double) (texture.getHeight() / 2)),
                    texture.getWidth(), texture.getHeight(),
                    0xff000000);
        }
    }

    public record InfoAndID(ResourceLocation id, StampInformation info, AtomicBoolean textureRegistered, int order) {
    }
}