package wawa.wayfinder.map.widgets;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec2;
import wawa.wayfinder.Helper;
import wawa.wayfinder.WayfinderClient;
import wawa.wayfinder.map.tool.DrawTool;
import wawa.wayfinder.map.tool.Tool;

import java.util.ArrayList;
import java.util.List;

public class ToolPickerWidget extends AbstractWidget {
    private final List<SingleToolWidget> tools = new ArrayList<>();
    private final DrawTool pencil = new DrawTool(0xFF000000);
    private final SingleToolWidget.Brush brush;
    private final DrawTool eraser = new DrawTool(0);
    public ToolPickerWidget(int x, int y) {
        super(x, y, 0, 0, Component.literal("tool picker"));
        pencil.icon = WayfinderClient.id("cursor/pencil");
        tools.add(new SingleToolWidget(
                getX(), getY(),
                WayfinderClient.id("tool/pencil"),
                WayfinderClient.id("tool/pencil_highlight"),
                (w) -> pencil,
                Component.literal("pencil")
        ));
        brush = new SingleToolWidget.Brush(getX(), getY() + 20);
        tools.add(brush);
        eraser.icon = WayfinderClient.id("cursor/eraser");
        tools.add(new SingleToolWidget(
                getX(), getY() + 40,
                WayfinderClient.id("tool/eraser"),
                WayfinderClient.id("tool/eraser_highlight"),
                (w) -> eraser,
                Component.literal("eraser")
        ));
        updateBounds();
    }

    public void pickPencil() {
        Tool.set(pencil);
    }

    public void pickBrush() {
        if (Tool.get() == brush.last) {
            Vec2 mouse = Helper.preciseMousePos();
            brush.openToMouse((int)mouse.x, (int)mouse.y);
        } else {
            Tool.set(brush.last);
        }
    }

    public void pickColor(int color) {
        if (color == 0xFF000000) {
            Tool.set(pencil);
        } else {
            for (DrawTool tool : brush.getBrushes()) {
                if (tool.getInternalColor() == color) {
                    Tool.set(tool);
                    return;
                }
            }
        }
    }

    private void updateBounds() {
        int left = 100000;
        int top = 100000;
        int right = 0;
        int bottom = 0;
        for (SingleToolWidget tool : tools) {
            left = Math.min(left, tool.getX());
            top = Math.min(top, tool.getY());
            right = Math.max(right, tool.getRight());
            bottom = Math.max(bottom, tool.getBottom());
        }
        setX(left);
        setY(top);
        setWidth(right - left);
        setHeight(bottom - top);
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return tools.stream().anyMatch(tool -> tool.isMouseOver(mouseX, mouseY));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (SingleToolWidget tool : tools) {
            if (tool.mouseClicked(mouseX, mouseY, button)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        for (SingleToolWidget tool :tools) {
            tool.render(guiGraphics, mouseX, mouseY, partialTick);
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}
}
