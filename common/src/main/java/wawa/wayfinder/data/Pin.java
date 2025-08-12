package wawa.wayfinder.data;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import org.joml.*;
import wawa.wayfinder.Helper;
import wawa.wayfinder.WayfinderClient;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Waypoints to mark positions
 */
public class Pin {
    public final Type type;
    private Vector2d position = null;

    public static Type DEFAULT = new Type(WayfinderClient.id("red"),
            WayfinderClient.id("pin/red"), WayfinderClient.id("pin/red_highlight"), new Vector2i(0, 15));
    private static final List<Type> TYPES = new ArrayList<>();

    public static Type SPYGLASS_EPHEMERAL = new Type(WayfinderClient.id("spyglass_ephemeral"),
            WayfinderClient.id("pin/spyglass"), WayfinderClient.id("pin/spyglass_highlight"), new Vector2i(7, 15));

    static {
        addPinType(DEFAULT);
        addPinType(new Type(WayfinderClient.id("green"),
                WayfinderClient.id("pin/green"), WayfinderClient.id("pin/green_highlight"), new Vector2i(7, 15)));
        addPinType(new Type(WayfinderClient.id("blue"),
                WayfinderClient.id("pin/blue"), WayfinderClient.id("pin/blue_highlight"), new Vector2i(15, 15)));
    }

    private static void addPinType(final Type type) {
        TYPES.add(type);
    }

    @Nullable
    public static Type getType(final ResourceLocation id) {
        for (final Type type : TYPES) {
            if (type.id.equals(id)) {
                return type;
            }
        }
        return null;
    }

    public static Collection<Type> getTypes() {
        return TYPES;
    }

    public Pin(final Type type) {
        this.type = type;
    }

    public Pin(final Type type, final Vector2d position) {
        this(type);
        this.position = position;
    }

    public void setPosition(final Vector2d position) {
        this.position = position;
    }

    public Vector2dc getPosition() {
        return this.position;
    }

    public void draw(final GuiGraphics guiGraphics, final double xOff, final double yOff, final float scale, final boolean highlight, final Vector4dc worldBounds) {
        if (this.position != null) {
            final Vector2d pos = new Vector2d(this.position).add(xOff, yOff).mul(scale);
            Helper.clampWithin(pos, worldBounds);
            this.type.draw(guiGraphics, pos.x, pos.y, highlight, true);
        }
    }

    public record Type(ResourceLocation id, ResourceLocation texture, ResourceLocation highlight, Vector2ic pointOffset) {
        @Override
        public int hashCode() {
            return this.id.hashCode();
        }

        public void draw(final GuiGraphics guiGraphics, double dx, double dy, final boolean highlight, final boolean onPoint) {
            if (onPoint) {
                dx -= this.pointOffset.x();
                dy -= this.pointOffset.y();
            }
            final int x = (int) dx;
            final int y = (int) dy;
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(dx - x, dy - y, 0); // grrrrah minecraft unecessarily uses ints for all its gui rendering and im too lazy to rewrite it all using doubles
            guiGraphics.blitSprite(this.texture, x, y, 16, 16);
            if (highlight) {
                guiGraphics.blitSprite(this.highlight, x-1, y-1, 18, 18);
            }
            guiGraphics.pose().popPose();
        }
    }
}
