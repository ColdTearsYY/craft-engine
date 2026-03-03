package net.momirealms.craftengine.core.plugin.config;

import net.momirealms.craftengine.core.pack.CachedConfigSection;
import net.momirealms.craftengine.core.pack.Pack;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.plugin.config.template.TemplateManager;
import net.momirealms.craftengine.core.util.GsonHelper;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.MiscUtils;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;

import java.nio.file.Path;
import java.util.Map;

public abstract class IdSectionConfigParser extends IdConfigParser {

    @Override
    protected void parseSection(CachedConfigSection cached) {
        ConfigSection config = cached.config();
        for (Map.Entry<String, Object> entry : config.values().entrySet()) {
            String key = entry.getKey();
            Key id = Key.withDefaultNamespace(key, cached.pack().namespace());
            Path filePath = cached.filePath();
            String currentNode = config.assemblePath(key);
            if (this.checkDuplicated() && isDuplicate(id, filePath, currentNode)) {
                return;
            }
            ResourceConfigUtils.runCatching(
                    filePath,
                    currentNode,
                    () -> {
                        Object value = TemplateManager.INSTANCE.applyTemplates(id, entry.getValue());
                        if (!(value instanceof Map<?, ?> section)) {
                            error(new KnownResourceException(filePath, ConfigConstants.PARSE_SECTION_FAILED, currentNode, value.getClass().getSimpleName()));
                            return;
                        }
                        ConfigSection innerSection = ConfigSection.of(currentNode, MiscUtils.castToMap(section));
                        if (!innerSection.getBoolean("enable", true)) {
                            return;
                        }
                        if (innerSection.getBoolean("debug")) {
                            CraftEngine.instance().logger().info(GsonHelper.get().toJson(value));
                        }
                        parseSection(cached.pack(), filePath, id, innerSection);
                    },
                    super.errorHandler
            );
        }
    }

    protected abstract void parseSection(Pack pack, Path path, Key id, ConfigSection section);
}
