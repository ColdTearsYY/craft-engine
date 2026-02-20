package net.momirealms.craftengine.core.pack;

import net.momirealms.craftengine.core.plugin.config.ConfigSection;

import java.nio.file.Path;

public record CachedConfigSection(Pack pack, Path filePath, ConfigSection config) {
}
