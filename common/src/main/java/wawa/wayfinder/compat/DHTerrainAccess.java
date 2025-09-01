package wawa.wayfinder.compat;

import com.seibel.distanthorizons.api.DhApi;
import com.seibel.distanthorizons.api.interfaces.data.IDhApiTerrainDataCache;
import com.seibel.distanthorizons.api.interfaces.world.IDhApiLevelWrapper;
import com.seibel.distanthorizons.api.objects.DhApiResult;
import com.seibel.distanthorizons.api.objects.data.DhApiRaycastResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;
import wawa.wayfinder.WayfinderClient;

/**
 * Classloading unsafe Distant Horizons api access
 */
public class DHTerrainAccess {
    public static DHTerrainAccess INSTANCE = new DHTerrainAccess();

    private IDhApiTerrainDataCache cache = null;

    public void clearCache() {
        if (this.cache != null) {
            this.cache.clear();
            this.cache = null;
        }
    }

    @Nullable
    public Vector3d clip(final Vec3 origin, final Vec3 direction, final int length) {
        if (!DhApi.Delayed.worldProxy.worldLoaded()) {
            return null;
        }

        final IDhApiLevelWrapper levelWrapper = DhApi.Delayed.worldProxy.getSinglePlayerLevel();
        if (levelWrapper == null) {
            return null;
        }

        if (this.cache == null) {
            this.cache = DhApi.Delayed.terrainRepo.getSoftCache();
        }

        final DhApiResult<DhApiRaycastResult> result = DhApi.Delayed.terrainRepo.raycast(
                levelWrapper,
                origin.x, origin.y, origin.z,
                (float) direction.x, (float) direction.y, (float) direction.z,
                length,
                this.cache
        );

        if (result.success && result.payload != null) {
            return new Vector3d(result.payload.pos.x, result.payload.pos.y, result.payload.pos.z);
        }
        return null;
    }

    public int getViewDistance() {
        if (WayfinderClient.isDHPresent()) {
            return DhApi.Delayed.configs.graphics().chunkRenderDistance().getValue() * 16;
        }
        return 0;
    }
}
