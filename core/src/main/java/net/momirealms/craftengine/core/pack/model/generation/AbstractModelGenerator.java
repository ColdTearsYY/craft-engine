package net.momirealms.craftengine.core.pack.model.generation;

import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.plugin.config.KnownResourceException;
import net.momirealms.craftengine.core.util.Key;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractModelGenerator implements ModelGenerator {
    protected final CraftEngine plugin;
    protected final Map<Key, ModelGeneration> modelsToGenerate = new ConcurrentHashMap<>();

    public AbstractModelGenerator(CraftEngine plugin) {
        this.plugin = plugin;
    }

    @Override
    public Collection<ModelGeneration> modelsToGenerate() {
        return this.modelsToGenerate.values();
    }

    @Override
    public void clearModelsToGenerate() {
        this.modelsToGenerate.clear();
    }

    public void prepareModelGeneration(ModelGeneration model) {
        this.modelsToGenerate.compute(model.path(), (k, conflict) -> {
            if (conflict != null && !conflict.equals(model)) {
                throw new KnownResourceException("resource.model.generation_conflict", model.path().asString());
            }
            return model;
        });
    }
}
