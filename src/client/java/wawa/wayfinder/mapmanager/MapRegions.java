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
        if (prev != null) {
            try {
                prev.close();
            } catch (Exception e) {
                Wayfinder.LOGGER.warn("error closing resources for region {} {}\n{}", rx, rz, e);
            }
        }
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

    @Override
    public void clear() {
        forEach((v, r) -> {
            try {
                r.close();
            } catch (Exception e) {
                Wayfinder.LOGGER.warn("error closing resources for region {} {}\n{}", r.rx(), r.rz(), e);
            }
        });
        super.clear();
    }

    public void cleanup(long msThreshold) {
        if (regionPath == null) {
            Wayfinder.LOGGER.warn("No path for map to save");
            return;
        }
        long rendertime = Util.getMillis();
        for (Iterator<Entry<Vector2i, AbstractMapWidgetRegion>> it = entrySet().iterator(); it.hasNext();) {
            AbstractMapWidgetRegion entry = it.next().getValue();
            if (rendertime - entry.getLastRenderTime() > msThreshold) {
                entry.save(getRegionPath());
                if (!entry.hasHistory()) {
                    deltaStats(entry, -1);
                    try {
                        entry.close();
                    } catch (Exception e) {
                        Wayfinder.LOGGER.warn("error closing resources for region {} {}\n{}", entry.rx(), entry.rz(), e);
                    }
                    it.remove();
                }
            }
        }
    }

    public void reloadFromHistory() {
        forEach((k, r) -> r.reloadFromHistory());
    }

    public void clearHistory() {
        forEach((k, r) -> r.clearHistory());
    }
}
