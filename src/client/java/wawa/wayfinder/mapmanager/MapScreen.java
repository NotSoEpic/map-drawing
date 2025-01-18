package wawa.wayfinder.mapmanager;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import wawa.wayfinder.MapBindings;
import wawa.wayfinder.Wayfinder;
import wawa.wayfinder.WayfinderClient;
import wawa.wayfinder.color.ColorPaletteManager;
import wawa.wayfinder.mapmanager.tools.Tool;
import wawa.wayfinder.mapmanager.widgets.MapWidget;
import wawa.wayfinder.mapmanager.widgets.StampListWidget;
import wawa.wayfinder.mapmanager.widgets.ToolSelectionWidget;

import java.util.List;

/**
 * The entire screen that gets rendered, including map and drawing tools
 */
public class MapScreen extends Screen {
    public MapWidget map;
    public ToolSelectionWidget toolSelection;

    public MapScreen() {
        super(Component.translatable("map"));
    }

    @Override
    protected void init() {
        super.init();

        map = new MapWidget(this, 50, 30, width - 100, height - 60);

        LocalPlayer player = Minecraft.getInstance().player;
        Vec3 spyglassPinPos = spyglassPinRaycast(player);
        if (spyglassPinPos != null) map.centerWorld(spyglassPinPos.x(), spyglassPinPos.z());
        else map.centerWorld(player.getX(), player.getZ());

        addRenderableWidget(map);

        WayfinderClient.palette = ColorPaletteManager.get(Wayfinder.id("default"));

        addRenderableWidget(new StampListWidget());

        toolSelection = new ToolSelectionWidget(this);
        addRenderableWidget(toolSelection);
    }

    // reverses order so the last added children (those rendering on top) are prioritized with mouse events
    @Override
    public List<? extends GuiEventListener> children() {
        return super.children().reversed();
    }

    @Override
    public <T extends GuiEventListener & Renderable & NarratableEntry> @NotNull T addRenderableWidget(T widget) {
        return super.addRenderableWidget(widget);
    }

    public Vec3 spyglassPinRaycast(LocalPlayer player) {
        int distance = Minecraft.getInstance().options.getEffectiveRenderDistance(); //client side view distance so we don't always cast into unloaded chunks

        if ((player.getMainHandItem().is(Items.SPYGLASS) || player.getOffhandItem().is(Items.SPYGLASS)) && player.isUsingItem()) {
            BlockHitResult hitResult = player.level().clip(new ClipContext(
                    player.getEyePosition(), player.position().add(player.getLookAngle().scale(distance * 16)), // this is a bit excessive but what if 64 render distance,,, // <- star was a nerd
                    ClipContext.Block.COLLIDER,
                    ClipContext.Fluid.ANY,
                    player));
            if (hitResult.getType() != HitResult.Type.MISS) {
                return hitResult.getLocation();
            }
        }
        return null;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (super.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        } else if (MapBindings.OPEN_MAP.matches(keyCode, scanCode)) {
            onClose();
            return true;
        } else if (Screen.hasControlDown() && MapBindings.UNDO.matches(keyCode, scanCode)) {
            WayfinderClient.regions.reloadFromHistory();
        } else if (MapBindings.SWAP_TOOL.matches(keyCode, scanCode)) {
            Tool.swap();
        } else if (MapBindings.PENCIL.matches(keyCode, scanCode)) {
            toolSelection.selectPencil();
        } else if (MapBindings.BRUSH.matches(keyCode, scanCode)) {
            toolSelection.selectBrush();
        } else if (MapBindings.ERASER.matches(keyCode, scanCode)) {
            toolSelection.selectEraser();
        } else if (MapBindings.RULER.matches(keyCode, scanCode)) {
            toolSelection.selectRuler();
        }
        return true;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
