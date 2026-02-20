package net.momirealms.craftengine.bukkit.compatibility.model.bettermodel;

import net.momirealms.craftengine.core.entity.furniture.ExternalModel;
import net.momirealms.craftengine.core.plugin.compatibility.ModelProvider;

public final class BetterModelProvider implements ModelProvider {

    @Override
    public String plugin() {
        return "better_model";
    }

    @Override
    public ExternalModel createModel(String id) {
        return new BetterModelModel(id);
    }
}
