package wawa.wayfinder.map.tool;

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
import wawa.wayfinder.Rendering;
import wawa.wayfinder.WayfinderClient;
import wawa.wayfinder.data.PageManager;
import wawa.wayfinder.map.stamp_bag.StampInformation;
import wawa.wayfinder.map.stamp_bag.StampTexture;
import wawa.wayfinder.map.widgets.MapWidget;

public class StampTool extends Tool {
	public static StampTool INSTANCE = new StampTool();
	private static final ResourceLocation TEXTURE = WayfinderClient.id("tool/stamp/stamp");

	private StampInformation information = null;
	private boolean registeredTexture = false;
	private ResourceLocation textureLoc = null;

	//effectively our on select now!
	public void setActiveStamp(@Nullable StampInformation stampInformation) {
		changeStamp(stampInformation);
	}

	@Override
	public void onDeselect() {
		changeStamp(null);
		Minecraft.getInstance().getTextureManager().release(textureLoc);
		registeredTexture = false;
		textureLoc = null;
	}

	private void changeStamp(@Nullable StampInformation stampInformation) {
		if (information != stampInformation) {
			if (information != null) {
				information.getTextureManager().removeUser();
			}

			if (stampInformation != null) {
				stampInformation.getTextureManager().addUser();
			}
		}

		this.information = stampInformation;
	}

	@Override
	public void mouseDown(PageManager activePage, MapWidget.MouseType mouseType, Vector2d world) {
		if (mouseType == MapWidget.MouseType.LEFT) {
			if (information != null) { //idk how this would be the case...
				StampTexture manager = information.getTextureManager();
				NativeImage texture = manager.getTexture();
				if (texture == null) {
					return;
				}

				//just making sure we can actually see the texture AND minecraft knows about it first
				if (registeredTexture) {
					world = world.add(0, 8);
					final Vector2ic end = new Vector2i(world, RoundingMode.FLOOR);

					activePage.putRegion(end.x() - texture.getWidth() / 2, end.y() - texture.getHeight() / 2, texture.getWidth(), texture.getHeight(),
							(dx, dy, old) -> {
								int pixelColor = texture.getPixelRGBA(dx, dy);
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
	public void renderWorld(GuiGraphics graphics, int worldX, int worldY, double xOff, double yOff) {
		if (information != null) {
			StampTexture manager = information.getTextureManager();
			NativeImage texture = manager.getTexture();
			if (texture == null) {
				return;
			}

			if (!registeredTexture) {
				registeredTexture = true;
				ResourceLocation managerLocation = WayfinderClient.id("stamp_tool_image");
				this.textureLoc = managerLocation;

				Minecraft.getInstance().getTextureManager().register(managerLocation, manager);
			}

			final RenderType renderType = VeilRenderType.get(Rendering.RenderTypes.PALETTE_SWAP, textureLoc);
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
	public void renderScreen(GuiGraphics graphics, double mouseX, double mouseY) {
		graphics.blitSprite(TEXTURE, (int) mouseX - 8, (int) mouseY - 8, 16, 16);
	}
}