package wawa.wayfinder;

import net.minecraft.client.Minecraft;
import net.minecraft.world.level.Level;
import wawa.wayfinder.input.InputListener;
import wawa.wayfinder.map.tool.DrawTool;
import wawa.wayfinder.map.tool.Tool;

public class ClientEvents {
    public static void tick(Minecraft client) {
        InputListener.tick(client);
        WayfinderClient.PAGE_MANAGER.tick();
        WayfinderClient.POSITION_HISTORY.tick(client.player);
    }

    public static void join(Level level, Minecraft client) {
        WayfinderClient.PAGE_MANAGER.reloadPageIO(level, client);
        Tool.set(new DrawTool());
        WayfinderClient.POSITION_HISTORY.clear();
    }

    public static void leave() {
        WayfinderClient.PAGE_MANAGER.saveAndClear();
    }
}
