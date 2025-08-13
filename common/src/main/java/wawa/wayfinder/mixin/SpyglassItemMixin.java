package wawa.wayfinder.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpyglassItem;
import org.spongepowered.asm.mixin.Mixin;
import wawa.wayfinder.Helper;
import wawa.wayfinder.WayfinderClient;
import wawa.wayfinder.data.SpyglassPins;

@Mixin(SpyglassItem.class)
public abstract class SpyglassItemMixin extends Item {
    public SpyglassItemMixin(final Properties properties) {
        super(properties);
    }

    @Override
    public boolean isBarVisible(final ItemStack stack) {
        return !WayfinderClient.PAGE_MANAGER.getSpyglassPins().getPins().isEmpty() || Helper.isUsingSpyglass(Minecraft.getInstance().player);
    }

    @Override
    public int getBarColor(final ItemStack stack) {
        return 0xCFA0F3;
    }

    @Override
    public int getBarWidth(final ItemStack stack) {
        return 13 - (int)((float)WayfinderClient.PAGE_MANAGER.getSpyglassPins().getPins().size() / SpyglassPins.MAX_PINS * 13F);
    }
}
