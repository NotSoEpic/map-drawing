package wawa.wayfinder.map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;
import wawa.wayfinder.WayfinderClient;
import wawa.wayfinder.gui.GUIElementAtlases;
import wawa.wayfinder.map.stamp_bag.widgets.SavetextWidget;
import wawa.wayfinder.map.stamp_bag.widgets.GUIElementButton;

import java.util.ArrayList;
import java.util.List;

public class StampBagScreen {

    public static final StampBagScreen INSTANCE = new StampBagScreen();

    public MapScreen mapScreen;
    private ScreenState state = ScreenState.IDLE;
    private List<AbstractWidget> allWidgets = new ArrayList<>();
    public List<AbstractWidget> activeWidgets = new ArrayList<>();

    /*Widgets*/
    //saving
    public SavetextWidget saveText;
    public GUIElementButton confirmSave;
    public GUIElementButton cancelSave;
    //

    public GUIElementButton nextPage;
    public GUIElementButton previousPage;
    public GUIElementButton favorites;
    public GUIElementButton exit;

    public StampEntry[] entries = new StampEntry[6];

//    public List<StampEntry> entries = new ArrayList<>();


    public StampBagScreen() {
        saveText = new SavetextWidget(Minecraft.getInstance().font, 72, 17, Component.empty());
        saveText.setFocused(false);
        saveText.setCanLoseFocus(true);
        saveText.setMaxLength(72);

        addSaveWidgets();
    }

    private void addSaveWidgets() {
        confirmSave = new GUIElementButton(0, 0, 16, GUIElementAtlases.STAMP_BAG_SAVE_CONFIRM, (b) -> {
            if (!saveText.getValue().isEmpty() && mapScreen != null && mapScreen.toolPicker.getCopiedImage() != null) {
                WayfinderClient.STAMP_HANDLER.addNewStamp(mapScreen.toolPicker.getCopiedImage(), saveText.getValue());
                saveText.setValue("");
            }

            changeStage(ScreenState.IDLE);
        });

        cancelSave = new GUIElementButton(0, 0, 16, GUIElementAtlases.STAMP_BAG_SAVE_CANCEL, (b) -> {
            saveText.setValue("");
            changeStage(ScreenState.IDLE);
        });

        allWidgets.addAll(List.of(saveText, confirmSave, cancelSave));
    }

    public void setMapScreen(MapScreen screen) {
        this.mapScreen = screen;
    }

    public void changeStage(ScreenState newStage) {
        activeWidgets.forEach(w -> mapScreen.removeWidget(w));
        resetWidgetInfo();
        activeWidgets.clear();

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
                for (int i = 0; i < entries.length; i++) {
                    StampEntry entry = entries[i];


                }




                setMapScreenWidgetActivity(false);
            }

            case IDLE -> setMapScreenWidgetActivity(true);
        }
    }

    private void setMapScreenWidgetActivity(boolean active) {
        for (AbstractWidget w : mapScreen.allWidgets) {
            w.active = active;
        }
    }

    public void resetWidgetInfo() {
        int width = mapScreen.width;
        int height = mapScreen.height;

        allWidgets.forEach(w -> {
            w.active = false;
            w.setFocused(false);
        });

        //saving
        int stampBagX = mapScreen.stampBag.getX();
        int stampBagY = mapScreen.stampBag.getY();

        saveText.setX(stampBagX - 123);
        saveText.setY(stampBagY - 1);

        confirmSave.setX(stampBagX - 49);
        confirmSave.setY(stampBagY);

        cancelSave.setX(stampBagX - 28);
        cancelSave.setY(stampBagY);
        //
    }

    public void addWidget(AbstractWidget wid) {
        wid.active = true;
        mapScreen.addWidget(wid);
        activeWidgets.add(wid);
    }

    public void renderScreen(final GuiGraphics guiGraphics, final int mouseX, final int mouseY, final float partialTick) {
        renderBackground(guiGraphics, mouseX, mouseY, partialTick);

        for (AbstractWidget wid : activeWidgets) {
            wid.render(guiGraphics, mouseX, mouseY, partialTick);
        }
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
                guiGraphics.fillGradient(0, 0, mapScreen.width, mapScreen.height, 0, -1072689136, -804253680);



            }
        }
    }

    public void parentClose() {
        activeWidgets.forEach(w -> {
            w.setFocused(false);
            mapScreen.removeWidget(w);
        });

        mapScreen = null;
    }

    public ScreenState getState() {
        return state;
    }

    public boolean hasAnyTextBoxFoxused() {
        return saveText.isFocused();
    }

    public enum ScreenState {
        IDLE, SAVING, BROWSING, SEARCHING, FAVORITING
    }

    public class StampEntry {

        public GUIElementButton favorite;
        public GUIElementButton delete;



        public int index = -1;





        public void setIndex(int index) {
            this.index = index;
        }

        public void removeInformation() {
            index = -1;
        }


        /*widgets*/
        //favorite
        //delete
        //entire self (click compatability and rendering images)
    }
}
