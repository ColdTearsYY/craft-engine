package net.momirealms.craftengine.core.item.recipe.remainder;

import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.plugin.config.ConfigSection;
import net.momirealms.craftengine.core.plugin.config.ConfigValue;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.MiscUtils;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class RecipeBasedCraftRemainder implements CraftRemainder {
    public static final CraftRemainderFactory<RecipeBasedCraftRemainder> FACTORY = new Factory();
    private final Map<Key, CraftRemainder> remainders;
    @Nullable
    private final CraftRemainder fallback;

    public RecipeBasedCraftRemainder(Map<Key, CraftRemainder> remainders, @Nullable CraftRemainder fallback) {
        this.remainders = remainders;
        this.fallback = fallback;
    }

    @Override
    public <T> Item<T> remainder(Key recipeId, Item<T> item) {
        CraftRemainder remainder = this.remainders.get(recipeId);
        if (remainder != null) {
            return remainder.remainder(recipeId, item);
        }
        return this.fallback != null ? this.fallback.remainder(recipeId, item) : null;
    }

    private static class Factory implements CraftRemainderFactory<RecipeBasedCraftRemainder> {

        @Override
        public RecipeBasedCraftRemainder create(ConfigSection section) {
            Map<Key, CraftRemainder> remainders = new HashMap<>();
            List<GroupedRemainder> remainderList = section.parseNonEmptyList(v -> {
                ConfigSection termSection = v.getAsSection();
                List<Key> recipes = termSection.parseNonEmptyList(ConfigValue::getAsIdentifier, "recipes");
                CraftRemainder remainder = termSection.getValue(ConfigValue::getAsCraftRemainder, "craft_remainder", "craft_remaining_item", "craft-remainder", "craft-remaining-item");
                return new GroupedRemainder(recipes, remainder);
            }, "terms");
            for (GroupedRemainder remainder : remainderList) {
                for (Key recipeId : remainder.recipes) {
                    remainders.put(recipeId, remainder.remainder());
                }
            }
            return new RecipeBasedCraftRemainder(remainders, section.getValue(ConfigValue::getAsCraftRemainder, "fallback"));
        }

        public record GroupedRemainder(List<Key> recipes, CraftRemainder remainder) {
        }
    }
}
