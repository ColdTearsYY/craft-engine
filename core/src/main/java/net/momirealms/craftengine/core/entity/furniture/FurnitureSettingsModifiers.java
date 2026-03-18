package net.momirealms.craftengine.core.entity.furniture;

import net.momirealms.craftengine.core.registry.BuiltInRegistries;
import net.momirealms.craftengine.core.registry.Registries;
import net.momirealms.craftengine.core.registry.WritableRegistry;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.ResourceKey;

public final class FurnitureSettingsModifiers {
    public static final FurnitureSettingsModifierType<FurnitureSettingsModifier> ITEM = register(Key.ce("item"), value -> {
        Key itemId = value.getAsIdentifier();
        return settings -> settings.itemId = itemId;
    });
    public static final FurnitureSettingsModifierType<FurnitureSettingsModifier> SOUNDS = register(Key.ce("sounds"), value -> {
        FurnitureSounds sounds = FurnitureSounds.fromConfig(value.getAsSection());
        return settings -> settings.sounds = sounds;
    });
    public static final FurnitureSettingsModifierType<FurnitureSettingsModifier> HIT_TIMES = register(Key.ce("hit_times"), value -> {
        Key itemId = value.getAsIdentifier();
        return settings -> settings.itemId = itemId;
    });

    private FurnitureSettingsModifiers() {}

    public static void init() {}

    public static <M extends FurnitureSettingsModifier> FurnitureSettingsModifierType<M> register(Key id, FurnitureSettingsModifierFactory<M> factory) {
        FurnitureSettingsModifierType<M> type = new FurnitureSettingsModifierType<>(id, factory);
        ((WritableRegistry<FurnitureSettingsModifierType<? extends FurnitureSettingsModifier>>) BuiltInRegistries.FURNITURE_SETTINGS_TYPE)
                .register(ResourceKey.create(Registries.FURNITURE_SETTINGS_TYPE.location(), id), type);
        return type;
    }
}
