package wawa.mapwright.map.stamp_bag.widgets;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import wawa.mapwright.gui.GUIElementAtlases;

public class GUIElementButton extends Button {

    private final GUIElementAtlases texture;

    public GUIElementButton(int x, int y, int scale, GUIElementAtlases element, OnPress onPress) {
        super(x, y, scale, scale, Component.empty(), onPress, ($) -> Component.empty());
        this.texture = element;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        texture.render(guiGraphics, getX(), getY());
    }
}
