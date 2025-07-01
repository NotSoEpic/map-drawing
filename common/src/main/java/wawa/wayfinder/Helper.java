package wawa.wayfinder;

import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.world.phys.Vec2;

public class Helper {
    public static Vec2 preciseMousePos() {
        final MouseHandler mouse = Minecraft.getInstance().mouseHandler;
        final Window window = Minecraft.getInstance().getWindow();
        return new Vec2(
                (float) (mouse.xpos() * window.getGuiScaledWidth() / window.getScreenWidth()),
                (float) (mouse.ypos() * window.getGuiScaledHeight() / window.getScreenHeight())
        );
    }
}
