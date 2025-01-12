package wawa.wayfinder.mapmanager.tools;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.joml.RoundingMode;
import org.joml.Vector2d;
import org.joml.Vector2i;
import org.lwjgl.opengl.GL11;
import wawa.wayfinder.AllComponents;
import wawa.wayfinder.WayfinderClient;
import wawa.wayfinder.mapmanager.MapWidget;
import wawa.wayfinder.rendering.WayfinderRenderTypes;

import java.util.HashSet;
import java.util.Set;

public class StampTool extends Tool {
    public final ResourceLocation stamp;
    private static DynamicTexture texture;
    private final int w;
    private final int h;
    public StampTool(ResourceLocation stamp) {
        this.stamp = stamp;
        Minecraft.getInstance().getTextureManager().getTexture(stamp).bind();
        w = GlStateManager._getTexLevelParameter(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_WIDTH);
        h = GlStateManager._getTexLevelParameter(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_HEIGHT);
    }
    @Override
    public void onSelect() {
        texture = new DynamicTexture(w, h, false);
        Minecraft.getInstance().getTextureManager().getTexture(stamp).bind();
        texture.getPixels().downloadTexture(0, false);
    }

    @Override
    public void leftDown(MapWidget widget, boolean shift, Vector2d mouse, Vector2i world) {
        WayfinderClient.regions.clearHistory();
        widget.putTextureWorld(world.x, world.y, texture.getPixels());
    }

    @Override
    public void rightDown(MapWidget widget, boolean shift, Vector2d mouse, Vector2i world) {
        WayfinderClient.regions.clearHistory();
        widget.putTextureWorld(world.x, world.y, texture.getPixels(), (pixel, current) -> 0);
    }

    @Override
    public void render(MapWidget widget, GuiGraphics context, boolean shift, Vector2d mouse, Vector2i world) {
        int sw = (int) (w * widget.scale);
        int sh = (int) (h * widget.scale);
        world = new Vector2i(widget.worldToScreen(world.x - w/2, world.y - h/2, true), RoundingMode.FLOOR);
        context.blit(WayfinderRenderTypes::getPaletteSwap, stamp,
                world.x, world.y, 0, 0, sw, sh, sw, sh);
    }

    @Override
    public boolean hideMouse(MapWidget widget, Vector2d mouse, Vector2i world) {
        return true;
    }

    public static Set<ResourceLocation> collectAvailableStamps(LocalPlayer player) {
        Set<ResourceLocation> stamps = new HashSet<>();
        collectAvailableStamps(stamps, player.getInventory().items);
        return stamps;
    }

    private static void collectAvailableStamps(Set<ResourceLocation> stamps, Iterable<ItemStack> items) {
        items.forEach(i -> {
            if (i.has(AllComponents.STAMP)) {
                stamps.add(i.get(AllComponents.STAMP).texture());
            }
            if (i.has(DataComponents.BUNDLE_CONTENTS)) {
                collectAvailableStamps(stamps, i.get(DataComponents.BUNDLE_CONTENTS).items());
            }
            if (i.has(DataComponents.CONTAINER)) {
                collectAvailableStamps(stamps, i.get(DataComponents.CONTAINER).nonEmptyItems());
            }
        });
    }
}
