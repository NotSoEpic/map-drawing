package wawa.wayfinder.map;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import wawa.wayfinder.WayfinderClient;
import wawa.wayfinder.gui.GUIElementAtlases;
import wawa.wayfinder.map.stamp_bag.StampInformation;
import wawa.wayfinder.map.stamp_bag.widgets.DualGUIElement;
import wawa.wayfinder.map.stamp_bag.widgets.GUIElementButton;
import wawa.wayfinder.map.stamp_bag.widgets.StampEntryWidget;
import wawa.wayfinder.map.tool.StampBagDebuggerTool;

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
	private int browseBGWidth = 178;
	private int browseBGY = 100;
	private int browseBGHeight = 138;

	public StampBagScreen() {
		saveText = new EditBox(Minecraft.getInstance().font, 72, 17, Component.empty());
		saveText.setFocused(false);
		saveText.setCanLoseFocus(true);
		saveText.setMaxLength(72);

		createSaveWidgets();
		createEntryWidgets();

		up = new GUIElementButton(0, 0, 16, GUIElementAtlases.STAMP_BAG_BROWSE_UP, b -> {
			int maxPageCount;
			if (usingSearch) {
				maxPageCount = requestedInfo.size();
			} else if (usingFavorites) {
				maxPageCount = WayfinderClient.STAMP_HANDLER.getTotalEntryCount(true);
			} else {
				maxPageCount = WayfinderClient.STAMP_HANDLER.getTotalEntryCount();
			}

			page = (int) Math.clamp(page - 1, 1, Math.ceil(maxPageCount / 3f));
			refreshStamps();
		});

		down = new GUIElementButton(0, 0, 16, GUIElementAtlases.STAMP_BAG_BROWSE_DOWN, b -> {
			int maxPageCount;
			if (usingSearch) {
				maxPageCount = requestedInfo.size();
			} else if (usingFavorites) {
				maxPageCount = WayfinderClient.STAMP_HANDLER.getTotalEntryCount(true);
			} else {
				maxPageCount = WayfinderClient.STAMP_HANDLER.getTotalEntryCount();
			}

			page = (int) Math.clamp(page + 1, 1, Math.ceil(maxPageCount / 3f));
			refreshStamps();
		});

		favorites = new DualGUIElement(0, 0, 16, GUIElementAtlases.STAMP_BAG_BROWSE_UNFAVORITE, GUIElementAtlases.STAMP_BAG_BROWSE_FAVORITE, b -> {
			usingFavorites ^= true;
			page = 1;

			((DualGUIElement) b).imageSwitch = usingFavorites;

			refreshStamps();
		});

		searchWidget = new EditBox(Minecraft.getInstance().font, GUIElementAtlases.STAMP_BAG_BROWSE_ENTRY.width() - 1, 16, Component.empty());
		searchWidget.setResponder(/*why not just call it a callback...*/ s -> {
			usingSearch = !s.isEmpty();
			if (!s.isEmpty()) {
				page = 1;
			}
			refreshStamps();
		});
		searchWidget.setFocused(false);
		searchWidget.setCanLoseFocus(true);
		searchWidget.setMaxLength(72);
	}

	private void createSaveWidgets() {
		confirmSave = new GUIElementButton(0, 0, 16, GUIElementAtlases.STAMP_BAG_SAVE_CONFIRM, (b) -> {
			if (!saveText.getValue().isEmpty() && mapScreen != null) {
				NativeImage clipboard = mapScreen.toolPicker.getImageFromScissorTool();
				if (clipboard != null) {
					NativeImage copied = new NativeImage(clipboard.getWidth(), clipboard.getHeight(), false);
					copied.copyFrom(clipboard);

					WayfinderClient.STAMP_HANDLER.addNewStamp(copied, saveText.getValue());
					saveText.setValue("");
				}
			}

			changeStage(ScreenState.IDLE);
		});

		cancelSave = new GUIElementButton(0, 0, 16, GUIElementAtlases.STAMP_BAG_SAVE_CANCEL, (b) -> {
			saveText.setValue("");
			changeStage(ScreenState.IDLE);
		});

		allWidgets.addAll(List.of(saveText, confirmSave, cancelSave));
	}

	private void createEntryWidgets() {
		for (int i = 0; i < entries.length; i++) {
			entries[i] = new StampEntry();
			StampEntry entry = entries[i];

			entry.self = new StampEntryWidget(0, 0, this, (wid) -> {
//				if (wid.stampInformation != null && wid.stampInformation.getTextureManager().getTexture() != null) {
//                    CopyTool.INSTANCE.clipboard = wid.stampInformation.getTextureManager().getTexture();
//				}
			}, WayfinderClient.id("stamp_widget_" + i));

			entry.delete = new GUIElementButton(0, 0, 16, GUIElementAtlases.STAMP_BAG_BROWSE_TRASH, (b) -> {
				StampInformation si = entry.self.stampInformation;
				if (si != null) {
					WayfinderClient.STAMP_HANDLER.removeStamp(si);
				}

				refreshStamps();
			});

			entry.favorite = new DualGUIElement(0, 0, 16, GUIElementAtlases.STAMP_BAG_BROWSE_UNFAVORITE, GUIElementAtlases.STAMP_BAG_BROWSE_FAVORITE, (b) -> {
				StampInformation si = entry.self.stampInformation;
				if (si != null) {
					si.setFavorited(!si.isFavorited());
					((DualGUIElement) b).imageSwitch = si.isFavorited();
					WayfinderClient.STAMP_HANDLER.setDirty();
				}
			});
		}
	}

	public void setMapScreen(MapScreen screen) {
		this.mapScreen = screen;
	}

	public void resetWidgetInfo() {
		allWidgets.forEach(w -> {
			w.active = false;
			w.setFocused(false);
		});

		//saving
		int stampBagX = mapScreen.stampBag.getX();
		int stampBagY = mapScreen.stampBag.getY();

		setWidgetXY(saveText, stampBagX - 123, stampBagY - 1);
		setWidgetXY(confirmSave, stampBagX - 49, stampBagY);
		setWidgetXY(cancelSave, stampBagX - 28, stampBagY);
		//

		//browsing
		int backgroundX = (this.mapScreen.width - 15 - 16 / 2) - (148 + 16) - 21;
		int backgroundY = 100;

		for (int i = 0; i < entries.length; i++) {
			int entryY = backgroundY + 26 + (i * 37);
			StampEntry entry = entries[i];
			setWidgetXY(entry.self, backgroundX + 10, entryY);
			setWidgetXY(entry.favorite, backgroundX + GUIElementAtlases.STAMP_BAG_BROWSE_ENTRY.width() + 9, entryY);
			setWidgetXY(entry.delete, backgroundX + GUIElementAtlases.STAMP_BAG_BROWSE_ENTRY.width() + 9, entryY + 16);
		}

		setWidgetXY(up, backgroundX + GUIElementAtlases.STAMP_BAG_BROWSE_ENTRY.width() + 28, backgroundY + 26);
		setWidgetXY(down, backgroundX + GUIElementAtlases.STAMP_BAG_BROWSE_ENTRY.width() + 28, backgroundY + 5 + (3 * 37));
		setWidgetXY(favorites, backgroundX + GUIElementAtlases.STAMP_BAG_BROWSE_ENTRY.width() + 9, backgroundY + 5);
		setWidgetXY(searchWidget, backgroundX + 10, backgroundY + 5);
		//
	}

	private static void setWidgetXY(AbstractWidget w, int x, int y) {
		w.setX(x);
		w.setY(y);
	}

	public void changeStage(ScreenState newStage) {
		for (AbstractWidget w : activeWidgets) {
			//TODO: properly fix this
			mapScreen.removeWidget(w);
			w.setFocused(false);
			w.active = false;
		}
		resetWidgetInfo();
		activeWidgets.clear();

		if (state == ScreenState.BROWSING) {
			for (StampInformation si : requestedInfo) {
				si.getTextureManager().removeUser();
			}
		}

		state = newStage;
		switch (newStage) {
			case SAVING -> {
				addWidget(saveText);
				addWidget(confirmSave);
				addWidget(cancelSave);

				//we can comment this out if we don't want this behaviour when saving a stamp!
				setMapScreenWidgetActivity(false);
			}

			case BROWSING -> {
				if (refreshStamps()) {
					return;
				}

				addWidget(up);
				addWidget(down);
				addWidget(searchWidget);
				addWidget(favorites);
			}

			case IDLE -> setMapScreenWidgetActivity(true);
		}
	}

	private boolean refreshStamps() {
		for (StampInformation si : requestedInfo) {
			si.getTextureManager().removeUser();
		}
		requestedInfo.clear();

		int pageLoc = 3 * page;
		if (usingSearch) {
			WayfinderClient.STAMP_HANDLER.requestStampContaining(requestedInfo, searchWidget.getValue(), usingFavorites);
		} else {
			WayfinderClient.STAMP_HANDLER.bulkRequestStamps(requestedInfo, usingFavorites, pageLoc - 3, pageLoc - 2, pageLoc - 1);
		}

		for (int i = 0; i < 3; i++) {
			int index = i;
			if (usingSearch) {
				index = pageLoc - (3 - i);
			}

			StampEntry entry = entries[i];

			if (!requestedInfo.isEmpty() && index >= 0 && index <= requestedInfo.size() - 1) {
				entry.self.changeStampInformation(requestedInfo.get(index));
				addWidget(entry.self);
				addWidget(entry.delete);
				addWidget(entry.favorite);
				entry.addedWidgets = true;
			} else {
				removeWidget(entry.self);
				removeWidget(entry.delete);
				removeWidget(entry.favorite);

				entry.self.changeStampInformation(null);
				entry.addedWidgets = false;
			}
		}

		for (StampInformation si : requestedInfo) {
			si.getTextureManager().addUser();
		}

		return false;
	}

	private void setMapScreenWidgetActivity(boolean active) {
		for (AbstractWidget w : mapScreen.allWidgets) {
			w.active = active;
		}

		//always keep the stamp bag widget active
		mapScreen.stampBag.active = true;
	}

	public void addWidget(AbstractWidget wid) {
		wid.active = true;
		mapScreen.addWidget(wid);
		activeWidgets.add(wid);
	}

	public void removeWidget(AbstractWidget wid) {
		mapScreen.removeWidget(wid);
		activeWidgets.remove(wid);
		wid.setFocused(false);
		wid.active = false;
	}

	public void renderScreen(final GuiGraphics guiGraphics, final int mouseX, final int mouseY, final float partialTick) {
		PoseStack ps = guiGraphics.pose();

		ps.pushPose();

		ps.pushPose();
		renderBackground(guiGraphics, mouseX, mouseY, partialTick);
		ps.popPose();

		ps.pushPose();
		for (AbstractWidget w : activeWidgets) {
			ps.pushPose();
			w.render(guiGraphics, mouseX, mouseY, partialTick);
			ps.popPose();
		}
		ps.popPose();
	}

	public void renderBackground(final GuiGraphics guiGraphics, final int mouseX, final int mouseY, final float partialTick) {
		switch (state) {
			case SAVING -> {
				GUIElementAtlases background = GUIElementAtlases.STAMP_BAG_SAVE;
				int x = mapScreen.stampBag.getX() - background.width();
				int y = mapScreen.stampBag.getY() - 5;
				background.render(guiGraphics, x, y);
			}

			case BROWSING -> {
				int browseBGX = (this.mapScreen.width - 15 - 16 / 2) - (164) - 21;
				guiGraphics.blitSprite(StampBagDebuggerTool.backgroundID, browseBGX, browseBGY, browseBGWidth, browseBGHeight);

				for (int i = 0; i < entries.length; i++) {
					if (entries[i].addedWidgets) {
						GUIElementAtlases.STAMP_BAG_BROWSE_ENTRY.render(guiGraphics, browseBGX + 10, browseBGY + 26 + (i * 37));
					}

					int maxPageCount;
					if (usingSearch) {
						maxPageCount = requestedInfo.size();
					} else if (usingFavorites) {
						maxPageCount = WayfinderClient.STAMP_HANDLER.getTotalEntryCount(true);
					} else {
						maxPageCount = WayfinderClient.STAMP_HANDLER.getTotalEntryCount();
					}

					guiGraphics.drawString(Minecraft.getInstance().font, "%s/%s".formatted(maxPageCount == 0 ? 0 : page, (int) Math.ceil(maxPageCount / 3f)), browseBGX + GUIElementAtlases.STAMP_BAG_BROWSE_ENTRY.width() + 27, browseBGY + 74, Color.WHITE.getRGB(), true);
				}
			}
		}
	}

	public void mouseClicked(double mouseX, double mouseY, int button) {
		if (state != ScreenState.IDLE && state != ScreenState.SAVING) {
			int browseBGX = (this.mapScreen.width - 15 - 16 / 2) - (164) - 21;
			if ((mouseX < browseBGX || mouseX > browseBGX + browseBGWidth || mouseY < browseBGY || mouseY > browseBGY + browseBGHeight) && !this.mapScreen.stampBag.isHovered()) {
				changeStage(ScreenState.IDLE);
			}
		}
	}

	public void parentClose() {
		for (AbstractWidget w : activeWidgets) {
			w.setFocused(false);
			mapScreen.removeWidget(w);
		}

		mapScreen = null;
	}

	public ScreenState getState() {
		return state;
	}

	public boolean hasAnyTextBoxFoxused() {
		return saveText.isFocused() || searchWidget.isFocused();
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
			addWidget(favorite);
			addWidget(delete);
			addWidget(self);
		}
	}
}
