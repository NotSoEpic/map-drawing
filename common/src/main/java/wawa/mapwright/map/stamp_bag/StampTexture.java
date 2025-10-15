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
        if (removedFromHandler) {
            if (stamp != null) {
                releaseStamp();
            }

            return;
        }

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
        if (!removedFromHandler) {
            usages++;
        }
    }

    public void removeUser() {
        if (usages > 0) {
            usages--;
        }
    }

    public NativeImage getTexture() {
        return this.stamp;
    }

    public void releaseStamp() {
        if (stamp != null) {
            stamp.close();
            super.releaseId();
            stamp = null;
        }
    }

    public void setFirstStamp(@NotNull NativeImage image) {
        if (!removedFromHandler && stamp == null) {
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
            MapwrightClient.LOGGER.warn("Trying to upload disposed texture {}", this.getId());
        }
    }

    public void removeFromHandler() {
        removedFromHandler = true;
        releaseStamp();
        usages = 0;
    }

    @Override
    public void releaseId() {
    }

    @Override
    public void load(ResourceManager resourceManager) throws IOException {

    }
}
