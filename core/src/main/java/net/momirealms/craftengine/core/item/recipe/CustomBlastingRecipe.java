package net.momirealms.craftengine.core.item.recipe;

import com.google.gson.JsonObject;
import net.momirealms.craftengine.core.item.recipe.result.CustomRecipeResult;
import net.momirealms.craftengine.core.plugin.config.ConfigConstants;
import net.momirealms.craftengine.core.plugin.config.ConfigSection;
import net.momirealms.craftengine.core.util.Key;
import org.jetbrains.annotations.NotNull;

public final class CustomBlastingRecipe extends CustomCookingRecipe {
    public static final Serializer SERIALIZER = new Serializer();

    public CustomBlastingRecipe(Key id,
                                boolean showNotification,
                                CustomRecipeResult result,
                                String group,
                                CookingRecipeCategory category,
                                Ingredient ingredient,
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

    public static class Serializer extends AbstractRecipeSerializer<CustomBlastingRecipe> {

        @SuppressWarnings("DuplicatedCode")
        @Override
        public CustomBlastingRecipe readConfig(Key id, ConfigSection section) {
            return new CustomBlastingRecipe(
                    id,
                    section.getBoolean(SHOW_NOTIFICATIONS, true),
                    super.parseResult(section.getNonNullSection("result")),
                    section.getString("group"),
                    section.getEnum("category", CookingRecipeCategory.class),
                    section.getNonNullValue(INGREDIENTS, ConfigConstants.ARGUMENT_LIST, super::parseIngredient),
                    section.getInt("time", 80),
                    section.getFloat(EXP)
            );
        }

        @Override
        public CustomBlastingRecipe readJson(Key id, JsonObject json) {
            return new CustomBlastingRecipe(id,
                    true,
                    parseResult(VANILLA_RECIPE_HELPER.cookingResult(json.get("result"))), VANILLA_RECIPE_HELPER.readGroup(json), VANILLA_RECIPE_HELPER.cookingCategory(json),
                    toIngredient(VANILLA_RECIPE_HELPER.singleIngredient(json.get("ingredient"))),
                    VANILLA_RECIPE_HELPER.cookingTime(json),
                    VANILLA_RECIPE_HELPER.cookingExperience(json)
            );
        }
    }
}
