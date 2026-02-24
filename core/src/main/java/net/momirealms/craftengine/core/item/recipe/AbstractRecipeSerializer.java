package net.momirealms.craftengine.core.item.recipe;

import net.momirealms.craftengine.core.item.*;
import net.momirealms.craftengine.core.item.recipe.reader.*;
import net.momirealms.craftengine.core.item.recipe.result.CustomRecipeResult;
import net.momirealms.craftengine.core.item.recipe.result.PostProcessor;
import net.momirealms.craftengine.core.item.recipe.result.PostProcessors;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.plugin.context.CommonConditions;
import net.momirealms.craftengine.core.plugin.context.Condition;
import net.momirealms.craftengine.core.plugin.context.Context;
import net.momirealms.craftengine.core.plugin.context.condition.AllOfCondition;
import net.momirealms.craftengine.core.plugin.locale.LocalizedResourceConfigException;
import net.momirealms.craftengine.core.util.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public abstract class AbstractRecipeSerializer<T, R extends Recipe<T>> implements RecipeSerializer<T, R> {
    protected static final VanillaRecipeReader VANILLA_RECIPE_HELPER =
            VersionHelper.isOrAbove26_1() ?
            new VanillaRecipeReader26_1() :
            VersionHelper.isOrAbove1_21_2() ?
            new VanillaRecipeReader1_21_2() :
            VersionHelper.isOrAbove1_20_5() ?
            new VanillaRecipeReader1_20_5() :
            new VanillaRecipeReader1_20();

    @SuppressWarnings("unchecked")
    protected CustomRecipeResult<T> parseResult(DatapackRecipeResult recipeResult) {
        Item<T> result = (Item<T>) CraftEngine.instance().itemManager().build(recipeResult);
        return new CustomRecipeResult<>(CloneableConstantItem.of(result), recipeResult.count(), null);
    }

    @Nullable
    protected Ingredient<T> toIngredient(List<String> items) {
        return toIngredient(items, 1);
    }

    @Nullable
    protected Ingredient<T> toIngredient(List<String> items, int count) {
        Set<UniqueKey> itemIds = new HashSet<>();
        Set<UniqueKey> minecraftItemIds = new HashSet<>();
        ItemManager<T> itemManager = CraftEngine.instance().itemManager();
        List<IngredientElement> elements = new ArrayList<>();
        for (String item : items) {
            if (item.charAt(0) == '#') {
                Key tag = Key.of(item.substring(1));
                elements.add(new IngredientElement.Tag(tag));
                List<UniqueKey> uniqueKeys = itemManager.itemIdsByTag(tag);
                if (uniqueKeys.isEmpty()) {
                    throw new LocalizedResourceConfigException("warning.config.recipe.invalid_ingredient", item);
                }
                itemIds.addAll(uniqueKeys);
                for (UniqueKey uniqueKey : uniqueKeys) {
                    List<UniqueKey> ingredientSubstitutes = itemManager.getIngredientSubstitutes(uniqueKey.key());
                    if (!ingredientSubstitutes.isEmpty()) {
                        itemIds.addAll(ingredientSubstitutes);
                    }
                }
            } else {
                Key itemId = Key.of(item);
                elements.add(new IngredientElement.Item(itemId));
                if (itemManager.getBuildableItem(itemId).isEmpty()) {
                    throw new LocalizedResourceConfigException("warning.config.recipe.invalid_ingredient", item);
                }
                itemIds.add(UniqueKey.create(itemId));
                List<UniqueKey> ingredientSubstitutes = itemManager.getIngredientSubstitutes(itemId);
                if (!ingredientSubstitutes.isEmpty()) {
                    itemIds.addAll(ingredientSubstitutes);
                }
            }
        }
        boolean hasCustomItem = false;
        for (UniqueKey holder : itemIds) {
            Optional<CustomItem<T>> optionalCustomItem = itemManager.getCustomItem(holder.key());
            UniqueKey vanillaItem;
            if (optionalCustomItem.isPresent()) {
                CustomItem<T> customItem = optionalCustomItem.get();
                if (customItem.isVanillaItem()) {
                    vanillaItem = holder;
                } else {
                    vanillaItem = UniqueKey.create(customItem.material());
                    hasCustomItem = true;
                }
            } else {
                if (itemManager.isVanillaItem(holder.key())) {
                    vanillaItem = holder;
                } else {
                    throw new LocalizedResourceConfigException("warning.config.recipe.invalid_ingredient", holder.key().asString());
                }
            }
            if (vanillaItem == UniqueKey.AIR) {
                throw new LocalizedResourceConfigException("warning.config.recipe.invalid_ingredient", holder.key().asString());
            }
            minecraftItemIds.add(vanillaItem);
        }
        if (itemIds.isEmpty()) {
            return null;
        }
        return Ingredient.of(elements, itemIds, minecraftItemIds, hasCustomItem, count);
    }
}
