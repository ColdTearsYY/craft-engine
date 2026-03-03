package net.momirealms.craftengine.core.pack.model.legacy;

import net.momirealms.craftengine.core.pack.model.generation.ModelGeneration;
import net.momirealms.craftengine.core.plugin.config.ConfigSection;
import net.momirealms.craftengine.core.plugin.config.ConfigValue;
import net.momirealms.craftengine.core.util.Key;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public final class LegacyItemModel {
    private final List<ModelGeneration> modelsToGenerate;
    private final Key path;
    private final List<LegacyOverridesModel> overrides;

    public LegacyItemModel(Key path, List<LegacyOverridesModel> overrides, List<ModelGeneration> modelsToGenerate) {
        this.modelsToGenerate = modelsToGenerate;
        this.path = path;
        this.overrides = overrides;
    }

    public List<ModelGeneration> modelsToGenerate() {
        return this.modelsToGenerate;
    }

    public List<LegacyOverridesModel> overrides() {
        return this.overrides;
    }

    public Key path() {
        return this.path;
    }

    private static final String[] PATH = new String[] {"path", "model"};

    public static LegacyItemModel fromConfig(ConfigSection section, int customModelData) {
        Key legacyModelPath = section.getNonNullAssetPath(PATH);
        ConfigSection generationSection = section.getSection("generation");
        ModelGeneration baseModelGeneration = null;
        if (generationSection != null) {
            baseModelGeneration = ModelGeneration.of(legacyModelPath, generationSection);
        }
        ConfigValue overridesValue = section.getValue("overrides");
        if (overridesValue != null) {
            List<ModelGeneration> modelGenerations = new ArrayList<>();
            List<LegacyOverridesModel> legacyOverridesModels = new ArrayList<>();
            if (baseModelGeneration != null) modelGenerations.add(baseModelGeneration);
            legacyOverridesModels.add(new LegacyOverridesModel(new HashMap<>(), legacyModelPath, customModelData));
            overridesValue.forEach(v -> {
                ConfigSection overrideSection = v.getAsSection();
                Key overrideModelPath = overrideSection.getNonNullAssetPath(PATH);
                ConfigSection predicateSection = overrideSection.getNonNullSection("predicate");
                ConfigSection overrideGenerationSection = overrideSection.getSection("generation");
                if (overrideGenerationSection != null) {
                    modelGenerations.add(ModelGeneration.of(overrideModelPath, overrideGenerationSection));
                }
                legacyOverridesModels.add(new LegacyOverridesModel(predicateSection.values(), overrideModelPath, customModelData));
            });
            return new LegacyItemModel(legacyModelPath, legacyOverridesModels, modelGenerations);
        } else {
            return new LegacyItemModel(
                    legacyModelPath,
                    List.of(new LegacyOverridesModel(null, legacyModelPath, customModelData)),
                    baseModelGeneration == null ? List.of() : List.of(baseModelGeneration)
            );
        }
    }
}
