package net.momirealms.craftengine.core.plugin.config;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.momirealms.craftengine.core.pack.CachedConfigSection;

import java.util.function.Consumer;

public abstract class AbstractConfigParser implements ConfigParser {
    protected final ObjectArrayList<CachedConfigSection> configStorage;
    protected Consumer<ResourceException> errorHandler = (e) -> {};

    public AbstractConfigParser() {
        this.configStorage = new ObjectArrayList<>(64);
    }

    @Override
    public void addConfig(CachedConfigSection section) {
        this.configStorage.add(section);
    }

    @Override
    public void loadAll() {
        Object[] elements = this.configStorage.elements();
        for (int i = 0, size = this.configStorage.size(); i < size; i++) {
            parseSection((CachedConfigSection) elements[i]);
        }
    }

    @Override
    public void clearConfigs() {
        this.configStorage.clear();
    }

    @Override
    public void setErrorHandler(Consumer<ResourceException> errorHandler) {
        this.errorHandler = errorHandler;
    }

    public void error(final ResourceException e) {
        this.errorHandler.accept(e);
    }

    public Consumer<ResourceException> errorHandler() {
        return this.errorHandler;
    }

    protected abstract void parseSection(CachedConfigSection section);
}
