package net.momirealms.craftengine.core.entity.furniture;

import net.momirealms.craftengine.core.plugin.config.ConfigValue;

public interface FurnitureSettingsModifierFactory<M extends FurnitureSettingsModifier> {

    M createModifier(ConfigValue value);
}
