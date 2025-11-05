package wawa.mapwright.map.stamp_bag.widgets;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import wawa.mapwright.gui.GUIElementAtlases;

public class GUIElementButton extends Button {

    private final GUIElementAtlases texture;

    public GUIElementButton(final int x, final int y, final int scale, final GUIElementAtlases element, final OnPress onPress) {
        super(x, y, scale, scale, Component.empty(), onPress, ($) -> Component.empty());
        this.texture = element;
    }

    @Override
    protected void renderWidget(final GuiGraphics guiGraphics, final int mouseX, final int mouseY, final float partialTick) {
        this.texture.render(guiGraphics, this.getX(), this.getY());
    }
}
