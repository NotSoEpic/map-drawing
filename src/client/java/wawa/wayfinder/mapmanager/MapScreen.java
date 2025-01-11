package wawa.wayfinder.mapmanager;

import wawa.wayfinder.MapBindings;
import wawa.wayfinder.Wayfinder;
import wawa.wayfinder.WayfinderClient;
import wawa.wayfinder.color.ColorPalette;
import wawa.wayfinder.color.ColorPaletteManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.Window;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import org.lwjgl.glfw.GLFW;

/**
 * The entire screen that gets rendered, including map and drawing tools
 */
public class MapScreen extends Screen {
    MapWidget map;

    public MapScreen() {
        super(Text.translatable("map"));
    }

    @Override
    protected void init() {
        super.init();

        map = new MapWidget(this, 50, 30, width - 100, height - 60);

        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        Vec3d spyglassPinPos = spyglassPinRaycast(player);
        if (spyglassPinPos != null) map.centerWorld(spyglassPinPos.getX(), spyglassPinPos.getZ());
        else map.centerWorld(player.getX(), player.getZ());

        Window window = MinecraftClient.getInstance().getWindow();
        GLFW.glfwSetInputMode(window.getHandle(), GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_HIDDEN);

        addDrawableChild(map);

        WayfinderClient.palette = ColorPaletteManager.get(Wayfinder.id("default"));

        for (int i = 0; i < ColorPalette.SIZE; i++) {
            addDrawableChild(new DrawToolWidget(this, 2 + i * 10, 2, 8, 8, i, 1));
            addDrawableChild(new DrawToolWidget(this, 2 + i * 10, 12, 8, 8, i, 3));
        }
    }

    public Vec3d spyglassPinRaycast(ClientPlayerEntity player) {
        int distance = MinecraftClient.getInstance().options.getClampedViewDistance(); //client side view distance so we don't always cast into unloaded chunks

        if ((player.getMainHandStack().isOf(Items.SPYGLASS) || player.getOffHandStack().isOf(Items.SPYGLASS)) && player.isUsingItem()) {
            BlockHitResult hitResult = player.getWorld().raycast(new RaycastContext(
                    player.getEyePos(), player.getPos().add(player.getRotationVector().multiply(distance * 16)), // this is a bit excessive but what if 64 render distance,,, // <- star was a nerd
                    RaycastContext.ShapeType.COLLIDER,
                    RaycastContext.FluidHandling.ANY,
                    player));
            if (hitResult.getType() != HitResult.Type.MISS) {
                return hitResult.getPos();
            }
        }
        return null;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (super.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        } else if (MapBindings.openMap.matchesKey(keyCode, scanCode)) {
            close();
            return true;
        }
        return true;
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
