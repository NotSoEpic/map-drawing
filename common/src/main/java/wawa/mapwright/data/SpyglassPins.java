package wawa.mapwright.data;

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
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.Blocks;
import org.joml.*;
import wawa.mapwright.DistantRaycast;
import wawa.mapwright.Helper;
import wawa.mapwright.MapwrightClient;
import wawa.mapwright.compat.multithread_testing.DhRequest;
import wawa.mapwright.compat.multithread_testing.MultithreadedDHTerrainAccess;
import wawa.mapwright.map.MapScreen;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SpyglassPins {
	private static final ResourceLocation PING_RENDERTYPE = MapwrightClient.id("ping");
	private static final ResourceLocation PING_TEXTURE = MapwrightClient.id("textures/gui/sprites/pin/spyglass/spyglass.png");
	private static final Vector3d EMPTY = new Vector3d(); //don't mutate

	public static final int MAX_PINS = 16;
	private final List<PinData> pins = new ArrayList<>();
	private final List<DhRequest> delayedPins = new ArrayList<>();
	private int zoomlessTimer = 0;

	public List<PinData> getPins() {
		return this.pins;
	}

	public void addDelayedRequest(final DhRequest request) {
        this.delayedPins.add(request);
		MultithreadedDHTerrainAccess.INSTANCE.addRequest(request);
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
            this.delayedPins.clear();

			MultithreadedDHTerrainAccess.INSTANCE.voidRequests();
			DistantRaycast.clearCache();
		}
	}

	public void tick() {
		if (Minecraft.getInstance().player != null) {
			//filter and move all delayed requests into pin data

			final int size = this.delayedPins.size();
			final Iterator<DhRequest> iter = this.delayedPins.iterator();
			while (iter.hasNext()) {
				final DhRequest req = iter.next();
				if (req.isFinished()) {
					iter.remove();

					if (!req.finishedLoc().equals(EMPTY)) {
                        this.add(req.finishedLoc());

						if (size == 1) {
							MapwrightClient.targetPanningPosition.set(req.finishedLoc().x, req.finishedLoc().z);
						}
					}
				}
			}

			if (!this.delayedPins.isEmpty()) {
				boolean oneTookLong = false;
				for (final DhRequest delayedPin : this.delayedPins) {
					final int ticks = delayedPin.ticksSinceRequested.incrementAndGet();
					if (ticks > 20) {
						oneTookLong = true;
					}
				}

				if (oneTookLong) {
					Minecraft.getInstance().player.displayClientMessage(Component.literal("Triangulating Position..."), true);
				}
			}

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
