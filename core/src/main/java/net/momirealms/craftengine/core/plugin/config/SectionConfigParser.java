package net.momirealms.craftengine.core.plugin.config;

import net.momirealms.craftengine.core.pack.CachedConfigSection;
import net.momirealms.craftengine.core.pack.Pack;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;

import java.nio.file.Path;

public abstract class SectionConfigParser extends AbstractConfigParser {

    @Override
    protected void parseSection(CachedConfigSection cached) {
        Path path = cached.filePath();
        ConfigSection config = cached.config();
        ResourceConfigUtils.runCatching(
                path,
                config.path(),
                () -> parseSection(cached.pack(), path, cached.config()),
                super.errorHandler
        );
    }

    protected abstract void parseSection(Pack pack, Path path, ConfigSection section);
}
