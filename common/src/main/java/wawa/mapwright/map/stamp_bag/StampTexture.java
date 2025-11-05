package wawa.mapwright.map.stamp_bag;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wawa.mapwright.MapwrightClient;

import java.io.IOException;

public class StampTexture extends AbstractTexture {
    private static final int MAX_LIFE = 20 * 30; //30 seconds after no users, remove memory

    private int usages = 0;
    private int ticksUntilRemoval = MAX_LIFE;

    private boolean removedFromHandler = false;

    @Nullable
    private NativeImage stamp;

    public void tick() {
        if (this.removedFromHandler) {
            if (this.stamp != null) {
                this.releaseStamp();
            }

            return;
        }

        if (this.stamp != null) {
            if (this.usages <= 0 && this.ticksUntilRemoval-- <= 0) {
                this.releaseStamp();
            } else if (this.usages > 0) {
                this.ticksUntilRemoval = MAX_LIFE;
            }

            return;
        }

        this.ticksUntilRemoval = MAX_LIFE;
    }

    public void addUser() {
        if (!this.removedFromHandler) {
            this.usages++;
        }
    }

    public void removeUser() {
        if (this.usages > 0) {
            this.usages--;
        }
    }

    public NativeImage getTexture() {
        return this.stamp;
    }

    public void releaseStamp() {
        if (this.stamp != null) {
            this.stamp.close();
            super.releaseId();
            this.stamp = null;
        }
    }

    public void setFirstStamp(@NotNull final NativeImage image) {
        if (!this.removedFromHandler && this.stamp == null) {
            this.stamp = image;
            this.prepareStamp(this.stamp);
        }
    }

    private void prepareStamp(@NotNull final NativeImage stamp) {
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
            MapwrightClient.LOGGER.warn("Trying to upload disposed texture {}", this.getId());
        }
    }

    public void removeFromHandler() {
        this.removedFromHandler = true;
        this.releaseStamp();
        this.usages = 0;
    }

    @Override
    public void releaseId() {
    }

    @Override
    public void load(final ResourceManager resourceManager) throws IOException {

    }
}
