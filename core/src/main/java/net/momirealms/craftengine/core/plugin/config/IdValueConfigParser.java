package net.momirealms.craftengine.core.plugin.config;

import net.momirealms.craftengine.core.pack.CachedConfigSection;
import net.momirealms.craftengine.core.pack.Pack;
import net.momirealms.craftengine.core.plugin.config.template.TemplateManager;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;

import java.nio.file.Path;
import java.util.Map;

public abstract class IdValueConfigParser extends IdConfigParser {

    @Override
    protected void parseSection(CachedConfigSection cached) {
        ConfigSection config = cached.config();
        Path path = cached.filePath();
        for (Map.Entry<String, Object> entry : config.values().entrySet()) {
            String key = entry.getKey();
            Path filePath = cached.filePath();
            Key id = Key.withDefaultNamespace(key, cached.pack().namespace());
            String currentNode = config.assemblePath(key);
            if (this.checkDuplicated() && isDuplicate(id, filePath, currentNode)) {
                return;
            }
            ResourceConfigUtils.runCatching(
                    path,
                    currentNode,
                    () -> parseValue(cached.pack(), filePath, id, new ConfigValue(currentNode, TemplateManager.INSTANCE.applyTemplates(id, entry.getValue()))),
                    super.errorHandler
            );
        }
    }

    protected abstract void parseValue(Pack pack, Path filePath, Key id, ConfigValue value);
}
