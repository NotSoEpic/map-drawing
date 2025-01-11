package wawa.wayfinder.mapmanager;

import wawa.wayfinder.MapBindings;
import wawa.wayfinder.Wayfinder;
import wawa.wayfinder.WayfinderClient;
import wawa.wayfinder.color.ColorPalette;
import wawa.wayfinder.color.ColorPaletteManager;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.glfw.GLFW;

/**
 * The entire screen that gets rendered, including map and drawing tools
 */
public class MapScreen extends Screen {
    MapWidget map;

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

        Window window = Minecraft.getInstance().getWindow();
        GLFW.glfwSetInputMode(window.getWindow(), GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_HIDDEN);

        addRenderableWidget(map);

        WayfinderClient.palette = ColorPaletteManager.get(Wayfinder.id("default"));

        for (int i = 0; i < ColorPalette.SIZE; i++) {
            addRenderableWidget(new DrawToolWidget(this, 2 + i * 10, 2, 8, 8, i, 1));
            addRenderableWidget(new DrawToolWidget(this, 2 + i * 10, 12, 8, 8, i, 3));
        }
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
        } else if (MapBindings.openMap.matches(keyCode, scanCode)) {
            onClose();
            return true;
        }
        return true;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
