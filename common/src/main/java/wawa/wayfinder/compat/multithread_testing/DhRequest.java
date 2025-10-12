package wawa.wayfinder.compat.multithread_testing;

import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;

import java.util.concurrent.atomic.AtomicBoolean;

public final class DhRequest {
	private final Vec3 origin;
	private final Vec3 direction;
	private final int length;

	private final Vector3d finishedLoc;
	private final AtomicBoolean finished;

	public volatile int ticksSinceRequested = 0;

	public DhRequest(Vec3 origin, Vec3 direction, int length) {
		this.origin = origin;
		this.direction = direction;
		this.length = length;

		finishedLoc = new Vector3d();
		finished = new AtomicBoolean();
	}

	public Vec3 origin() {
		return origin;
	}

	public Vec3 direction() {
		return direction;
	}

	public int length() {
		return length;
	}

	public boolean isFinished() {
		synchronized (finished) {
			return finished.get();
		}
	}

	public void setFinished() {
		synchronized (finished) {
			finished.set(true);
		}
	}

	public void setFinishedLoc(Vector3d vec) {
		synchronized (finishedLoc) {
			finishedLoc.set(vec);
		}
	}

	public synchronized Vector3d finishedLoc() {
		synchronized (finishedLoc) {
			return finishedLoc;
		}
	}
}
