package wawa.mapwright.map;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import wawa.mapwright.MapwrightClient;
import wawa.mapwright.gui.GUIElementAtlases;
import wawa.mapwright.map.stamp_bag.StampInformation;
import wawa.mapwright.map.stamp_bag.widgets.DualGUIElement;
import wawa.mapwright.map.stamp_bag.widgets.GUIElementButton;
import wawa.mapwright.map.stamp_bag.widgets.StampEntryWidget;
import wawa.mapwright.map.tool.StampBagDebuggerTool;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class StampBagScreen {

	public static final StampBagScreen INSTANCE = new StampBagScreen();

	public MapScreen mapScreen;
	private ScreenState state = ScreenState.IDLE;
	private final Set<AbstractWidget> allWidgets = new HashSet<>();
	private final Set<AbstractWidget> activeWidgets = new HashSet<>();

	private int page = 1;
	boolean usingSearch = false;
	boolean usingFavorites = false;
	private final List<StampInformation> requestedInfo = new ArrayList<>();

	private final EditBox saveText;
	private GUIElementButton confirmSave;
	private GUIElementButton cancelSave;

	private final GUIElementButton up;
	private final GUIElementButton down;
	private final GUIElementButton favorites;
	private final EditBox searchWidget;

	public StampEntry[] entries = new StampEntry[3];
	private final int browseBGWidth = 178;
	private final int browseBGY = 100;
	private final int browseBGHeight = 138;

	public StampBagScreen() {
        this.saveText = new EditBox(Minecraft.getInstance().font, 72, 17, Component.empty());
        this.saveText.setFocused(false);
        this.saveText.setCanLoseFocus(true);
        this.saveText.setMaxLength(72);

        this.createSaveWidgets();
        this.createEntryWidgets();

        this.up = new GUIElementButton(0, 0, 16, GUIElementAtlases.STAMP_BAG_BROWSE_UP, b -> {
			final int maxPageCount;
			if (this.usingSearch) {
				maxPageCount = this.requestedInfo.size();
			} else if (this.usingFavorites) {
				maxPageCount = MapwrightClient.STAMP_HANDLER.getTotalEntryCount(true);
			} else {
				maxPageCount = MapwrightClient.STAMP_HANDLER.getTotalEntryCount();
			}

            this.page = (int) Math.clamp(this.page - 1, 1, maxPageCount == 0 ? 1 : Math.ceil(maxPageCount / 3f));
            this.refreshStamps();
		});

        this.down = new GUIElementButton(0, 0, 16, GUIElementAtlases.STAMP_BAG_BROWSE_DOWN, b -> {
			final int maxPageCount;
			if (this.usingSearch) {
				maxPageCount = this.requestedInfo.size();
			} else if (this.usingFavorites) {
				maxPageCount = MapwrightClient.STAMP_HANDLER.getTotalEntryCount(true);
			} else {
				maxPageCount = MapwrightClient.STAMP_HANDLER.getTotalEntryCount();
			}

            this.page = (int) Math.clamp(this.page + 1, 1, maxPageCount == 0 ? 1 : Math.ceil(maxPageCount / 3f));
            this.refreshStamps();
		});

        this.favorites = new DualGUIElement(0, 0, 16, GUIElementAtlases.STAMP_BAG_BROWSE_UNFAVORITE, GUIElementAtlases.STAMP_BAG_BROWSE_FAVORITE, b -> {
            this.usingFavorites ^= true;
            this.page = 1;

			((DualGUIElement) b).imageSwitch = this.usingFavorites;

            this.refreshStamps();
		});

        this.searchWidget = new EditBox(Minecraft.getInstance().font, GUIElementAtlases.STAMP_BAG_BROWSE_ENTRY.width() - 1, 16, Component.empty());
        this.searchWidget.setResponder(/*why not just call it a callback...*/ s -> {
            this.usingSearch = !s.isEmpty();
			if (!s.isEmpty()) {
                this.page = 1;
			}
            this.refreshStamps();
		});
        this.searchWidget.setFocused(false);
        this.searchWidget.setCanLoseFocus(true);
        this.searchWidget.setMaxLength(72);
	}

	private void createSaveWidgets() {
        this.confirmSave = new GUIElementButton(0, 0, 16, GUIElementAtlases.STAMP_BAG_SAVE_CONFIRM, (b) -> {
			if (!this.saveText.getValue().isEmpty() && this.mapScreen != null) {
				final NativeImage clipboard = this.mapScreen.toolPicker.getImageFromScissorTool();
				if (clipboard != null) {
					final NativeImage copied = new NativeImage(clipboard.getWidth(), clipboard.getHeight(), false);
					copied.copyFrom(clipboard);

					MapwrightClient.STAMP_HANDLER.addNewStamp(copied, this.saveText.getValue());
                    this.saveText.setValue("");
					this.mapScreen.toolPicker.pickHand();
					this.changeStage(ScreenState.BROWSING);
					return;
				}
			}

            this.changeStage(ScreenState.IDLE);
		});

        this.cancelSave = new GUIElementButton(0, 0, 16, GUIElementAtlases.STAMP_BAG_SAVE_CANCEL, (b) -> {
            this.saveText.setValue("");
            this.changeStage(ScreenState.IDLE);
		});

        this.allWidgets.addAll(List.of(this.saveText, this.confirmSave, this.cancelSave));
	}

	private void createEntryWidgets() {
		for (int i = 0; i < this.entries.length; i++) {
            this.entries[i] = new StampEntry();
			final StampEntry entry = this.entries[i];

			entry.self = new StampEntryWidget(0, 0, this, (wid) -> {
//				if (wid.stampInformation != null && wid.stampInformation.getTextureManager().getTexture() != null) {
//                    CopyTool.INSTANCE.clipboard = wid.stampInformation.getTextureManager().getTexture();
//				}
			}, MapwrightClient.id("stamp_widget_" + i));

			entry.delete = new GUIElementButton(0, 0, 16, GUIElementAtlases.STAMP_BAG_BROWSE_TRASH, (b) -> {
				final StampInformation si = entry.self.stampInformation;
				if (si != null) {
					MapwrightClient.STAMP_HANDLER.removeStamp(si);
				}

                this.refreshStamps();
			});

			entry.favorite = new DualGUIElement(0, 0, 16, GUIElementAtlases.STAMP_BAG_BROWSE_UNFAVORITE, GUIElementAtlases.STAMP_BAG_BROWSE_FAVORITE, (b) -> {
				final StampInformation si = entry.self.stampInformation;
				if (si != null) {
					si.setFavorited(!si.isFavorited());
					((DualGUIElement) b).imageSwitch = si.isFavorited();
					MapwrightClient.STAMP_HANDLER.setDirty();
				}
			});
		}
	}

	public void setMapScreen(final MapScreen screen) {
		this.mapScreen = screen;
	}

	public void resetWidgetInfo() {
        this.allWidgets.forEach(w -> {
			w.active = false;
			w.setFocused(false);
		});

		//saving
		final int stampBagX = this.mapScreen.stampBag.getX();
		final int stampBagY = this.mapScreen.stampBag.getY();

		setWidgetXY(this.saveText, stampBagX - 123, stampBagY - 1);
		setWidgetXY(this.confirmSave, stampBagX - 49, stampBagY);
		setWidgetXY(this.cancelSave, stampBagX - 28, stampBagY);
		//

		//browsing
		final int backgroundX = (this.mapScreen.width - 15 - 16 / 2) - (148 + 16) - 21;
		final int backgroundY = 100;

		for (int i = 0; i < this.entries.length; i++) {
			final int entryY = backgroundY + 26 + (i * 37);
			final StampEntry entry = this.entries[i];
			setWidgetXY(entry.self, backgroundX + 10, entryY);
			setWidgetXY(entry.favorite, backgroundX + GUIElementAtlases.STAMP_BAG_BROWSE_ENTRY.width() + 9, entryY);
			setWidgetXY(entry.delete, backgroundX + GUIElementAtlases.STAMP_BAG_BROWSE_ENTRY.width() + 9, entryY + 16);
		}

		setWidgetXY(this.up, backgroundX + GUIElementAtlases.STAMP_BAG_BROWSE_ENTRY.width() + 28, backgroundY + 26);
		setWidgetXY(this.down, backgroundX + GUIElementAtlases.STAMP_BAG_BROWSE_ENTRY.width() + 28, backgroundY + 5 + (3 * 37));
		setWidgetXY(this.favorites, backgroundX + GUIElementAtlases.STAMP_BAG_BROWSE_ENTRY.width() + 9, backgroundY + 5);
		setWidgetXY(this.searchWidget, backgroundX + 10, backgroundY + 5);
		//
	}

	private static void setWidgetXY(final AbstractWidget w, final int x, final int y) {
		w.setX(x);
		w.setY(y);
	}

	public void changeStage(final ScreenState newStage) {
		for (final AbstractWidget w : this.activeWidgets) {
			//TODO: properly fix this
            this.mapScreen.removeWidget(w);
			w.setFocused(false);
			w.active = false;
		}
        this.resetWidgetInfo();
        this.activeWidgets.clear();

		if (this.state == ScreenState.BROWSING) {
			for (final StampInformation si : this.requestedInfo) {
				si.getTextureManager().removeUser();
			}
		}

        this.state = newStage;
		switch (newStage) {
			case SAVING -> {
                this.addWidget(this.saveText);
                this.addWidget(this.confirmSave);
                this.addWidget(this.cancelSave);

				//we can comment this out if we don't want this behaviour when saving a stamp!
                this.setMapScreenWidgetActivity(false);
			}

			case BROWSING -> {
				if (this.refreshStamps()) {
					return;
				}

                this.addWidget(this.up);
                this.addWidget(this.down);
                this.addWidget(this.searchWidget);
                this.addWidget(this.favorites);
			}

			case IDLE -> this.setMapScreenWidgetActivity(true);
		}
	}

	private boolean refreshStamps() {
		for (final StampInformation si : this.requestedInfo) {
			si.getTextureManager().removeUser();
		}
        this.requestedInfo.clear();

		final int pageLoc = 3 * this.page;
		if (this.usingSearch) {
			MapwrightClient.STAMP_HANDLER.requestStampContaining(this.requestedInfo, this.searchWidget.getValue(), this.usingFavorites);
		} else {
			MapwrightClient.STAMP_HANDLER.bulkRequestStamps(this.requestedInfo, this.usingFavorites, pageLoc - 3, pageLoc - 2, pageLoc - 1);
		}

		for (int i = 0; i < 3; i++) {
			int index = i;
			if (this.usingSearch) {
				index = pageLoc - (3 - i);
			}

			final StampEntry entry = this.entries[i];

			if (!this.requestedInfo.isEmpty() && index >= 0 && index <= this.requestedInfo.size() - 1) {
				final StampInformation si = this.requestedInfo.get(index);

				entry.self.changeStampInformation(si);
                this.addWidget(entry.self);
                this.addWidget(entry.delete);
                this.addWidget(entry.favorite);
				entry.favorite.imageSwitch = si.isFavorited();

				entry.addedWidgets = true;
			} else {
                this.removeWidget(entry.self);
                this.removeWidget(entry.delete);
                this.removeWidget(entry.favorite);

				entry.self.changeStampInformation(null);
				entry.addedWidgets = false;
			}
		}

		for (final StampInformation si : this.requestedInfo) {
			si.getTextureManager().addUser();
		}

		return false;
	}

	private void setMapScreenWidgetActivity(final boolean active) {
		for (final AbstractWidget w : this.mapScreen.allWidgets) {
			w.active = active;
		}

		//always keep the stamp bag widget active
        this.mapScreen.stampBag.active = true;
	}

	public void addWidget(final AbstractWidget wid) {
		wid.active = true;
        this.mapScreen.addWidget(wid);
        this.activeWidgets.add(wid);
	}

	public void removeWidget(final AbstractWidget wid) {
        this.mapScreen.removeWidget(wid);
        this.activeWidgets.remove(wid);
		wid.setFocused(false);
		wid.active = false;
	}

	public void renderScreen(final GuiGraphics guiGraphics, final int mouseX, final int mouseY, final float partialTick) {
		final PoseStack ps = guiGraphics.pose();

		ps.pushPose();

		ps.pushPose();
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
		ps.popPose();

		ps.pushPose();
		for (final AbstractWidget w : this.activeWidgets) {
			ps.pushPose();
			w.render(guiGraphics, mouseX, mouseY, partialTick);
			ps.popPose();
		}
		ps.popPose();
	}

	public void renderBackground(final GuiGraphics guiGraphics, final int mouseX, final int mouseY, final float partialTick) {
		switch (this.state) {
			case SAVING -> {
				final GUIElementAtlases background = GUIElementAtlases.STAMP_BAG_SAVE;
				final int x = this.mapScreen.stampBag.getX() - background.width();
				final int y = this.mapScreen.stampBag.getY() - 5;
				background.render(guiGraphics, x, y);
			}

			case BROWSING -> {
				final int browseBGX = (this.mapScreen.width - 15 - 16 / 2) - (164) - 21;
				guiGraphics.blitSprite(StampBagDebuggerTool.backgroundID, browseBGX, this.browseBGY, this.browseBGWidth, this.browseBGHeight);

				for (int i = 0; i < this.entries.length; i++) {
					if (this.entries[i].addedWidgets) {
						GUIElementAtlases.STAMP_BAG_BROWSE_ENTRY.render(guiGraphics, browseBGX + 10, this.browseBGY + 26 + (i * 37));
					}

					final int maxPageCount;
					if (this.usingSearch) {
						maxPageCount = this.requestedInfo.size();
					} else if (this.usingFavorites) {
						maxPageCount = MapwrightClient.STAMP_HANDLER.getTotalEntryCount(true);
					} else {
						maxPageCount = MapwrightClient.STAMP_HANDLER.getTotalEntryCount();
					}

					guiGraphics.drawString(Minecraft.getInstance().font, "%s/%s".formatted(maxPageCount == 0 ? 0 : this.page, (int) Math.ceil(maxPageCount / 3f)), browseBGX + GUIElementAtlases.STAMP_BAG_BROWSE_ENTRY.width() + 27, this.browseBGY + 74, Color.WHITE.getRGB(), true);
				}
			}
		}
	}

	public void mouseClicked(final double mouseX, final double mouseY, final int button) {
		if (this.state != ScreenState.IDLE && this.state != ScreenState.SAVING) {
			final int browseBGX = (this.mapScreen.width - 15 - 16 / 2) - (164) - 21;
			if ((mouseX < browseBGX || mouseX > browseBGX + this.browseBGWidth || mouseY < this.browseBGY || mouseY > this.browseBGY + this.browseBGHeight) && !this.mapScreen.stampBag.isHovered()) {
                this.changeStage(ScreenState.IDLE);
			}
		}
	}

	public void parentClose() {
		for (final AbstractWidget w : this.activeWidgets) {
			w.setFocused(false);
            this.mapScreen.removeWidget(w);
		}

        this.mapScreen = null;
	}

	public ScreenState getState() {
		return this.state;
	}

	public boolean hasAnyTextBoxFoxused() {
		return this.saveText.isFocused() || this.searchWidget.isFocused();
	}

	public enum ScreenState {
		IDLE, SAVING, BROWSING, SEARCHING, FAVORITING
	}

	public class StampEntry {

		public DualGUIElement favorite;
		public GUIElementButton delete;
		public StampEntryWidget self;

		public boolean addedWidgets = false;

		public void addAllWidgets() {
            StampBagScreen.this.addWidget(this.favorite);
            StampBagScreen.this.addWidget(this.delete);
            StampBagScreen.this.addWidget(this.self);
		}
	}
}
