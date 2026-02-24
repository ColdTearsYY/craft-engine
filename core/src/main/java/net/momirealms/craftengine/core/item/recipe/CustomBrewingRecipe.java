package net.momirealms.craftengine.core.item.recipe;

import com.google.gson.JsonObject;
import net.momirealms.craftengine.core.item.recipe.input.BrewingInput;
import net.momirealms.craftengine.core.item.recipe.input.RecipeInput;
import net.momirealms.craftengine.core.item.recipe.result.CustomRecipeResult;
import net.momirealms.craftengine.core.plugin.config.ConfigConstants;
import net.momirealms.craftengine.core.plugin.config.ConfigSection;
import net.momirealms.craftengine.core.util.Key;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public final class CustomBrewingRecipe<T> extends AbstractFixedResultRecipe<T> {
    public static final Serializer<?> SERIALIZER = new Serializer<>();
    private final Ingredient<T> container;
    private final Ingredient<T> ingredient;

    public CustomBrewingRecipe(@NotNull Key id,
                               boolean showNotification, @NotNull Ingredient<T> ingredient, @NotNull CustomRecipeResult<T> result, @NotNull Ingredient<T> container) {
        super(id, showNotification, result);
        this.container = container;
        this.ingredient = ingredient;
        this.result = result;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean matches(RecipeInput input) {
        BrewingInput<T> brewingInput = (BrewingInput<T>) input;
        return this.container.test(brewingInput.container()) && this.ingredient.test(brewingInput.ingredient());
    }

    @Override
    public List<Ingredient<T>> ingredientsInUse() {
        List<Ingredient<T>> ingredients = new ArrayList<>();
        ingredients.add(this.container);
        ingredients.add(this.ingredient);
        return ingredients;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void takeInput(@NotNull RecipeInput input, int ignore) {
        BrewingInput<T> brewingInput = (BrewingInput<T>) input;
        takeIngredient(this.container, brewingInput.container().item(), ignore);
        takeIngredient(this.ingredient, brewingInput.ingredient().item(), ignore);
    }

    @Override
    public @NotNull Key serializerType() {
        return RecipeSerializers.BREWING;
    }

    @Override
    public RecipeType type() {
        return RecipeType.BREWING;
    }

    @NotNull
    public Ingredient<T> container() {
        return this.container;
    }

    @NotNull
    public Ingredient<T> ingredient() {
        return this.ingredient;
    }

    @SuppressWarnings({"DuplicatedCode"})
    public static class Serializer<A> extends AbstractRecipeSerializer<A, CustomBrewingRecipe<A>> {

        @Override
        public CustomBrewingRecipe<A> readConfig(Key id, ConfigSection section) {
            return new CustomBrewingRecipe<>(
                    id,
                    section.getBoolean(true, "show_notification", "show-notification"),
                    section.getNonNullValue(ConfigConstants.ARGUMENT_LIST, "ingredient", "ingredients").getAsIngredient(),
                    section.getNonNullValue(ConfigConstants.ARGUMENT_SECTION, "result").getAsCustomRecipeResult(),
                    section.getNonNullValue(ConfigConstants.ARGUMENT_LIST, "container").getAsIngredient()
            );
        }

        @Override
        public CustomBrewingRecipe<A> readJson(Key id, JsonObject json) {
            throw new UnsupportedOperationException();
        }
    }
}
