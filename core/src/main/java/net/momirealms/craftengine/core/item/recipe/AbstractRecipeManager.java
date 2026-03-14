package net.momirealms.craftengine.core.item.recipe;

import net.momirealms.craftengine.core.item.recipe.input.RecipeInput;
import net.momirealms.craftengine.core.pack.Pack;
import net.momirealms.craftengine.core.plugin.config.Config;
import net.momirealms.craftengine.core.plugin.config.ConfigParser;
import net.momirealms.craftengine.core.plugin.config.ConfigSection;
import net.momirealms.craftengine.core.plugin.config.IdSectionConfigParser;
import net.momirealms.craftengine.core.plugin.config.lifecycle.LoadingStage;
import net.momirealms.craftengine.core.plugin.config.lifecycle.LoadingStages;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.UniqueKey;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.*;

public abstract class AbstractRecipeManager<T> implements RecipeManager<T> {
    protected final Map<RecipeType, List<Recipe<T>>> byType = new EnumMap<>(RecipeType.class);
    protected final Map<Key, Recipe<T>> byId = new LinkedHashMap<>();
    protected final Map<Key, List<Recipe<T>>> byResult = new HashMap<>();
    protected final Map<Key, List<Recipe<T>>> byIngredient = new HashMap<>();
    protected final Map<Key, List<IngredientUnlockable>> ingredientUnlockable = new HashMap<>();
    protected final Set<Key> dataPackRecipes = new HashSet<>();
    protected final ConfigParser recipeParser;

    public AbstractRecipeManager() {
        this.recipeParser = new RecipeParser();
    }

    @Override
    public ConfigParser parser() {
        return this.recipeParser;
    }

    @Override
    public void unload() {
        this.dataPackRecipes.clear();
        this.byType.clear();
        this.byId.clear();
        this.byResult.clear();
        this.byIngredient.clear();
        this.ingredientUnlockable.clear();
    }

    protected void markAsDataPackRecipe(Key key) {
        this.dataPackRecipes.add(key);
    }

    @Override
    public boolean isDataPackRecipe(Key key) {
        return this.dataPackRecipes.contains(key);
    }

    @Override
    public boolean isCustomRecipe(Key key) {
        return this.byId.containsKey(key);
    }

    @Override
    public Optional<Recipe<T>> recipeById(Key key) {
        return Optional.ofNullable(this.byId.get(key));
    }

    @Override
    public List<Recipe<T>> recipesByType(RecipeType type) {
        return this.byType.getOrDefault(type, List.of());
    }

    @Override
    public List<Recipe<T>> recipeByResult(Key result) {
        return this.byResult.getOrDefault(result, List.of());
    }

    @Override
    public List<Recipe<T>> recipeByIngredient(Key ingredient) {
        return this.byIngredient.getOrDefault(ingredient, List.of());
    }

    @Nullable
    @Override
    public Recipe<T> recipeByInput(RecipeType type, RecipeInput input) {
        List<Recipe<T>> recipes = this.byType.get(type);
        if (recipes == null) return null;
        for (Recipe<T> recipe : recipes) {
            if (recipe.matches(input)) {
                return recipe;
            }
        }
        return null;
    }

    @Nullable
    @Override
    public Recipe<T> recipeByInput(RecipeType type, RecipeInput input, Key lastRecipe) {
        if (lastRecipe != null) {
            Recipe<T> last = this.byId.get(lastRecipe);
            if (last != null && last.matches(input)) {
                return last;
            }
        }
        return recipeByInput(type, input);
    }

    public List<IngredientUnlockable> ingredientUnlockablesByChangedItem(Key item) {
        return this.ingredientUnlockable.getOrDefault(item, List.of());
    }

    protected boolean registerInternalRecipe(Recipe<T> recipe, boolean unlockOnIngredientObtained) {
        if (this.byId.containsKey(recipe.id())) return false;
        this.byType.computeIfAbsent(recipe.type(), k -> new ArrayList<>()).add(recipe);
        this.byId.put(recipe.id(), recipe);
        if (recipe instanceof AbstractFixedResultRecipe<?> fixedResult) {
            this.byResult.computeIfAbsent(fixedResult.result().item().id(), k -> new ArrayList<>()).add(recipe);
        }
        List<Ingredient<T>> ingredients = recipe.ingredientsInUse();
        if (recipe.canBeSearchedByIngredients()) {
            HashSet<Key> usedKeys = new HashSet<>();
            for (Ingredient<T> ingredient : ingredients) {
                for (UniqueKey holder : ingredient.items()) {
                    Key key = holder.key();
                    if (usedKeys.add(key)) {
                        this.byIngredient.computeIfAbsent(key, k -> new ArrayList<>()).add(recipe);
                    }
                }
            }
        }
        if (unlockOnIngredientObtained) {
            List<IngredientUnlockable.Requirement> requirements =  new ArrayList<>(4);
            HashSet<UniqueKey> usedKeys = new HashSet<>();
            for (Ingredient<T> ingredient : ingredients) {
                List<UniqueKey> items = ingredient.items();
                if (items.size() > 1) {
                    requirements.add(new IngredientUnlockable.Multiple(items.toArray(new UniqueKey[0])));
                } else if (!items.isEmpty()) {
                    requirements.add(new IngredientUnlockable.Single(items.getFirst()));
                }
                usedKeys.addAll(items);
            }
            IngredientUnlockable unlockable = new IngredientUnlockable(recipe, requirements.toArray(new IngredientUnlockable.Requirement[0]));
            for (UniqueKey usedKey : usedKeys) {
                this.ingredientUnlockable.computeIfAbsent(usedKey.key(), l -> new ArrayList<>()).add(unlockable);
            }
        }
        return true;
    }

    private final class RecipeParser extends IdSectionConfigParser {
        public static final String[] CONFIG_SECTION_NAME = new String[] {"recipes", "recipe"};
        private final List<TempRecipe<T>> recipes = Collections.synchronizedList(new ArrayList<>());

        @Override
        public String[] sectionId() {
            return CONFIG_SECTION_NAME;
        }

        @Override
        public int count() {
            return Math.max(0, AbstractRecipeManager.this.byId.size() - AbstractRecipeManager.this.dataPackRecipes.size());
        }

        @Override
        public boolean async() {
            return true;
        }

        @Override
        public LoadingStage loadingStage() {
            return LoadingStages.RECIPE;
        }

        @Override
        public void preProcess() {
            if (!this.recipes.isEmpty()) {
                this.recipes.clear();
            }
        }

        @Override
        public void postProcess() {
            if (!this.recipes.isEmpty()) {
                for (TempRecipe<T> recipe : this.recipes) {
                    registerInternalRecipe(recipe.recipe, recipe.unlockOnIngredientObtained);
                }
                this.recipes.clear();
            }
        }

        @Override
        public List<LoadingStage> dependencies() {
            return List.of(LoadingStages.TEMPLATE, LoadingStages.ITEM);
        }

        private static final String[] UNLOCK_ON_INGREDIENT_OBTAINED = new String[] {"unlock_on_ingredient_obtained", "unlock-on-ingredient-obtained"};

        @Override
        public void parseSection(Pack pack, Path path, Key id, ConfigSection section) {
            if (!Config.enableRecipeSystem()) return;
            boolean unlockOnIngredientObtained = section.getBoolean(UNLOCK_ON_INGREDIENT_OBTAINED, Config.unlockOnIngredientObtained());
            Recipe<T> recipe = RecipeSerializers.fromConfig(id, section);
            this.recipes.add(new TempRecipe<>(recipe, unlockOnIngredientObtained));
        }

        private record TempRecipe<T>(Recipe<T> recipe, boolean unlockOnIngredientObtained) {}
    }

    protected abstract void unregisterPlatformRecipeMainThread(Key key, boolean isBrewingRecipe);

    protected abstract void registerPlatformRecipeMainThread(Recipe<T> recipe);
}
