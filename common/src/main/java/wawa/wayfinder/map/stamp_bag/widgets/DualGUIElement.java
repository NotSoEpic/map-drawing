package wawa.wayfinder.map.stamp_bag.widgets;

import net.minecraft.client.gui.GuiGraphics;
import wawa.wayfinder.gui.GUIElementAtlases;

public class DualGUIElement extends GUIElementButton{

	private final GUIElementAtlases secondary;
	public boolean imageSwitch = false;

	public DualGUIElement(int x, int y, int scale, GUIElementAtlases firstImage, GUIElementAtlases secondImage, OnPress onPress) {
		super(x, y, scale, firstImage, onPress);
		this.secondary = secondImage;
	}

	@Override
	protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
		if (imageSwitch) {
			secondary.render(guiGraphics, getX(), getY());
		} else {
			super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
		}
	}
}
