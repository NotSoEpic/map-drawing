package wawa.wayfinder.map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;
import wawa.wayfinder.gui.GUIElementAtlases;
import wawa.wayfinder.map.stamp_bag.widgets.SavetextWidget;
import wawa.wayfinder.map.stamp_bag.widgets.abstract_classes.GUIElementButton;

import java.util.ArrayList;
import java.util.List;

public class StampBagScreen {

    public static final StampBagScreen INSTANCE = new StampBagScreen();

    public MapScreen mapScreen;
    private ScreenStage stage = ScreenStage.IDLE;
    private List<AbstractWidget> activeWidgets = new ArrayList<>();

    /*Widgets*/
    public SavetextWidget saveText;
    public GUIElementButton confirmSave;
    public GUIElementButton cancelSave;

    //close screen
    //textPrompt searching
    //favorites
    //
    //stamp entry array of 6
    //previous button
    //next button

    //x out of y pages at bottom center


    public StampBagScreen() {
        saveText = new SavetextWidget(Minecraft.getInstance().font, 32, 16, Component.empty());
        saveText.setFocused(false);
        saveText.setCanLoseFocus(true);
        saveText.setMaxLength(20);

        confirmSave = new GUIElementButton(0, 0, 16, GUIElementAtlases.STAMP_BAG_SAVE_CONFIRM, (b) -> {
            changeStage(ScreenStage.IDLE);
        });
        cancelSave = new GUIElementButton(0, 0, 16, GUIElementAtlases.STAMP_BAG_SAVE_CANCEL, (b) -> {
            changeStage(ScreenStage.IDLE);
        });
    }

    public void setMapScreen(MapScreen screen) {
        this.mapScreen = screen;
    }

    public void resetWidgets() {
        int width = mapScreen.width;
        int height = mapScreen.height;

        //saving
        saveText.setX(width / 2);
        saveText.setY(0);

        confirmSave.setX(0);
        confirmSave.setY(16);

        cancelSave.setX(16);
        cancelSave.setY(16);
        //
    }

    public void changeStage(ScreenStage newStage) {
        activeWidgets.forEach(w -> mapScreen.removeWidget(w));
        activeWidgets.clear();

        stage = newStage;
        switch (newStage) {
            case SAVING -> {
                addWidget(saveText);
                addWidget(confirmSave);
                addWidget(cancelSave);
            }
        }
    }

    public void addWidget(AbstractWidget wid) {
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
        switch (stage) {
            case SAVING -> {
                GUIElementAtlases.STAMP_BAG_BACKGROUND.render(guiGraphics, mouseX, mouseY);
            }
        }
    }

    public enum ScreenStage {
        IDLE, SAVING, BROWSING, SEARCHING, FAVORITING
    }

    public class StampEntry {
        /*widgets*/
        //favorite
        //delete
        //entire self (click compatability and rendering images)
    }
}
