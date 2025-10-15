package wawa.mapwright.map.stamp_bag.widgets;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.PoseStack;
import foundry.veil.api.client.render.rendertype.VeilRenderType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import wawa.mapwright.MapwrightClient;
import wawa.mapwright.Rendering;
import wawa.mapwright.gui.GUIElementAtlases;
import wawa.mapwright.map.StampBagScreen;
import wawa.mapwright.map.stamp_bag.StampInformation;
import wawa.mapwright.map.stamp_bag.StampTexture;
import wawa.mapwright.map.stamp_bag.widgets.abstract_classes.AbstractStampScreenWidget;
import wawa.mapwright.map.tool.StampBagDebuggerTool;
import wawa.mapwright.map.tool.StampTool;

import java.awt.*;
import java.util.function.Consumer;

public class StampEntryWidget extends AbstractStampScreenWidget {

    public @Nullable StampInformation stampInformation;

    private final Consumer<StampEntryWidget> onClick;

    private final ResourceLocation id;
    private boolean registered = false;

//    private float scrollFactor = 0;

    public StampEntryWidget(int x, int y, StampBagScreen parentScreen, Consumer<StampEntryWidget> onclick, ResourceLocation loc) {
        super(x, y, GUIElementAtlases.STAMP_BAG_BROWSE_ENTRY.width(), GUIElementAtlases.STAMP_BAG_BROWSE_ENTRY.height(), parentScreen);
        this.onClick = onclick;

        this.id = loc;
    }

    public void changeStampInformation(StampInformation newInfo) {
        registered = false;
        Minecraft.getInstance().getTextureManager().release(id);
        this.stampInformation = newInfo;
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        onClick.accept(this);
        MapwrightClient.TOOL_MANAGER.set(StampTool.INSTANCE);
        StampTool.INSTANCE.setActiveStamp(this.stampInformation);
        StampBagScreen.INSTANCE.changeStage(StampBagScreen.ScreenState.IDLE);
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mx, int my, float v) {
        if (stampInformation == null) {
            return;
        }

        StampTexture manager = stampInformation.getTextureManager();
        NativeImage tex = manager.getTexture();
        if (tex == null) {
            return;
        }

        if (!registered) {
            registered = true;
            Minecraft.getInstance().getTextureManager().register(id, manager);
        }

        PoseStack ps = guiGraphics.pose();
        ps.pushPose();
        final RenderType renderType = VeilRenderType.get(Rendering.RenderTypes.PALETTE_SWAP, id);
        if (renderType == null) {
            return;
        }

        ps.pushPose();
        //TODO:center
        ps.translate(getX() + 1, getY() + 1, 0);

        float scale = Math.min(56f / tex.getWidth() / 2f, 56f / tex.getHeight() / 2f);
        ps.scale(scale, scale, 1);

        Rendering.renderTypeBlit(guiGraphics, renderType, 4.1, 4, 0, 0f, 0f,
                manager.getTexture().getWidth(), manager.getTexture().getHeight(), manager.getTexture().getWidth(), manager.getTexture().getHeight(), 1);
        ps.popPose();

        ps.pushPose();
        guiGraphics.enableScissor(getX() + 36, getY() + 5, getX() + 123, getY() + 27);
        ps.translate(getX() + 38, getY() + 8, 0);
        if (Minecraft.getInstance().font.width(stampInformation.getCustomName()) < 85) {
            ps.translate(0, 4, 0);
        }

        // slight scale to handle wordwrap weirdness
        ps.scale(1.05f, 1.05f, 1);

        //TODO: scroll
        guiGraphics.drawWordWrap(Minecraft.getInstance().font, FormattedText.of(stampInformation.getCustomName()), 0, 0, 85, Color.WHITE.getRGB());
        guiGraphics.disableScissor();
        ps.popPose();

        if (isHovered()) {
            ps.translate(0, 0, 10);
            int tx = mx - tex.getWidth() - 16;
            int ty = my - tex.getHeight() / 2;
            guiGraphics.blitSprite(StampBagDebuggerTool.backgroundID, tx - 5, ty - 5, tex.getWidth() + 10, tex.getHeight() + 10);
            Rendering.renderTypeBlit(guiGraphics, renderType, tx, ty, 0, 0f, 0f,
                    manager.getTexture().getWidth(), manager.getTexture().getHeight(), manager.getTexture().getWidth(), manager.getTexture().getHeight(), 1);
        }

        ps.popPose();
    }
}
