package net.momirealms.craftengine.bukkit.compatibility.model.modelengine;

import net.momirealms.craftengine.core.entity.furniture.ExternalModel;
import net.momirealms.craftengine.core.plugin.compatibility.ModelProvider;

public final class ModelEngineProvider implements ModelProvider {

    @Override
    public String plugin() {
        return "model_engine";
    }

    @Override
    public ExternalModel createModel(String id) {
        return new ModelEngineModel(id);
    }
}
