package wawa.wayfinder.map.widgets;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Supplier;

public class SideTabWidget extends AbstractWidget {
    private final Supplier<ResourceLocation> sprite;
    private final Runnable onClick;

    public SideTabWidget(final int x, final int y, final String description, final Supplier<ResourceLocation> sprite, final Runnable onClick) {
        super(x, y, 16, 16, Component.literal(description));
        this.sprite = sprite;
        this.onClick = onClick;
    }

    @Override
    protected void renderWidget(final GuiGraphics guiGraphics, final int mouseX, final int mouseY, final float partialTick) {
        guiGraphics.blitSprite(this.sprite.get(), this.getX() - 16, this.getY() - 8, 32, 32);
        if (this.isMouseOver(mouseX, mouseY)) {
            guiGraphics.renderTooltip(Minecraft.getInstance().font, this.getMessage(), mouseX, mouseY);
        }
    }

    @Override
    public void onClick(final double mouseX, final double mouseY) {
        this.onClick.run();
    }

    @Override
    protected void updateWidgetNarration(final NarrationElementOutput narrationElementOutput) {

    }
}
