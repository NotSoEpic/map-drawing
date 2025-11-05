package wawa.mapwright.map.stamp_bag.widgets;

import net.minecraft.client.gui.GuiGraphics;
import wawa.mapwright.gui.GUIElementAtlases;

public class DualGUIElement extends GUIElementButton{

	private final GUIElementAtlases secondary;
	public boolean imageSwitch = false;

	public DualGUIElement(final int x, final int y, final int scale, final GUIElementAtlases firstImage, final GUIElementAtlases secondImage, final OnPress onPress) {
		super(x, y, scale, firstImage, onPress);
		this.secondary = secondImage;
	}

	@Override
	protected void renderWidget(final GuiGraphics guiGraphics, final int mouseX, final int mouseY, final float partialTick) {
		if (this.imageSwitch) {
            this.secondary.render(guiGraphics, this.getX(), this.getY());
		} else {
			super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
		}
	}
}
