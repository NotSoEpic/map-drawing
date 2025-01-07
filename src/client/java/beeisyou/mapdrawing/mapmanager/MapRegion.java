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
        texture.upload();
        MinecraftClient.getInstance().getTextureManager().registerTexture(id, texture);
    }

    public boolean inBoundsRel(int x, int z) {
        return x >= 0 && x < 512 && z >= 0 && z < 512;
    }

    public boolean inBoundsAbs(int x, int z) {
        return inBoundsRel(x - rx * 512, z - rz * 512);
    }

    public boolean putPixelAbs(int x, int z, int color) {
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
