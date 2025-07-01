package wawa.wayfinder;

import org.joml.Vector2d;

/**
 * Interpolates between two {@link org.joml.Vector2d} positions
 * <br>
 * Ideally would be more generalized but is only used for one effect so whatever
 * <br>
 * Also the name is a lie it isn't <em>linearly</em> interpolated but lerp sounds better
 */
public class LerpedVector2d {
    private Vector2d start;
    private Vector2d end;
    private double progress = 0;
    public LerpedVector2d(final Vector2d start, final Vector2d end) {
        this.start = start;
        this.end = end;
    }

    public void set(final Vector2d pos) {
        this.start = pos;
        this.end = pos;
        this.progress = 0;
    }

    public void setStart(final Vector2d start) {
        this.start = start;
    }

    public void setEnd(final Vector2d end) {
        this.end = end;
    }

    public void tickProgress(final double delta) {
        this.progress = Math.clamp(this.progress + delta, 0, 1);
    }

    /**
     * Uses a <a href="https://math.stackexchange.com/questions/121720/ease-in-out-function/121755#121755"> parametrized function</a>
     * @return fractional interpolation between start and end
     */
    public double getFrac() {
        final double alpha = 2;
        return Math.pow(this.progress, alpha) / (Math.pow(this.progress, alpha) + Math.pow(1 - this.progress, alpha));
    }

    public Vector2d get() {
        final double frac = this.getFrac();
        return new Vector2d(this.start).mul(1 - frac).add(new Vector2d(this.end).mul(frac));
    }
}
