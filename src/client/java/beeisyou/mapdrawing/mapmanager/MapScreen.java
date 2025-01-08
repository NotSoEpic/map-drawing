package beeisyou.mapdrawing.mapmanager;

import beeisyou.mapdrawing.MapBindings;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

/**
 * The entire screen that gets rendered, including map and drawing tools
 */
public class MapScreen extends Screen {
    MapWidget map;
    public int color = ColorHelper.getArgb(255, 255, 255);
    public int size = 2;
    public boolean highlight = false;
    public MapScreen() {
        super(Text.translatable("map"));
    }

    @Override
    protected void init() {
        super.init();

        map = new MapWidget(this, 50, 30, width - 100, height - 60);

        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player.getMainHandStack().isOf(Items.SPYGLASS) || player.getOffHandStack().isOf(Items.SPYGLASS)) {
            Vec3d pos = player.getWorld().raycast(new RaycastContext(
                    player.getEyePos(), player.getPos().add(player.getRotationVector().multiply(128)),
                    RaycastContext.ShapeType.COLLIDER,
                    RaycastContext.FluidHandling.ANY,
                    player)).getPos();
            map.centerWorld(pos.x, pos.z);
        } else {
            map.centerWorld(player.getX(), player.getZ());
        }

        addDrawableChild(map);

        for (int i = 0; i < DyeColor.values().length; i++) {
            addDrawableChild(new DrawToolWidget(this, 2 + i * 10, 2, 8, 8, DyeColor.values()[i].getMapColor().color | 0xFF000000, 1));
            addDrawableChild(new DrawToolWidget(this, 2 + i * 10, 12, 8, 8, DyeColor.values()[i].getMapColor().color | 0xFF000000, 3));
        }
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
