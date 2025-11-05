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

    public StampEntryWidget(final int x, final int y, final StampBagScreen parentScreen, final Consumer<StampEntryWidget> onclick, final ResourceLocation loc) {
        super(x, y, GUIElementAtlases.STAMP_BAG_BROWSE_ENTRY.width(), GUIElementAtlases.STAMP_BAG_BROWSE_ENTRY.height(), parentScreen);
        this.onClick = onclick;

        this.id = loc;
    }

    public void changeStampInformation(final StampInformation newInfo) {
        this.registered = false;
        Minecraft.getInstance().getTextureManager().release(this.id);
        this.stampInformation = newInfo;
    }

    @Override
    public void onClick(final double mouseX, final double mouseY) {
        this.onClick.accept(this);
        MapwrightClient.TOOL_MANAGER.set(StampTool.INSTANCE);
        StampTool.INSTANCE.setActiveStamp(this.stampInformation);
        StampBagScreen.INSTANCE.changeStage(StampBagScreen.ScreenState.IDLE);
    }

    @Override
    protected void renderWidget(final GuiGraphics guiGraphics, final int mx, final int my, final float v) {
        if (this.stampInformation == null) {
            return;
        }

        final StampTexture manager = this.stampInformation.getTextureManager();
        final NativeImage tex = manager.getTexture();
        if (tex == null) {
            return;
        }

        if (!this.registered) {
            this.registered = true;
            Minecraft.getInstance().getTextureManager().register(this.id, manager);
        }

        final PoseStack ps = guiGraphics.pose();
        ps.pushPose();
        final RenderType renderType = VeilRenderType.get(Rendering.RenderTypes.PALETTE_SWAP, this.id);
        if (renderType == null) {
            return;
        }

        ps.pushPose();
        //TODO:center
        ps.translate(this.getX() + 1, this.getY() + 1, 0);

        final float scale = Math.min(56f / tex.getWidth() / 2f, 56f / tex.getHeight() / 2f);
        ps.scale(scale, scale, 1);

        Rendering.renderTypeBlit(guiGraphics, renderType, 4.1, 4, 0, 0f, 0f,
                manager.getTexture().getWidth(), manager.getTexture().getHeight(), manager.getTexture().getWidth(), manager.getTexture().getHeight(), 1);
        ps.popPose();

        ps.pushPose();
        guiGraphics.enableScissor(this.getX() + 36, this.getY() + 5, this.getX() + 123, this.getY() + 27);
        ps.translate(this.getX() + 38, this.getY() + 8, 0);
        if (Minecraft.getInstance().font.width(this.stampInformation.getCustomName()) < 85) {
            ps.translate(0, 4, 0);
        }

        // slight scale to handle wordwrap weirdness
        ps.scale(1.05f, 1.05f, 1);

        //TODO: scroll
        guiGraphics.drawWordWrap(Minecraft.getInstance().font, FormattedText.of(this.stampInformation.getCustomName()), 0, 0, 85, Color.WHITE.getRGB());
        guiGraphics.disableScissor();
        ps.popPose();

        if (this.isHovered()) {
            ps.translate(0, 0, 10);
            final int tx = mx - tex.getWidth() - 16;
            final int ty = my - tex.getHeight() / 2;
            guiGraphics.blitSprite(StampBagDebuggerTool.backgroundID, tx - 5, ty - 5, tex.getWidth() + 10, tex.getHeight() + 10);
            Rendering.renderTypeBlit(guiGraphics, renderType, tx, ty, 0, 0f, 0f,
                    manager.getTexture().getWidth(), manager.getTexture().getHeight(), manager.getTexture().getWidth(), manager.getTexture().getHeight(), 1);
        }

        ps.popPose();
    }
}
