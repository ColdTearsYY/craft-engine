package net.momirealms.craftengine.core.item;

import net.momirealms.craftengine.core.plugin.config.ConfigValue;

public interface ItemSettingsModifierFactory<M extends ItemSettingsModifier> {

    M create(ConfigValue value);
}
