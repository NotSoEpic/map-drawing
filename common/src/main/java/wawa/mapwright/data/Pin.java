package wawa.mapwright.data;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2d;
import org.joml.Vector2dc;
import org.joml.Vector4dc;
import wawa.mapwright.Helper;
import wawa.mapwright.MapwrightClient;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Waypoints to mark positions
 */
public class Pin {
    public final Type type;
    private Vector2dc position = null;

    public static Type DEFAULT = Type.simpleShorthand("red");
    private static final List<Type> TYPES = new ArrayList<>();

    public static Type SPYGLASS_EPHEMERAL = Type.simpleShorthand("spyglass");

    static {
        addPinType(DEFAULT);
        addPinType(Type.simpleShorthand("green"));
        addPinType(Type.simpleShorthand("blue"));
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

    public void setPosition(final Vector2dc position) {
        this.position = position;
    }

    public Vector2dc getPosition() {
        return this.position;
    }

    public void draw(final GuiGraphics guiGraphics, final Vector2dc mouseScreen, final double xOff, final double yOff, final float scale, final boolean highlight, final Vector4dc worldBounds) {
        if (this.position != null) {
            final Vector2d pos = new Vector2d(this.position).add(xOff, yOff).mul(scale); // world position to screen position
            final float alpha = Helper.getMouseProximityFade(mouseScreen, pos);
            Helper.clampWithin(pos, worldBounds);
            this.type.draw(guiGraphics, pos.x, pos.y, highlight, true, alpha);
        }
    }

    public record Type(ResourceLocation id, ResourceLocation uiTexture, ResourceLocation highlight, ResourceLocation positionedTexture, ResourceLocation positionedHighlight) {
        @Override
        public int hashCode() {
            return this.id.hashCode();
        }

        public static Type simpleShorthand(final String name) {
            return new Type(
                    MapwrightClient.id(name),
                    MapwrightClient.id("pin/" + name + "/" + name),
                    MapwrightClient.id("pin/" + name + "/" + name + "_highlight"),
                    MapwrightClient.id("pin/" + name + "/" + name + "_positioned"),
                    MapwrightClient.id("pin/" + name + "/" + name + "_positioned_highlight")
            );
        }

        public void draw(final GuiGraphics guiGraphics, final double dx, final double dy, final boolean highlight, final boolean onPoint, final float alpha) {
            final int x = (int) dx;
            final int y = (int) dy;
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(dx - x, dy - y, 0); // grrrrah minecraft unecessarily uses ints for all its gui rendering and im too lazy to rewrite it all using doubles
            if (onPoint) {
                guiGraphics.blit(x - 16, y - 16, 0, 32, 32, Minecraft.getInstance().getGuiSprites().getSprite(this.positionedTexture), 1f, 1f, 1f, alpha);
                if (highlight) {
                    guiGraphics.blit(x - 17, y - 17, 0, 34, 34, Minecraft.getInstance().getGuiSprites().getSprite(this.positionedHighlight), 1f, 1f, 1f, alpha);
                }
            } else {
                guiGraphics.blit(x, y, 0, 16, 16, Minecraft.getInstance().getGuiSprites().getSprite(this.uiTexture), 1f, 1f, 1f, alpha);
                if (highlight) {
                    guiGraphics.blit(x - 1, y - 1, 0, 18, 18, Minecraft.getInstance().getGuiSprites().getSprite(this.highlight), 1f, 1f, 1f, alpha);
                }
            }
            guiGraphics.pose().popPose();
        }
    }
}
