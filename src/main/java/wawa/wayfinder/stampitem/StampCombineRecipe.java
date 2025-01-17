package wawa.wayfinder.stampitem;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import wawa.wayfinder.AllComponents;
import wawa.wayfinder.AllItems;
import wawa.wayfinder.Wayfinder;

import java.util.HashSet;
import java.util.Set;

public class StampCombineRecipe extends CustomRecipe {
    public StampCombineRecipe(CraftingBookCategory category) {
        super(category);
    }

    @Nullable
    private Set<ResourceLocation> combineTextures(CraftingInput input) {
        if (input.size() <= 1)
            return null;
        Set<ResourceLocation> combined = new HashSet<>();
        ResourceLocation group = null;
        for (ItemStack stack : input.items()) {
            StampComponent component = stack.get(AllComponents.STAMP);
            if (!stack.is(AllItems.STAMP) || component == null)
                return null;
            if (!combined.addAll(component.textures()))
                return null;
            if (group == null)
                group = component.getGroup();
            if (group == null)
                return null;
        }
        ResourceLocation finalGroup = group;
        if (combined.stream().allMatch(r -> finalGroup.equals(StampGroups.getGroup(r))))
            return combined;
        return null;
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        return combineTextures(input) != null;
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        ItemStack stack = new ItemStack(AllItems.STAMP);
        stack.set(AllComponents.STAMP, new StampComponent(combineTextures(input).stream().toList()));
        return stack;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    public static final ResourceLocation ID = Wayfinder.id("crafting_special_stamp_combine");
    private static final RecipeSerializer<StampCombineRecipe> COMBINE_STAMP = Registry.register(BuiltInRegistries.RECIPE_SERIALIZER,
            ID, new SimpleCraftingRecipeSerializer<>(StampCombineRecipe::new));
    @Override
    public RecipeSerializer<?> getSerializer() {
        return COMBINE_STAMP;
    }

    public static void init() {}
}
