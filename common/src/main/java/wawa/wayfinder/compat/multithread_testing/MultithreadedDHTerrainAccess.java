package wawa.wayfinder.compat.multithread_testing;

import com.seibel.distanthorizons.api.DhApi;
import net.minecraft.client.player.LocalPlayer;
import wawa.wayfinder.WayfinderClient;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.locks.LockSupport;

public class MultithreadedDHTerrainAccess {

	public static final MultithreadedDHTerrainAccess INSTANCE = new MultithreadedDHTerrainAccess();

	/**
	 * All requests to DH for clipping. Passed into the DH thread.
	 */
	private final Queue<DhRequest> requests = new ArrayDeque<>();

	/**
	 * The clip executor thread.
	 */
	private final DHThread executor;

	public MultithreadedDHTerrainAccess() {
		executor = new DHThread(requests);
	}

	/**
	 * Adds a request for the DH clip thread to perform a clip. <B/>
	 * Make sure to hold onto the object passed in, as it will contain the finished Vector3D location.
	 *
	 * @param request The request to perform a clip on.
	 */
	public void addRequest(DhRequest request) {
		synchronized (requests) {
			requests.add(request);

			if (!executor.isAlive()) {
				executor.start();
			} else {
				if (executor.getState() == Thread.State.WAITING) {
					LockSupport.unpark(executor);
				}
			}
		}
	}

	public synchronized void clearThreadCache() {
//		executor.clearCache();
	}

	public synchronized void voidRequests() {
		synchronized (requests) {
			requests.clear();
		}
	}

	public static DhRequest createRequest(LocalPlayer player) {
		return new DhRequest(player.getEyePosition(), player.getLookAngle(), MultithreadedDHTerrainAccess.INSTANCE.getViewDistance());
	}

	public int getViewDistance() {
		if (WayfinderClient.isDHPresent()) {
			return DhApi.Delayed.configs.graphics().chunkRenderDistance().getValue() * 16;
		}
		return 0;
	}
}
