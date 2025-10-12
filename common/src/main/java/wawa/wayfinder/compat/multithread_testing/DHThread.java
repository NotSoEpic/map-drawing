package wawa.wayfinder.compat.multithread_testing;

import com.seibel.distanthorizons.api.DhApi;
import com.seibel.distanthorizons.api.interfaces.data.IDhApiTerrainDataCache;
import com.seibel.distanthorizons.api.interfaces.world.IDhApiLevelWrapper;
import com.seibel.distanthorizons.api.objects.DhApiResult;
import com.seibel.distanthorizons.api.objects.data.DhApiRaycastResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;
import wawa.wayfinder.WayfinderClient;

import java.util.Queue;
import java.util.concurrent.locks.LockSupport;

public class DHThread extends Thread {

	private final Queue<DhRequest> requests;
	private final IDhApiTerrainDataCache cache;
	//cache clearing casues crashes, just going to remove it entirely

	public DHThread(Queue<DhRequest> requests) {
		this.requests = requests;
		this.cache = DhApi.Delayed.terrainRepo.getSoftCache();
	}

	@Override
	public void run() {
		while (true) {
			DhRequest request = null;
			synchronized (requests) {
				if (!requests.isEmpty()) {
					//gather request
					request = requests.poll();
				}
			}

			if (request == null) {
				//park if there are no requests
				LockSupport.park(this);
			} else {
				//execute clip and set finished location inside the request
				Vector3d clip = clip(request.origin(), request.direction(), request.length());
				if (clip != null) {
					request.setFinishedLoc(clip);
				}

				request.setFinished();
			}
		}
	}

	@Nullable
	private Vector3d clip(final Vec3 origin, final Vec3 direction, final int length) {
		if (!DhApi.Delayed.worldProxy.worldLoaded()) {
			return null;
		}

		final IDhApiLevelWrapper levelWrapper = DhApi.Delayed.worldProxy.getSinglePlayerLevel();
		if (levelWrapper == null) {
			return null;
		}

		final DhApiResult<DhApiRaycastResult> result = DhApi.Delayed.terrainRepo.raycast(
				levelWrapper,
				origin.x, origin.y, origin.z,
				(float) direction.x, (float) direction.y, (float) direction.z,
				length,
				cache
		);

		if (result.success && result.payload != null) {
			return new Vector3d(result.payload.pos.x, result.payload.pos.y, result.payload.pos.z);
		}
		return null;
	}

	private synchronized void clearCache() {
//		cache = null;
//		cache = DhApi.Delayed.terrainRepo.getSoftCache();
	}
}
