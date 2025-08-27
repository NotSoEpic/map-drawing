package wawa.wayfinder.data;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.Blocks;
import org.joml.*;
import wawa.wayfinder.Helper;
import wawa.wayfinder.WayfinderClient;
import wawa.wayfinder.map.MapScreen;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SpyglassPins {
    private static final ResourceLocation PING_RENDERTYPE = WayfinderClient.id("ping");
    private static final ResourceLocation PING_TEXTURE = WayfinderClient.id("textures/gui/sprites/pin/spyglass/spyglass.png");

    public static final int MAX_PINS = 16;
    private final List<PinData> pins = new ArrayList<>();
    private int zoomlessTimer = 0;

    public Collection<PinData> getPins() {
        return this.pins;
    }

    public void add(final Vector3dc position) {
        if (this.pins.size() < MAX_PINS) {
            this.pins.add(new PinData(position));
            Minecraft.getInstance().player.playSound(SoundEvents.AMETHYST_BLOCK_FALL, 0.25f, 1);
            for (int i = 0; i < 20; i++) {
                Minecraft.getInstance().particleEngine.createParticle(
                        new BlockParticleOption(ParticleTypes.BLOCK, Blocks.COPPER_BLOCK.defaultBlockState()),
                        position.x(), position.y(), position.z(),
                        0, 0.1, 0
                );
            }
        }
    }

    public void clear() {
        if (!this.pins.isEmpty()) {
            Minecraft.getInstance().player.playSound(SoundEvents.AMETHYST_BLOCK_RESONATE, 0.25f, 1f);
            this.pins.clear();
        }
    }

    public void tick() {
        if (Minecraft.getInstance().player != null) {
            if (Helper.isUsingSpyglass(Minecraft.getInstance().player) || Minecraft.getInstance().screen instanceof MapScreen) {
                this.zoomlessTimer = 0;
            } else if (!Minecraft.getInstance().isPaused()) {
                this.zoomlessTimer++;
                if (this.zoomlessTimer > 10 * 20) {
                    this.clear();
                }
            }
        }
    }


    public void render(final MultiBufferSource bufferSource, final PoseStack poseStack, final float partialTick) {
        final Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        // billboarded rotating around Y
        final Quaternionf quaternion = new Quaternionf(0, camera.rotation().y, 0, camera.rotation().w);
        final VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityCutout(PING_TEXTURE));
        poseStack.pushPose();
        poseStack.translate(-camera.getPosition().x(), -camera.getPosition().y(), -camera.getPosition().z());
        for (final PinData ping : this.pins) {
            poseStack.pushPose();
            poseStack.translate(ping.position.x(), ping.position.y(), ping.position.z());
            renderPing(consumer, poseStack.last(), partialTick, quaternion);
            poseStack.popPose();
        }
        poseStack.popPose();
    }

    private static void renderPing(final VertexConsumer consumer, final PoseStack.Pose pose, final float partialTick, final Quaternionfc quaternion) {
        vertex(consumer, pose, -0.5f, 0f, 0.25f, 0, 1, quaternion);
        vertex(consumer, pose, 0.5f, 0f, 0.25f, 1, 1, quaternion);
        vertex(consumer, pose, 0.5f, 1f, 0.25f, 1, 0, quaternion);
        vertex(consumer, pose, -0.5f, 1f, 0.25f, 0, 0, quaternion);
    }

    private static void vertex(final VertexConsumer consumer, final PoseStack.Pose pose, final float x, final float y, final float z, final float u, final float v, final Quaternionfc quaternion) {
        final Vector3f vec = new Vector3f(x, y, z).rotate(quaternion);
        consumer.addVertex(pose, vec.x, vec.y, vec.z)
                .setNormal(0, 1, 0)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(LightTexture.FULL_BRIGHT)
                .setColor(-1);
    }

    public record PinData(Pin pin, Vector3dc position) {
        public PinData(final Vector3dc position) {
            this(new Pin(Pin.SPYGLASS_EPHEMERAL, new Vector2d(position.x(), position.z())), position);
        }
    }
}
