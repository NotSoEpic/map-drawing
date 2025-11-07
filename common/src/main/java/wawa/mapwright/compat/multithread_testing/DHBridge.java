package wawa.mapwright.compat.multithread_testing;

import net.minecraft.client.player.LocalPlayer;
import wawa.mapwright.MapwrightClient;

public class DHBridge {

	/**
	 * Adds a request for the DH clip thread to perform a clip.
	 * Make sure to hold onto the object passed in, as it will contain the finished Vector3D location.
	 *
	 * @param request The request to perform a clip on.
	 */
	public static void addRequest(final DhRequest request) {
		if (MapwrightClient.isDHPresent()) {
			MultithreadedDHTerrainAccess.INSTANCE.addRequest(request);
		}
	}

	public static synchronized void voidRequests() {
		if (MapwrightClient.isDHPresent()) {
			MultithreadedDHTerrainAccess.INSTANCE.voidRequests();
		}
	}

	public static DhRequest createRequest(final LocalPlayer player) {
		return new DhRequest(player.getEyePosition(), player.getLookAngle(), getViewDistance());
	}

	public static int getViewDistance() {
		if (MapwrightClient.isDHPresent()) {
			return MultithreadedDHTerrainAccess.getRenderDistance();
		}

		return 0;
	}
}
