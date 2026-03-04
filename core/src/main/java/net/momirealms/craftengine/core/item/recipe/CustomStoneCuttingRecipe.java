package net.momirealms.craftengine.core.item.recipe;

import com.google.gson.JsonObject;
import net.momirealms.craftengine.core.item.recipe.input.RecipeInput;
import net.momirealms.craftengine.core.item.recipe.input.SingleItemInput;
import net.momirealms.craftengine.core.item.recipe.result.CustomRecipeResult;
import net.momirealms.craftengine.core.plugin.config.ConfigConstants;
import net.momirealms.craftengine.core.plugin.config.ConfigSection;
import net.momirealms.craftengine.core.util.Key;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class CustomStoneCuttingRecipe<T> extends AbstractGroupedRecipe<T> {
    public static final Serializer<?> SERIALIZER = new Serializer<>();
    private final Ingredient<T> ingredient;

    public CustomStoneCuttingRecipe(Key id,
                                    boolean showNotification,
                                    CustomRecipeResult<T> result,
                                    String group,
                                    Ingredient<T> ingredient) {
        super(id, showNotification, result, group);
        this.ingredient = ingredient;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean matches(RecipeInput input) {
        return this.ingredient.test(((SingleItemInput<T>) input).input());
    }

    @Override
    public List<Ingredient<T>> ingredientsInUse() {
        return List.of(this.ingredient);
    }

    @Override
    public @NotNull Key serializerType() {
        return RecipeSerializers.STONECUTTING;
    }

    @Override
    public RecipeType type() {
        return RecipeType.STONECUTTING;
    }

    public Ingredient<T> ingredient() {
        return this.ingredient;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void takeInput(@NotNull RecipeInput in, int ignore) {
        SingleItemInput<T> input = (SingleItemInput<T>) in;
        takeIngredient(this.ingredient, input.input().item(), ignore);
    }

    public static class Serializer<A> extends AbstractRecipeSerializer<A, CustomStoneCuttingRecipe<A>> {

        @SuppressWarnings({"DuplicatedCode"})
        @Override
        public CustomStoneCuttingRecipe<A> readConfig(Key id, ConfigSection section) {
            return new CustomStoneCuttingRecipe<>(id,
                    section.getBoolean(SHOW_NOTIFICATIONS, true),
                    super.parseResult(section.getNonNullSection("result")),
                    section.getString("group"),
                    section.getNonNullValue(INGREDIENTS, ConfigConstants.ARGUMENT_LIST, super::parseIngredient)
            );
        }

        @Override
        public CustomStoneCuttingRecipe<A> readJson(Key id, JsonObject json) {
            String group = VANILLA_RECIPE_HELPER.readGroup(json);
            return new CustomStoneCuttingRecipe<>(id,
                    true,
                    parseResult(VANILLA_RECIPE_HELPER.stoneCuttingResult(json)), group,
                    toIngredient(VANILLA_RECIPE_HELPER.singleIngredient(json.get("ingredient")))
            );
        }
    }
}
