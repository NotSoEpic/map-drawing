package wawa.mapwright.compat.multithread_testing;

import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public final class DhRequest {
	private final Vec3 origin;
	private final Vec3 direction;
	private final int length;

	private final Vector3d finishedLoc;
	private final AtomicBoolean finished;

	public volatile AtomicInteger ticksSinceRequested;

	public DhRequest(final Vec3 origin, final Vec3 direction, final int length) {
		this.origin = origin;
		this.direction = direction;
		this.length = length;

        this.finishedLoc = new Vector3d();
        this.finished = new AtomicBoolean();
        this.ticksSinceRequested = new AtomicInteger();
	}

	public Vec3 origin() {
		return this.origin;
	}

	public Vec3 direction() {
		return this.direction;
	}

	public int length() {
		return this.length;
	}

	public boolean isFinished() {
		synchronized (this.finished) {
			return this.finished.get();
		}
	}

	public void setFinished() {
		synchronized (this.finished) {
            this.finished.set(true);
		}
	}

	public void setFinishedLoc(final Vector3d vec) {
		synchronized (this.finishedLoc) {
            this.finishedLoc.set(vec);
		}
	}

	public synchronized Vector3d finishedLoc() {
		synchronized (this.finishedLoc) {
			return this.finishedLoc;
		}
	}
}
