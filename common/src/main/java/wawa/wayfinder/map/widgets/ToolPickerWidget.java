package wawa.wayfinder.map.widgets;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec2;
import wawa.wayfinder.Helper;
import wawa.wayfinder.WayfinderClient;
import wawa.wayfinder.map.tool.CopyTool;
import wawa.wayfinder.map.tool.DrawTool;
import wawa.wayfinder.map.tool.PanTool;
import wawa.wayfinder.map.tool.PinTool;

import java.util.ArrayList;
import java.util.List;

public class ToolPickerWidget extends AbstractWidget {
    private final List<SingleToolWidget> tools = new ArrayList<>();
    private final DrawTool pencil = new DrawTool(WayfinderClient.id("tool/pencil/pencil_cursor"), 0xFF000000, 0xFF000000);
    private final DrawTool eraser = new DrawTool(WayfinderClient.id("tool/eraser/eraser_cursor"), 0, 0);
    private final SingleToolWidget.BrushWidget brushWidget;
    private final PinTool pin = new PinTool();

    /**
     * Used for additional widgets being added after our tools have been.
     */
    public int finalToolY;

    public ToolPickerWidget(final int x, final int y) {
        super(x, y, 0, 0, Component.literal("tool picker"));
        //updated after every tool entry
        //make sure of it!!
        finalToolY = this.getY();

        this.tools.add(new SingleToolWidget(
                this.getX(), offsetFinalY(0),
                WayfinderClient.id("tool/paw"),
                WayfinderClient.id("tool/paw_highlight"),
                (w) -> PanTool.INSTANCE,
                Component.literal("paw")
        ));

        this.tools.add(new SingleToolWidget(
                this.getX(), offsetFinalY(20),
                WayfinderClient.id("tool/pencil/pencil"),
                WayfinderClient.id("tool/pencil/pencil_highlight"),
                (w) -> this.pencil,
                Component.literal("pencil")
        ));

        this.brushWidget = new SingleToolWidget.BrushWidget(this.getX(), offsetFinalY(20));
        this.tools.add(this.brushWidget);

        this.tools.add(new SingleToolWidget(
                this.getX(), offsetFinalY(20),
                WayfinderClient.id("tool/eraser/eraser"),
                WayfinderClient.id("tool/eraser/eraser_highlight"),
                (w) -> this.eraser,
                Component.literal("eraser")
        ));

        this.tools.add(new SingleToolWidget.PinWidget(
                this.getX(), offsetFinalY(20),
                (w) -> this.pin,
                Component.literal("pin")
        ));

        this.tools.add(new SingleToolWidget(
                this.getX(), offsetFinalY(20),
                WayfinderClient.id("tool/scissors"),
                WayfinderClient.id("tool/scissors_highlight"),
                (w) -> CopyTool.INSTANCE,
                Component.literal("copy")
        ));

        this.updateBounds();
    }

    private int offsetFinalY(int offset) {
        return finalToolY += offset;
    }

    public void pickHand() {
        WayfinderClient.TOOL_MANAGER.set(PanTool.INSTANCE);
    }

    public void pickPencil() {
        WayfinderClient.TOOL_MANAGER.set(this.pencil);
    }

    public void pickBrush() {
        if (WayfinderClient.TOOL_MANAGER.get() == this.brushWidget.last) {
            final Vec2 mouse = Helper.preciseMousePos();
            this.brushWidget.openToMouse(mouse.x, mouse.y);
        } else {
            WayfinderClient.TOOL_MANAGER.set(this.brushWidget.last);
        }
    }

    public void pickEraser() {
        WayfinderClient.TOOL_MANAGER.set(this.eraser);
    }

    public void pickColor(final int color) {
        if (color == 0xFF000000) {
            WayfinderClient.TOOL_MANAGER.set(this.pencil);
        } else {
            for (final DrawTool tool : this.brushWidget.getBrushes()) {
                if (tool.getInternalColor() == color) {
                    WayfinderClient.TOOL_MANAGER.set(tool);
                    return;
                }
            }
        }
    }

    public NativeImage getCopiedImage() {
        return CopyTool.INSTANCE.clipboard;
    }

    private void updateBounds() {
        int left = 100000;
        int top = 100000;
        int right = 0;
        int bottom = 0;
        for (final SingleToolWidget tool : this.tools) {
            left = Math.min(left, tool.getX());
            top = Math.min(top, tool.getY());
            right = Math.max(right, tool.getRight());
            bottom = Math.max(bottom, tool.getBottom());
        }
        this.setX(left);
        this.setY(top);
        this.setWidth(right - left);
        this.setHeight(bottom - top);
    }

    @Override
    public boolean isMouseOver(final double mouseX, final double mouseY) {
        return this.tools.stream().anyMatch(tool -> tool.isMouseOver(mouseX, mouseY));
    }

    @Override
    public boolean mouseClicked(final double mouseX, final double mouseY, final int button) {
        for (final SingleToolWidget tool : this.tools) {
            if (tool.mouseClicked(mouseX, mouseY, button)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void renderWidget(final GuiGraphics guiGraphics, final int mouseX, final int mouseY, final float partialTick) {
        for (final SingleToolWidget tool : this.tools) {
            tool.render(guiGraphics, mouseX, mouseY, partialTick);
        }
    }

    @Override
    protected void updateWidgetNarration(final NarrationElementOutput narrationElementOutput) {}
}
