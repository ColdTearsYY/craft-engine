package net.momirealms.craftengine.core.item.recipe.remainder;

import net.momirealms.craftengine.core.plugin.config.ConfigSection;
import net.momirealms.craftengine.core.plugin.config.KnownResourceException;
import net.momirealms.craftengine.core.plugin.context.number.ConstantNumberProvider;
import net.momirealms.craftengine.core.plugin.locale.LocalizedResourceConfigException;
import net.momirealms.craftengine.core.registry.BuiltInRegistries;
import net.momirealms.craftengine.core.registry.Registries;
import net.momirealms.craftengine.core.registry.WritableRegistry;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.MiscUtils;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;
import net.momirealms.craftengine.core.util.ResourceKey;

import java.util.List;
import java.util.Map;

public final class CraftRemainders {
    public static final CraftRemainderType<FixedCraftRemainder> FIXED = register(Key.ce("fixed"), FixedCraftRemainder.FACTORY);
    public static final CraftRemainderType<RecipeBasedCraftRemainder> RECIPE_BASED = register(Key.ce("recipe_based"), RecipeBasedCraftRemainder.FACTORY);
    public static final CraftRemainderType<HurtAndBreakRemainder> HURT_AND_BREAK = register(Key.ce("hurt_and_break"), HurtAndBreakRemainder.FACTORY);

    private CraftRemainders() {}

    public static <T extends CraftRemainder> CraftRemainderType<T> register(Key key, CraftRemainderFactory<T> factory) {
        CraftRemainderType<T> type = new CraftRemainderType<>(key, factory);
        ((WritableRegistry<CraftRemainderType<?>>) BuiltInRegistries.CRAFT_REMAINDER_TYPE)
                .register(ResourceKey.create(Registries.CRAFT_REMAINDER_TYPE.location(), key), type);
        return type;
    }

    public static CraftRemainder fromConfig(ConfigSection section) {
        String type = section.getNonNullString("type");
        Key key = Key.ce(type);
        CraftRemainderType<?> craftRemainderType = BuiltInRegistries.CRAFT_REMAINDER_TYPE.getValue(key);
        if (craftRemainderType == null) {
            throw new KnownResourceException("resource.item.settings.craft_remainder.unknown_type", section.assemblePath("type"), key.asString());
        }
        return craftRemainderType.factory().create(section);
    }
}
