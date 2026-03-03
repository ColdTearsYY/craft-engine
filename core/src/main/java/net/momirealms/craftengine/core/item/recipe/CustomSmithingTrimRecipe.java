package net.momirealms.craftengine.core.item.recipe;

import com.google.gson.JsonObject;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.item.ItemBuildContext;
import net.momirealms.craftengine.core.item.recipe.input.RecipeInput;
import net.momirealms.craftengine.core.item.recipe.input.SmithingInput;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.plugin.config.ConfigConstants;
import net.momirealms.craftengine.core.plugin.config.ConfigSection;
import net.momirealms.craftengine.core.plugin.context.CommonConditions;
import net.momirealms.craftengine.core.plugin.context.CommonFunctions;
import net.momirealms.craftengine.core.plugin.context.Context;
import net.momirealms.craftengine.core.plugin.context.function.Function;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.MiscUtils;
import net.momirealms.craftengine.core.util.VersionHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

public final class CustomSmithingTrimRecipe<T> extends AbstractRecipe<T>
        implements ConditionalRecipe<T>, FunctionalRecipe<T> {
    public static final Serializer<?> SERIALIZER = new Serializer<>();
    private final Ingredient<T> base;
    private final Ingredient<T> template;
    private final Ingredient<T> addition;
    @Nullable // 1.21.5
    private final Key pattern;
    @Nullable
    private final Predicate<Context> condition;
    private final Function<Context>[] smithingFunctions;

    public CustomSmithingTrimRecipe(@NotNull Key id,
                                    boolean showNotification,
                                    @NotNull Ingredient<T> template,
                                    @NotNull Ingredient<T> base,
                                    @NotNull Ingredient<T> addition,
                                    @Nullable Key pattern,
                                    @Nullable Function<Context>[] smithingFunctions,
                                    @Nullable Predicate<Context> condition
    ) {
        super(id, showNotification);
        this.base = base;
        this.template = template;
        this.addition = addition;
        this.pattern = pattern;
        this.condition = condition;
        this.smithingFunctions = smithingFunctions;
        if (pattern == null && VersionHelper.isOrAbove1_21_5()) {
            throw new IllegalStateException("SmithingTrimRecipe cannot have a null pattern on 1.21.5 and above.");
        }
    }

    @Override
    public Function<Context>[] functions() {
        return this.smithingFunctions;
    }

    @Override
    public boolean canUse(Context context) {
        if (this.condition != null) return this.condition.test(context);
        return true;
    }

    @Override
    public boolean hasCondition() {
        return this.condition != null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void takeInput(@NotNull RecipeInput input, int ignore) {
        SmithingInput<T> smithingInput = (SmithingInput<T>) input;
        super.takeIngredient(this.base, smithingInput.base().item(), ignore);
        super.takeIngredient(this.template, smithingInput.template().item(), ignore);
        super.takeIngredient(this.addition, smithingInput.addition().item(), ignore);
    }

    @SuppressWarnings("unchecked")
    @Override
    public T assemble(RecipeInput input, ItemBuildContext context) {
        SmithingInput<T> smithingInput = (SmithingInput<T>) input;
        Item<T> processed = (Item<T>) CraftEngine.instance().itemManager().applyTrim((Item<Object>) smithingInput.base().item(), (Item<Object>) smithingInput.addition().item(), (Item<Object>) smithingInput.template().item(), this.pattern);
        return processed.getItem();
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean matches(RecipeInput input) {
        SmithingInput<T> smithingInput = (SmithingInput<T>) input;
        return checkIngredient(this.base, smithingInput.base())
                && checkIngredient(this.template, smithingInput.template())
                && checkIngredient(this.addition, smithingInput.addition());
    }

    private boolean checkIngredient(Ingredient<T> ingredient, UniqueIdItem<T> item) {
        return ingredient.test(item);
    }

    @Override
    public List<Ingredient<T>> ingredientsInUse() {
        List<Ingredient<T>> ingredients = new ArrayList<>();
        ingredients.add(this.base);
        ingredients.add(this.template);
        ingredients.add(this.addition);
        return ingredients;
    }

    @Override
    public @NotNull Key serializerType() {
        return RecipeSerializers.SMITHING_TRIM;
    }

    @Override
    public RecipeType type() {
        return RecipeType.SMITHING;
    }

    @NotNull
    public Ingredient<T> base() {
        return this.base;
    }

    @NotNull
    public Ingredient<T> template() {
        return template;
    }

    @NotNull
    public Ingredient<T> addition() {
        return addition;
    }

    @Nullable
    public Key pattern() {
        return pattern;
    }

    @Override
    public boolean canBeSearchedByIngredients() {
        return false;
    }

    @SuppressWarnings({"DuplicatedCode"})
    public static class Serializer<A> extends AbstractRecipeSerializer<A, CustomSmithingTrimRecipe<A>> {
        private static final String[] TEMPLATE_TYPE = new String[]{"template_type", "template-type"};

        @SuppressWarnings("unchecked")
        @Override
        public CustomSmithingTrimRecipe<A> readConfig(Key id, ConfigSection section) {
            return new CustomSmithingTrimRecipe<>(id,
                    section.getBoolean(SHOW_NOTIFICATIONS, true),
                    section.getNonNullValue(TEMPLATE_TYPE, ConfigConstants.ARGUMENT_LIST, super::parseIngredient),
                    section.getNonNullValue("base", ConfigConstants.ARGUMENT_LIST, super::parseIngredient),
                    section.getNonNullValue("addition", ConfigConstants.ARGUMENT_LIST, super::parseIngredient),
                    VersionHelper.isOrAbove1_21_5() ? section.getNonNullIdentifier("pattern") : null,
                    section.getList(FUNCTIONS, CommonFunctions::fromConfig).toArray(new Function[0]),
                    MiscUtils.allOf(section.getList(CONDITIONS, CommonConditions::fromConfig))
            );
        }

        @Override
        public CustomSmithingTrimRecipe<A> readJson(Key id, JsonObject json) {
            return new CustomSmithingTrimRecipe<>(id,
                    VANILLA_RECIPE_HELPER.showNotification(json),
                    Objects.requireNonNull(toIngredient(VANILLA_RECIPE_HELPER.singleIngredient(json.get("template")))),
                    Objects.requireNonNull(toIngredient(VANILLA_RECIPE_HELPER.singleIngredient(json.get("base")))),
                    Objects.requireNonNull(toIngredient(VANILLA_RECIPE_HELPER.singleIngredient(json.get("addition")))),
                    VersionHelper.isOrAbove1_21_5() ? Key.of(json.get("pattern").getAsString()) : null,
                    null,
                    null
            );
        }
    }
}
