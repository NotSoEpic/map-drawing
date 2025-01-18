package wawa.wayfinder.mapmanager.widgets;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.tooltip.TooltipRenderUtil;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector4i;
import wawa.wayfinder.AllComponents;
import wawa.wayfinder.AllItems;
import wawa.wayfinder.ClientStampTooltipComponent;
import wawa.wayfinder.RenderHelper;
import wawa.wayfinder.mapmanager.tools.StampTool;
import wawa.wayfinder.mapmanager.tools.Tool;
import wawa.wayfinder.rendering.WayfinderRenderTypes;
import wawa.wayfinder.stampitem.StampGroups;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class StampListWidget extends AbstractWidget {
    private final Map<ResourceLocation, List<ResourceLocation>> availableStampGroups;
    private final Map<ResourceLocation, Vector4i> stampBounds = new HashMap<>();
    private double scrollAmount = 0;
    private int fullWidth;
    private int fullHeight;
    private boolean menuEnabled = false;
    private ResourceLocation selectedGroup;
    private int selectedIndex;
    public StampListWidget() {
        super(25, 25, 0, 0, Component.literal("stamp list"));
        availableStampGroups = collectAvailableStampGroups(Minecraft.getInstance().player);
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.renderFakeItem(new ItemStack(AllItems.STAMP), 8, 8);
        if (menuEnabled) {
            TooltipRenderUtil.renderTooltipBackground(guiGraphics, getX(), getY(), width, height, 0);
            AtomicInteger y = new AtomicInteger((int) scrollAmount + getX());
            guiGraphics.enableScissor(getX(), getY(), getRight(), getBottom());
            fullWidth = 0;
            fullHeight = 0;
            availableStampGroups.keySet().stream().sorted().forEach(key -> {
                int x = getX();
                Component groupTitle = Component.translatable(key.toLanguageKey("stamp_group"));
                fullWidth = Math.max(fullWidth, Minecraft.getInstance().font.width(groupTitle));
                guiGraphics.drawString(Minecraft.getInstance().font, groupTitle,
                        x, y.get(), -1, true);
                for (ResourceLocation texture : availableStampGroups.get(key)) {
                    RenderHelper.renderTypeBlit(guiGraphics, WayfinderRenderTypes.getPaletteSwap(texture.withPath(ClientStampTooltipComponent::fromPathShorthand)),
                            x, y.get() + 10, 0,
                            0.0f, 0.0f,
                            16, 16, 16, 16
                    );
                    stampBounds.put(texture, new Vector4i(x, y.get() + 10, x + 16, y.get() + 10 + 16));
                    x += 18;
                    fullWidth = Math.max(fullWidth, x - getX());
                }
                y.addAndGet(28);
                fullHeight += 28;
            });
            RenderSystem.disableScissor();

            width = fullWidth;
            height = Math.min(fullHeight, 250);

            ResourceLocation texture = getClickedStamp(mouseX, mouseY);
            if (texture != null) {
                guiGraphics.drawString(Minecraft.getInstance().font, Component.translatable(texture.toLanguageKey("stamp")),
                        mouseX, mouseY, -1, true);
            }
        }
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return (menuEnabled && super.isMouseOver(mouseX, mouseY)) || (active && visible && isToggleButton(mouseX, mouseY));
    }

    private boolean isToggleButton(double mouseX, double mouseY) {
        return mouseX >= 8 && mouseX <= 24 && mouseY >= 8 && mouseY <= 24;
    }

    @Nullable
    private ResourceLocation getClickedStamp(double mouseX, double mouseY) {
        if (!menuEnabled || !isMouseOver(mouseX, mouseY))
            return null;
        for (Map.Entry<ResourceLocation, Vector4i> entry : stampBounds.entrySet()) {
            Vector4i b = entry.getValue();
            if (b.x <= mouseX && b.z >= mouseX && b.y <= mouseY && b.w >= mouseY) {
                return entry.getKey();
            }
        }
        return null;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        scrollAmount += scrollY * 20;
        if (fullHeight - height > 0)
            scrollAmount = Mth.clamp(scrollAmount, height - fullHeight, 0);
        else
            scrollAmount = 0;
        return true;
    }

    @Override
    protected boolean clicked(double mouseX, double mouseY) {
        return isMouseOver(mouseX, mouseY);
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        if (isToggleButton(mouseX, mouseY)) {
            menuEnabled = !menuEnabled;
            return;
        }

        ResourceLocation texture = getClickedStamp(mouseX, mouseY);
        if (texture != null) {
            Tool.set(new StampTool(texture.withPath(ClientStampTooltipComponent::fromPathShorthand)));
            selectedGroup = StampGroups.getGroup(texture);
            if (selectedGroup != null)
                selectedIndex = availableStampGroups.get(selectedGroup).indexOf(texture);
            menuEnabled = false;
        }
    }

    public static Map<ResourceLocation, List<ResourceLocation>> collectAvailableStampGroups(Player player) {
        boolean stampAllowed = !(Tool.get() instanceof StampTool);
        Map<ResourceLocation, List<ResourceLocation>> groups = new HashMap<>();
        for (ResourceLocation texture : collectAvailableStamps(player)) {
            ResourceLocation group = StampGroups.getGroup(texture);
            if (group == null)
                group = StampGroups.UNGROUPED;
            groups.computeIfAbsent(group, g -> new ArrayList<>()).add(texture);
            if (!stampAllowed && Tool.get() instanceof StampTool stampTool && stampTool.stamp.equals(texture.withPath(ClientStampTooltipComponent::fromPathShorthand)))
                stampAllowed = true;
        }
        if (!stampAllowed)
            Tool.set(null);
        groups.forEach((k, v) -> v.sort(ResourceLocation::compareTo));
        return groups;
    }

    private static Set<ResourceLocation> collectAvailableStamps(Player player) {
        Set<ResourceLocation> stamps = new HashSet<>();
        collectAvailableStamps(stamps, player.getInventory().items);
        return stamps;
    }

    private static void collectAvailableStamps(Set<ResourceLocation> stamps, Iterable<ItemStack> items) {
        items.forEach(i -> {
            if (i.has(AllComponents.STAMP)) {
                stamps.addAll(i.get(AllComponents.STAMP).textures());
            }
            if (i.has(DataComponents.BUNDLE_CONTENTS)) {
                collectAvailableStamps(stamps, i.get(DataComponents.BUNDLE_CONTENTS).items());
            }
            if (i.has(DataComponents.CONTAINER)) {
                collectAvailableStamps(stamps, i.get(DataComponents.CONTAINER).nonEmptyItems());
            }
        });
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}
}
