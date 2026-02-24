package net.momirealms.craftengine.core.item.recipe;

import com.google.gson.JsonObject;
import net.momirealms.craftengine.core.item.recipe.result.CustomRecipeResult;
import net.momirealms.craftengine.core.plugin.config.ConfigConstants;
import net.momirealms.craftengine.core.plugin.config.ConfigSection;
import net.momirealms.craftengine.core.plugin.config.ConfigValue;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;
import org.jetbrains.annotations.NotNull;

public final class CustomBlastingRecipe<T> extends CustomCookingRecipe<T> {
    public static final Serializer<?> SERIALIZER = new Serializer<>();

    public CustomBlastingRecipe(Key id,
                                boolean showNotification,
                                CustomRecipeResult<T> result,
                                String group,
                                CookingRecipeCategory category,
                                Ingredient<T> ingredient,
                                int cookingTime,
                                float experience) {
        super(id, showNotification, result, group, category, ingredient, cookingTime, experience);
    }

    @Override
    public @NotNull Key serializerType() {
        return RecipeSerializers.BLASTING;
    }

    @Override
    public RecipeType type() {
        return RecipeType.BLASTING;
    }

    public static class Serializer<A> extends AbstractRecipeSerializer<A, CustomBlastingRecipe<A>> {

        @SuppressWarnings({"unchecked", "rawtypes", "DuplicatedCode"})
        @Override
        public CustomBlastingRecipe<A> readConfig(Key id, ConfigSection section) {
            return new CustomBlastingRecipe(
                    id,
                    section.getBoolean(true, "show_notification", "show-notification"),
                    section.getNonNullValue(ConfigConstants.ARGUMENT_SECTION, "result").getAsCustomRecipeResult(),
                    section.getString("group"),
                    section.getEnum(null, CookingRecipeCategory.class, "category"),
                    section.getNonNullValue(ConfigConstants.ARGUMENT_LIST, "ingredient", "ingredients").getAsIngredient(),
                    section.getInt(80, "time"),
                    section.getFloat("exp", "experience")
            );
        }

        @Override
        public CustomBlastingRecipe<A> readJson(Key id, JsonObject json) {
            return new CustomBlastingRecipe<>(id,
                    true,
                    parseResult(VANILLA_RECIPE_HELPER.cookingResult(json.get("result"))), VANILLA_RECIPE_HELPER.readGroup(json), VANILLA_RECIPE_HELPER.cookingCategory(json),
                    toIngredient(VANILLA_RECIPE_HELPER.singleIngredient(json.get("ingredient"))),
                    VANILLA_RECIPE_HELPER.cookingTime(json),
                    VANILLA_RECIPE_HELPER.cookingExperience(json)
            );
        }
    }
}
