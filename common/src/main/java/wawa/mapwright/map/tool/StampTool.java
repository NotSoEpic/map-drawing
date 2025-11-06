package wawa.mapwright.map.tool;

import com.mojang.blaze3d.platform.NativeImage;
import foundry.veil.api.client.render.rendertype.VeilRenderType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import org.joml.RoundingMode;
import org.joml.Vector2d;
import org.joml.Vector2i;
import org.joml.Vector2ic;
import wawa.mapwright.MapwrightClient;
import wawa.mapwright.Rendering;
import wawa.mapwright.data.PageManager;
import wawa.mapwright.map.stamp_bag.StampInformation;
import wawa.mapwright.map.stamp_bag.StampTexture;
import wawa.mapwright.map.widgets.MapWidget;

public class StampTool extends Tool {
	public static StampTool INSTANCE = new StampTool();
	private static final ResourceLocation TEXTURE = MapwrightClient.id("tool/stamp/stamp");

	private StampInformation information = null;
	private boolean registeredTexture = false;
	private ResourceLocation textureLoc = null;

	@Override
	public void onDeselect() {
		setActiveStamp(null);

		if (isTempStamp()) {
			//make sure we release the stamp if this is a temp one
			information.getTextureManager().releaseStamp();
		}
	}

	public void setActiveStamp(@Nullable final StampInformation stampInformation) {
		if (registeredTexture) { //always make sure we release the texture before we do anything else
			registeredTexture = false;
			Minecraft.getInstance().getTextureManager().release(textureLoc);
			textureLoc = null;
		}

        this.changeStamp(stampInformation);
	}

	private void changeStamp(@Nullable final StampInformation stampInformation) {
		if (this.information != stampInformation) {
			if (this.information != null) {
                this.information.getTextureManager().removeUser();
			}

			if (stampInformation != null) {
				stampInformation.getTextureManager().addUser();
			}
		}

		this.information = stampInformation;
	}

	@Override
	public void mouseDown(final PageManager activePage, final MapWidget.MouseType mouseType, Vector2d world) {
		if (mouseType == MapWidget.MouseType.LEFT) {
			if (this.information != null) { //idk how this would be the case...
				final StampTexture manager = this.information.getTextureManager();
				final NativeImage texture = manager.getTexture();
				if (texture == null) {
					return;
				}

				//just making sure we can actually see the texture AND minecraft knows about it first
				if (this.registeredTexture) {
					world = world.add(0, 8);
					final Vector2ic end = new Vector2i(world, RoundingMode.FLOOR);

					activePage.putRegion(end.x() - texture.getWidth() / 2, end.y() - texture.getHeight() / 2, texture.getWidth(), texture.getHeight(),
							(dx, dy, old) -> {
								final int pixelColor = texture.getPixelRGBA(dx, dy);
								if (pixelColor == 0) {
									return activePage.getPixelARGB((end.x() - texture.getWidth() / 2) + dx, (end.y() - texture.getHeight() / 2) + dy);
								} else {
									return texture.getPixelRGBA(dx, dy);
								}
							}
					);
				}
			}
		}
	}

	@Override
	public void renderWorld(final GuiGraphics graphics, final int worldX, int worldY, final double xOff, final double yOff) {
		if (this.information != null) {
			final StampTexture manager = this.information.getTextureManager();
			final NativeImage texture = manager.getTexture();
			if (texture == null) {
				return;
			}

			if (!this.registeredTexture) {
                this.registeredTexture = true;
				final ResourceLocation managerLocation = MapwrightClient.id("stamp_tool_image");
				this.textureLoc = managerLocation;

				Minecraft.getInstance().getTextureManager().register(managerLocation, manager);
			}

			final RenderType renderType = VeilRenderType.get(Rendering.RenderTypes.PALETTE_SWAP, this.textureLoc);
			if (renderType == null) {
				return;
			}

			worldY = worldY + 8;

			Rendering.renderTypeBlit(graphics, renderType, worldX + xOff - (double) (texture.getWidth() / 2), worldY + yOff - (double) (texture.getHeight() / 2), 0, 0f, 0f,
					texture.getWidth(), texture.getHeight(), texture.getWidth(), texture.getHeight(), 1);

			graphics.renderOutline((int) (worldX + xOff - (double) (texture.getWidth() / 2)), (int) (worldY + yOff - (double) (texture.getHeight() / 2)),
					texture.getWidth(), texture.getHeight(),
					0xff000000);
		}
	}

	@Override
	public void renderScreen(final GuiGraphics graphics, final double mouseX, final double mouseY) {
		graphics.blitSprite(TEXTURE, (int) mouseX - 8, (int) mouseY - 8, 16, 16);
	}

	public boolean isTempStamp() {
		return information == MapwrightClient.STAMP_HANDLER.temporaryStampInformation;
	}
}