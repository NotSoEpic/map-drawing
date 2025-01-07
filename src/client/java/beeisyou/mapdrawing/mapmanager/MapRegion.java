package beeisyou.mapdrawing.mapmanager;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;

public class MapRegion {
    public final int rx;
    public final int rz;
    private final NativeImageBackedTexture texture;
    public final Identifier id;
    private boolean dirty = false; // unuploaded changes
    public MapRegion(int rx, int rz) {
        this.rx = rx;
        this.rz = rz;
        texture = new NativeImageBackedTexture(512, 512, false); // each chunk region is 512x512 blocks, seems fitting
        id = Identifier.of("mapmanager", String.format("map_%d_%d", rx, rz));
        texture.getImage().apply(i -> 0); // reset to clear
//        int l = ((rx * 21673 + rz * 2938437) % 32 + 32) % 32;
//        texture.getImage().apply(i -> ColorHelper.getArgb(l, l, l)); // reset to clear
//        texture.getImage().fillRect(10, 0, 3, 512, ColorHelper.getArgb(0, 255, 0));
//        texture.getImage().fillRect(0, 10, 512, 3, ColorHelper.getArgb(0, 255, 0));
//        texture.getImage().fillRect(501, 0, 3, 512, ColorHelper.getArgb(0, 255, 0));
//        texture.getImage().fillRect(0, 501, 512, 3, ColorHelper.getArgb(0, 255, 0));
        texture.upload();
        MinecraftClient.getInstance().getTextureManager().registerTexture(id, texture);
    }

    public boolean inBoundsRel(int x, int z) {
        return x >= 0 && x < 512 && z >= 0 && z < 512;
    }

    public boolean inBoundsAbs(int x, int z) {
        return inBoundsRel(x - rx * 512, z - rz * 512);
    }

    public boolean putPixelWorld(int x, int z, int color) {
        if (!inBoundsAbs(x, z))
            return false;
        texture.getImage().setColorArgb(x - rx * 512, z - rz * 512, color);
        dirty = true;
        return true;
    }

    public void checkDirty() {
        if (dirty) {
            texture.upload();
            dirty = false;
        }
    }

    public void clear() {
        texture.close();
    }
}
