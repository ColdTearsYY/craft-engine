package net.momirealms.craftengine.core.entity.furniture;

import net.momirealms.craftengine.core.plugin.config.ConfigSection;
import net.momirealms.craftengine.core.plugin.config.ConfigValue;
import net.momirealms.craftengine.core.registry.BuiltInRegistries;
import net.momirealms.craftengine.core.util.CustomDataType;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Optional;

public final class FurnitureSettings {
    FurnitureSounds sounds = FurnitureSounds.EMPTY;
    @Nullable
    Key itemId;
    Map<CustomDataType<?>, Object> customData = new IdentityHashMap<>(4);
    int hitTimes;

    private FurnitureSettings() {}

    public static FurnitureSettings of() {
        return new FurnitureSettings();
    }

    public static FurnitureSettings fromConfig(ConfigSection section) {
        return applyModifiers(FurnitureSettings.of(), section);
    }

    public static FurnitureSettings applyModifiers(FurnitureSettings settings, ConfigSection section) {
        if (section != null) {
            for (String type : section.keySet()) {
                ConfigValue value = section.getValue(type);
                if (value == null) continue;
                String key = StringUtils.normalizeSettingsType(type);
                Optional.ofNullable(BuiltInRegistries.FURNITURE_SETTINGS_TYPE.getValue(Key.ce(key)))
                        .ifPresent(modifierType ->
                                modifierType.factory().create(value).apply(settings));
            }
        }
        return settings;
    }

    public static FurnitureSettings ofFullCopy(FurnitureSettings settings) {
        FurnitureSettings newSettings = of();
        newSettings.sounds = settings.sounds;
        newSettings.itemId = settings.itemId;
        newSettings.hitTimes = settings.hitTimes;
        newSettings.customData = new IdentityHashMap<>(settings.customData);
        return newSettings;
    }

    @SuppressWarnings("unchecked")
    public <T> T getCustomData(CustomDataType<T> type) {
        return (T) this.customData.get(type);
    }

    public void clearCustomData() {
        this.customData.clear();
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public <T> T removeCustomData(CustomDataType<?> type) {
        return (T) this.customData.remove(type);
    }

    public <T> void addCustomData(CustomDataType<T> key, T value) {
        this.customData.put(key, value);
    }

    public FurnitureSounds sounds() {
        return this.sounds;
    }

    @Nullable
    public Key itemId() {
        return this.itemId;
    }

    public int hitTimes() {
        return this.hitTimes;
    }

    public FurnitureSettings sounds(FurnitureSounds sounds) {
        this.sounds = sounds;
        return this;
    }

    public FurnitureSettings itemId(Key itemId) {
        this.itemId = itemId;
        return this;
    }

    public FurnitureSettings hitTimes(int hitTimes) {
        this.hitTimes = hitTimes;
        return this;
    }
}
