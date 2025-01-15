package wawa.wayfinder.mapmanager;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import wawa.wayfinder.ClientStampTooltipComponent;
import wawa.wayfinder.MapBindings;
import wawa.wayfinder.Wayfinder;
import wawa.wayfinder.WayfinderClient;
import wawa.wayfinder.color.ColorPaletteManager;
import wawa.wayfinder.mapmanager.tools.StampTool;
import wawa.wayfinder.mapmanager.tools.Tool;
import wawa.wayfinder.mapmanager.widgets.MapWidget;
import wawa.wayfinder.mapmanager.widgets.StampToolWidget;
import wawa.wayfinder.mapmanager.widgets.ToolSelectionWidget;

import java.util.Iterator;

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

//        for (int i = 0; i < ColorPalette.SIZE; i++) {
//            addRenderableWidget(new DrawToolWidget(this, 2 + i * 10, 2, 8, 8, i, 1));
//            addRenderableWidget(new DrawToolWidget(this, 2 + i * 10, 12, 8, 8, i, 3));
//        }
        boolean stampAllowed = false;
        Iterator<ResourceLocation> stamps = StampTool.collectAvailableStamps(Minecraft.getInstance().player).iterator();
        for (int i = 0; stamps.hasNext(); i++) {
            ResourceLocation stamp = stamps.next().withPath(ClientStampTooltipComponent::fromPathShorthand);
            if (Tool.get() instanceof StampTool stampTool && stampTool.stamp.equals(stamp))
                stampAllowed = true;
            addRenderableWidget(new StampToolWidget(2, 2 + i * 18, stamp));
        }
        if (Tool.get() instanceof StampTool && !stampAllowed) {
            Tool.set(null);
        }

        toolSelection = new ToolSelectionWidget(this);
        addRenderableWidget(toolSelection);
    }

    public void setDrawingEnabled(boolean enabled) {
        map.active = enabled;
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
