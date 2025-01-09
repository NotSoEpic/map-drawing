package beeisyou.mapdrawing.mapmanager;

import net.minecraft.util.Util;
import org.joml.Vector2i;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Stores regions in memory for quick access
 */
public class MapRegions extends HashMap<Vector2i, AbstractMapWidgetRegion> {
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

    public void save() {
        this.forEach((v, r) -> {
            if (r instanceof LoadedMapWidgetRegion loaded) {
                loaded.save();
            }
        });
    }

    int cleanupTimer = 0;
    public void tick() {
        cleanupTimer--;
        if (cleanupTimer < 0) {
            cleanupTimer = 20 * 10;
            cleanup(1000 * 10);
        }
    }
    public void cleanup(long msThreshold) {
        long rendertime = Util.getMeasuringTimeMs();
        for (Iterator<Entry<Vector2i, AbstractMapWidgetRegion>> it = entrySet().iterator(); it.hasNext();) {
            Map.Entry<Vector2i, AbstractMapWidgetRegion> entry = it.next();
            if (rendertime - entry.getValue().getLastRenderTime() > msThreshold) {
                entry.getValue().save();
                entry.getValue().clear();
                deltaStats(entry.getValue(), -1);
                it.remove();
            }
        }
    }
}
