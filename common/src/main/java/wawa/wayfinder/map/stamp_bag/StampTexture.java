package wawa.wayfinder.map.stamp_bag;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wawa.wayfinder.WayfinderClient;

import java.io.IOException;

public class StampTexture extends AbstractTexture {
    private static final int MAX_LIFE = 20;

    private int usages = 0;
    private int ticksUntilRemoval = MAX_LIFE;

    @Nullable
    private NativeImage stamp;

    public StampTexture(@Nullable NativeImage stamp) {
        this.stamp = stamp;
        if (stamp != null) {
            prepareStamp(stamp);
        }
    }

    public void tick() {
        if (stamp != null) {
            if (usages <= 0 && ticksUntilRemoval-- <= 0) {
                releaseStamp();
            } else if (usages > 0) {
                ticksUntilRemoval = MAX_LIFE;
            }

            return;
        }

        ticksUntilRemoval = MAX_LIFE;
    }

    public void addUser() {
        usages++;
    }

    public void removeUser() {
        if (usages > 0) {
            usages--;
        }
    }

    public NativeImage getTexture() {
        return this.stamp;
    }

    private void releaseStamp() {
        if (stamp != null) {
            stamp.close();
            super.releaseId();
            stamp = null;
        }
    }

    public void setFirstStamp(@NotNull NativeImage image) {
        if (stamp == null) {
            stamp = image;
            prepareStamp(stamp);
        }
    }

    private void prepareStamp(@NotNull NativeImage stamp) {
        if (!RenderSystem.isOnRenderThread()) {
            RenderSystem.recordRenderCall(() -> {
                TextureUtil.prepareImage(this.getId(), stamp.getWidth(), stamp.getHeight());
                this.upload();
            });
        } else {
            TextureUtil.prepareImage(this.getId(), stamp.getWidth(), stamp.getHeight());
            this.upload();
        }
    }

    private void upload() {
        if (this.stamp != null) {
            this.bind();
            this.stamp.upload(0, 0, 0, false);
        } else {
            WayfinderClient.LOGGER.warn("Trying to upload disposed texture {}", this.getId());
        }
    }

    @Override
    public void releaseId() {
    }

    @Override
    public void load(ResourceManager resourceManager) throws IOException {

    }
}
