package beeisyou.mapdrawing;

import beeisyou.mapdrawing.mapmanager.ColorElement;
import beeisyou.mapdrawing.mapmanager.MapWidget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

public class MapScreen extends Screen {
    MapWidget map;
    public int color = ColorHelper.getArgb(255, 255, 255);
    protected MapScreen() {
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
        addDrawableChild(new ColorElement(2, 2, 10, 10, ColorHelper.getArgb(255, 255, 255), this));
        addDrawableChild(new ColorElement(14, 2, 10, 10, ColorHelper.getArgb(0, 0, 0, 0), this));
        addDrawableChild(new ColorElement(26, 2, 10, 10, ColorHelper.getArgb(255, 0, 0), this));
        addDrawableChild(new ColorElement(38, 2, 10, 10, ColorHelper.getArgb(0, 255, 0), this));
        addDrawableChild(new ColorElement(50, 2, 10, 10, ColorHelper.getArgb(0, 0, 255), this));
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
