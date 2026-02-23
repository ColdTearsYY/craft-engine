package net.momirealms.craftengine.core.pack;

import net.momirealms.craftengine.core.plugin.config.ConfigSection;
import net.momirealms.craftengine.core.util.Key;

import java.nio.file.Path;

public record PendingConfigSection(Pack pack, Path path, Key id, ConfigSection section) {
}
