package wawa.wayfinder.mapmanager;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2i;
import wawa.wayfinder.Wayfinder;

import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Iterator;
import java.util.UUID;

/**
 * Stores regions in memory for quick access
 */
public class MapRegions extends HashMap<Vector2i, AbstractMapWidgetRegion> {
    private Path regionPath;
    private int unloaded;
    private int loaded;
    private void deltaStats(AbstractMapWidgetRegion region, int delta) {
        if (region instanceof UnloadedMapWidgetRegion)
            unloaded += delta;
        else if (region instanceof LoadedMapWidgetRegion)
            loaded += delta;
    }

    public AbstractMapWidgetRegion put(int rx, int rz, AbstractMapWidgetRegion region) {
        deltaStats(region, 1);
        AbstractMapWidgetRegion prev = put(new Vector2i(rx, rz), region);
        deltaStats(prev, -1);
        return prev;
    }

    public AbstractMapWidgetRegion get(int rx, int rz) {
        return get(new Vector2i(rx, rz));
    }

    public boolean contains(int rx, int rz) {
        return containsKey(new Vector2i(rx, rz));
    }

    public int getLoaded() {
        return loaded;
    }

    public int getUnloaded() {
        return unloaded;
    }

    public void clearRegionPath() {
        regionPath = null;
    }

    /**
     * Sets the region path for this map for saving / loading
     *
     * @param folderID The UUID of the folder
     * @param name the secondary name of the folder, usually the name of the server / world
     * @param client whether map data should be saved as singleplayer information or server information
     * @author Cyvack
     */
    public void setRegionPathGeneral(UUID folderID, String name, boolean client) {
        File mainDir = Minecraft.getInstance().gameDirectory;

        regionPath = mainDir.toPath()
                .resolve("wayfinder_maps")
                .resolve(client ? "singleplayer" : "multiplayer")
                .resolve(folderID.toString() + "_" + name);
    }

    @Nullable
    public Path getRegionPath() {
        if (regionPath != null && Minecraft.getInstance().level != null) {
            return regionPath.resolve(Minecraft.getInstance().level.dimension().location().toDebugFileName());
        }

        return null;
    }

    public void save() {
        if (regionPath != null) {
            this.forEach((v, r) -> {
                r.save(getRegionPath());
            });
        }
    }

    int cleanupTimer = 0;
    public void tick() {
        if (regionPath != null) {
            cleanupTimer--;
            if (cleanupTimer < 0) {
                cleanupTimer = 20 * 10;
                cleanup(1000 * 10);
            }
        }
    }

    public void cleanup(long msThreshold) {
        if (regionPath == null) {
            Wayfinder.LOGGER.warn("No path for map to save");
            return;
        }
        long rendertime = Util.getMillis();
        for (Iterator<Entry<Vector2i, AbstractMapWidgetRegion>> it = entrySet().iterator(); it.hasNext();) {
            Entry<Vector2i, AbstractMapWidgetRegion> entry = it.next();
            if (rendertime - entry.getValue().getLastRenderTime() > msThreshold) {
                entry.getValue().save(getRegionPath());
                deltaStats(entry.getValue(), -1);
                it.remove();
            }
        }
    }
}
