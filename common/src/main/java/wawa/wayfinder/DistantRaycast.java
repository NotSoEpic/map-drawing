package wawa.wayfinder;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;

/**
 * Classloading-safe wrapper for optional Distant Horizons support
 */
public class DistantRaycast {
    public static void clearCache() {
        if (WayfinderClient.isDHPresent()) {
//            DHTerrainAccess.INSTANCE.clearCache();
        }
    }

    public static Vector3d clip(final Player player, final Vec3 start, final Vec3 direction, final int vanilla, final int distantLength) {
        final BlockHitResult result = player.level().clip(new ClipContext(
                start,
                start.add(direction.scale(vanilla)),
                ClipContext.Block.COLLIDER, ClipContext.Fluid.ANY, player));
        if (result.getType() != HitResult.Type.MISS) {
            return new Vector3d(result.getLocation().x, result.getLocation().y, result.getLocation().z);
        }
        // todo maybe a config in case these raycasts are causing issues?
//        if (WayfinderClient.isDHPresent()) {
//            return DHTerrainAccess.INSTANCE.clip(start, direction, distantLength);
//        }
        return null;
    }
}
