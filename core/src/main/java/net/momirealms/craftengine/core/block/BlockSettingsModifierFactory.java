package net.momirealms.craftengine.core.block;

import net.momirealms.craftengine.core.plugin.config.ConfigValue;

public interface BlockSettingsModifierFactory<M extends BlockSettingsModifier> {

    M create(ConfigValue value);
}
