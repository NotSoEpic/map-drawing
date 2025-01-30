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

    public SideTabWidget(int x, int y, String description, Supplier<ResourceLocation> sprite, Runnable onClick) {
        super(x, y, 16, 16, Component.literal(description));
        this.sprite = sprite;
        this.onClick = onClick;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.blitSprite(sprite.get(), getX() - 16, getY() - 8, 32, 32);
        if (isMouseOver(mouseX, mouseY)) {
            guiGraphics.renderTooltip(Minecraft.getInstance().font, getMessage(), mouseX, mouseY);
        }
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        onClick.run();
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

    }
}
