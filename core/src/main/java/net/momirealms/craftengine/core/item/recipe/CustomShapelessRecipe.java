package net.momirealms.craftengine.core.item.recipe;

import com.google.gson.JsonObject;
import net.momirealms.craftengine.core.item.recipe.input.CraftingInput;
import net.momirealms.craftengine.core.item.recipe.input.RecipeInput;
import net.momirealms.craftengine.core.item.recipe.result.CustomRecipeResult;
import net.momirealms.craftengine.core.plugin.config.ConfigConstants;
import net.momirealms.craftengine.core.plugin.config.ConfigSection;
import net.momirealms.craftengine.core.plugin.config.ConfigValue;
import net.momirealms.craftengine.core.plugin.context.CommonConditions;
import net.momirealms.craftengine.core.plugin.context.CommonFunctions;
import net.momirealms.craftengine.core.plugin.context.Condition;
import net.momirealms.craftengine.core.plugin.context.Context;
import net.momirealms.craftengine.core.plugin.context.function.Function;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.MiscUtils;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public final class CustomShapelessRecipe<T> extends CustomCraftingTableRecipe<T> {
    public static final Serializer<?> SERIALIZER = new Serializer<>();
    private final List<Ingredient<T>> ingredients;
    private final PlacementInfo<T> placementInfo;
    private final boolean ingredientCountSupport;

    public CustomShapelessRecipe(Key id,
                                 boolean showNotification,
                                 CustomRecipeResult<T> result,
                                 CustomRecipeResult<T> visualResult,
                                 String group,
                                 CraftingRecipeCategory category,
                                 List<Ingredient<T>> ingredients,
                                 Function<Context>[] craftingFunctions,
                                 Predicate<Context> craftingCondition,
                                 boolean alwaysRebuildOutput,
                                 boolean ingredientCountSupport) {
        super(id, showNotification, result, visualResult, group, category, craftingFunctions, craftingCondition, alwaysRebuildOutput);
        this.ingredients = ingredients;
        this.placementInfo = PlacementInfo.create(ingredients);
        this.ingredientCountSupport = ingredientCountSupport;
    }

    public PlacementInfo<T> placementInfo() {
        return this.placementInfo;
    }

    @Override
    public List<Ingredient<T>> ingredientsInUse() {
        return this.ingredients;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean matches(RecipeInput input) {
        return matches((CraftingInput<T>) input);
    }

    private boolean matches(CraftingInput<T> input) {
        if (input.ingredientCount() != this.ingredients.size()) {
            return false;
        }
        if (input.size() == 1 && this.ingredients.size() == 1) {
            return this.ingredients.getFirst().test(input.getItem(0));
        }
        return input.finder().canCraft(this);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void takeInput(@NotNull RecipeInput in, int ignore) {
        CraftingInput<T> input = (CraftingInput<T>) in;
        if (input.ingredientCount() != this.ingredients.size()) {
            return;
        }
        if (input.size() == 1 && this.ingredients.size() == 1) {
            UniqueIdItem<T> item = input.getItem(0);
            Ingredient<T> first = this.ingredients.getFirst();
            item.item().shrink(first.count() - ignore);
            return;
        }
        List<UniqueIdItem<T>> inputItems = new ArrayList<>(input.items);
        // 数量多的排前面
        inputItems.sort((o1, o2) -> Integer.compare(o2.item().count(), o1.item().count()));
        boolean[] taken = new boolean[inputItems.size()];
        outer:
        for (Ingredient<T> ingredient : this.ingredients) {
            for (int j = 0; j < taken.length; j++) {
                if (!taken[j]) {
                    UniqueIdItem<T> inputItem = inputItems.get(j);
                    if (ingredient.test(inputItem)) {
                        int toShrink = ingredient.count() - ignore;
                        if (toShrink > 0) {
                            inputItem.item().shrink(toShrink);
                        }
                        taken[j] = true;
                        continue outer;
                    }
                }
            }
        }
    }

    public boolean ingredientCountSupport() {
        return this.ingredientCountSupport;
    }

    @Override
    public @NotNull Key serializerType() {
        return RecipeSerializers.SHAPELESS;
    }

    public static class Serializer<A> extends AbstractRecipeSerializer<A, CustomShapelessRecipe<A>> {

        @SuppressWarnings({"unchecked", "rawtypes", "DuplicatedCode"})
        @Override
        public CustomShapelessRecipe<A> readConfig(Key id, ConfigSection section) {
            List<Ingredient<A>> ingredients;
            boolean hasAdditionalInput = false;
            ConfigValue ingredientsValue = section.getNonNullValue(ConfigConstants.ARGUMENT_LIST, "ingredients", "ingredient");
            if (ingredientsValue.is(Map.class)) {
                ingredients = new ArrayList<>();
                ConfigSection ingredientSection = ingredientsValue.getAsSection();
                for (String key : ingredientSection.keySet()) {
                    Ingredient<A> value = ingredientSection.getNonNullValue(ConfigConstants.ARGUMENT_LIST, key).getAsIngredient();
                    ingredients.add(value);
                    if (value.count() > 1) {
                        hasAdditionalInput = true;
                    }
                }
            } else if (ingredientsValue.is(List.class)) {
                ingredients = ingredientsValue.parseAsList(ConfigValue::getAsIngredient);
                for (Ingredient<A> ingredient : ingredients) {
                    if (ingredient.count() > 1) {
                        hasAdditionalInput = true;
                        break;
                    }
                }
            } else {
                Ingredient<A> ingredient = ingredientsValue.getAsIngredient();
                ingredients = List.of(ingredient);
                if (ingredient.count() > 1) {
                    hasAdditionalInput = true;
                }
            }
            // 按照数量从多到少排序
            if (hasAdditionalInput) {
                ingredients.sort((o1, o2) -> Integer.compare(o2.count(), o1.count()));
            }
            return new CustomShapelessRecipe(
                    id,
                    section.getBoolean(true, "show_notification", "show-notification"),
                    section.getNonNullValue(ConfigConstants.ARGUMENT_SECTION, "result").getAsCustomRecipeResult(),
                    section.getNonNullValue(ConfigConstants.ARGUMENT_SECTION, "visual_result", "visual-result").getAsCustomRecipeResult(),
                    section.getString("group"),
                    section.getEnum(null, CraftingRecipeCategory.class, "category"),
                    ingredients,
                    section.parseSectionList(CommonFunctions::fromConfig, "functions", "function").toArray(new Function[0]),
                    MiscUtils.allOf(section.parseSectionList(CommonConditions::fromConfig, "conditions", "condition")),
                    section.getBoolean(true, "always_rebuild_result", "always-rebuild-result"),
                    hasAdditionalInput
            );
        }

        @Override
        public CustomShapelessRecipe<A> readJson(Key id, JsonObject json) {
            return new CustomShapelessRecipe<>(
                    id,
                    true,
                    parseResult(VANILLA_RECIPE_HELPER.craftingResult(json.get("result"))),
                    null,
                    VANILLA_RECIPE_HELPER.readGroup(json), VANILLA_RECIPE_HELPER.craftingCategory(json),
                    VANILLA_RECIPE_HELPER.shapelessIngredients(json.getAsJsonArray("ingredients")).stream().map(this::toIngredient).toList(),
                    null,
                    null,
                    false,
                    false
            );
        }
    }
}
