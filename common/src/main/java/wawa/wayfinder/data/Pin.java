package wawa.wayfinder.data;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import org.joml.Vector2d;
import org.joml.Vector2dc;
import org.joml.Vector4dc;
import wawa.wayfinder.Helper;
import wawa.wayfinder.WayfinderClient;

import java.awt.*;
import java.util.Map;

/**
 * Waypoints to mark positions
 */
public class Pin {
    public final Type type;
    private Vector2d position = null;

    public static Map<ResourceLocation, Type> TYPES = Map.of(
            WayfinderClient.id("red"), new Type(WayfinderClient.id("red"), new Color(255, 0, 0))
    );

    public static ResourceLocation TEXTURE = WayfinderClient.id("tool/pin");
    public static ResourceLocation TEXTURE_MASK = WayfinderClient.id("tool/pin_mask");
    public static ResourceLocation TEXTURE_HIGHLIGHT = WayfinderClient.id("tool/pin_highlight");

    public Pin(final ResourceLocation id, final Color color) {
        this(new Type(id, color));
    }

    public Pin(final Type type) {
        this.type = type;
    }

    public void setPosition(final Vector2d position) {
        this.position = position;
    }

    public Vector2dc getPosition() {
        return this.position;
    }

    public void draw(final GuiGraphics guiGraphics, final double xOff, final double yOff, final boolean highlight, final Vector4dc worldBounds) {
        if (this.position != null) {
            final Vector2d pos = new Vector2d(this.position).add(xOff, yOff);
            Helper.clampWithin(pos, worldBounds);
            this.type.draw(guiGraphics, pos.x, pos.y, highlight, true);
        }
    }

    public record Type(ResourceLocation id, Color color) {
        @Override
        public int hashCode() {
            return this.id.hashCode();
        }

        public void draw(final GuiGraphics guiGraphics, double dx, double dy, final boolean highlight, final boolean onPoint) {
            if (onPoint) {
                dx -= 7;
                dy -= 15;
            }
            final int x = (int) dx;
            final int y = (int) dy;
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(dx - x, dy - y, 0); // grrrrah minecraft unecessarily uses ints for all its gui rendering and im too lazy to rewrite it all using doubles
            guiGraphics.blitSprite(TEXTURE, x, y, 16, 16);
            final float[] rgb = this.color.getRGBColorComponents(null);
            guiGraphics.blit(x, y, 0, 16, 16,
                    Minecraft.getInstance().getGuiSprites().getSprite(TEXTURE_MASK),
                    rgb[0], rgb[1], rgb[2], 1);
            if (highlight) {
                guiGraphics.blitSprite(TEXTURE_HIGHLIGHT, x-1, y-1, 18, 18);
            }
            guiGraphics.pose().popPose();
        }
    }
}
