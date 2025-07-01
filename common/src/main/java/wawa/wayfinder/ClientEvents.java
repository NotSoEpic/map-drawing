package wawa.wayfinder;

import net.minecraft.client.Minecraft;
import net.minecraft.world.level.Level;
import wawa.wayfinder.input.InputListener;

public class ClientEvents {
    public static void tick(final Minecraft client) {
        InputListener.tick(client);
        WayfinderClient.PAGE_MANAGER.tick();
        WayfinderClient.POSITION_HISTORY.tick(client.player);
    }

    public static void join(final Level level, final Minecraft client) {
        WayfinderClient.PAGE_MANAGER.reloadPageIO(level, client);
        WayfinderClient.POSITION_HISTORY.clear();
    }

    public static void leave() {
        WayfinderClient.PAGE_MANAGER.saveAndClear();
    }
}
