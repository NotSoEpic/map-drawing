package wawa.mapwright.compat.multithread_testing;

import com.seibel.distanthorizons.api.DhApi;

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
	private final DHThread executor = new DHThread(requests);

	public void addRequest(final DhRequest request) {
		synchronized (this.requests) {
			this.requests.add(request);

			if (!this.executor.isAlive()) {
				this.executor.start();
			} else {
				if (this.executor.getState() == Thread.State.WAITING) {
					LockSupport.unpark(this.executor);
				}
			}
		}
	}

	public void voidRequests() {
		synchronized (this.requests) {
			this.requests.clear();
		}
	}

	public static int getRenderDistance() {
		return DhApi.Delayed.configs.graphics().chunkRenderDistance().getValue() * 16;
	}
}
