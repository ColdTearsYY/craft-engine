package net.momirealms.craftengine.core.pack.model.definition;

import com.google.gson.JsonObject;
import net.momirealms.craftengine.core.pack.model.definition.condition.ConditionProperties;
import net.momirealms.craftengine.core.pack.model.definition.condition.ConditionProperty;
import net.momirealms.craftengine.core.pack.model.generation.ModelGeneration;
import net.momirealms.craftengine.core.pack.revision.Revision;
import net.momirealms.craftengine.core.plugin.config.ConfigConstants;
import net.momirealms.craftengine.core.plugin.config.ConfigSection;
import net.momirealms.craftengine.core.util.MinecraftVersion;

import java.util.ArrayList;
import java.util.List;

public final class ConditionItemModel implements ItemModel {
    public static final ItemModelFactory<ConditionItemModel> FACTORY = new Factory();
    public static final ItemModelReader<ConditionItemModel> READER = new Reader();
    private final ConditionProperty property;
    private final ItemModel onTrue;
    private final ItemModel onFalse;

    public ConditionItemModel(ConditionProperty property, ItemModel onTrue, ItemModel onFalse) {
        this.property = property;
        this.onTrue = onTrue;
        this.onFalse = onFalse;
    }

    public ConditionProperty property() {
        return this.property;
    }

    public ItemModel onTrue() {
        return this.onTrue;
    }

    public ItemModel onFalse() {
        return this.onFalse;
    }

    @Override
    public List<Revision> revisions() {
        List<Revision> onTrueVersions = this.onTrue.revisions();
        List<Revision> onFalseVersions = this.onFalse.revisions();
        if (onTrueVersions.isEmpty() && onFalseVersions.isEmpty()) return List.of();
        List<Revision> versions = new ArrayList<>(onTrueVersions.size() + onFalseVersions.size());
        versions.addAll(onTrueVersions);
        versions.addAll(onFalseVersions);
        return versions;
    }

    @Override
    public List<ModelGeneration> modelsToGenerate() {
        List<ModelGeneration> onTrueModels = this.onTrue.modelsToGenerate();
        List<ModelGeneration> onFalseModels = this.onFalse.modelsToGenerate();
        if (onTrueModels.isEmpty() && onFalseModels.isEmpty()) return List.of();
        List<ModelGeneration> models = new ArrayList<>(onTrueModels.size() + onFalseModels.size());
        models.addAll(onTrueModels);
        models.addAll(onFalseModels);
        return models;
    }

    @Override
    public JsonObject apply(MinecraftVersion version) {
        JsonObject json = new JsonObject();
        json.addProperty("type", "condition");
        json.add("on_true", this.onTrue.apply(version));
        json.add("on_false", this.onFalse.apply(version));
        this.property.accept(json);
        return json;
    }

    private static class Factory implements ItemModelFactory<ConditionItemModel> {
        private static final String[] ON_TRUE = new String[] {"on_true", "on-true"};
        private static final String[] ON_FALSE = new String[] {"on_false", "on-false"};

        @Override
        public ConditionItemModel create(ConfigSection section) {
            return new ConditionItemModel(
                    ConditionProperties.fromConfig(section),
                    section.getNonNullValue(ON_TRUE, ConfigConstants.ARGUMENT_ITEM_MODEL_DEFINITION, ItemModels::fromConfig),
                    section.getNonNullValue(ON_FALSE, ConfigConstants.ARGUMENT_ITEM_MODEL_DEFINITION, ItemModels::fromConfig)
            );
        }
    }

    private static class Reader implements ItemModelReader<ConditionItemModel> {

        @Override
        public ConditionItemModel read(JsonObject json) {
            return new ConditionItemModel(
                    ConditionProperties.fromJson(json),
                    ItemModels.fromJson(json.getAsJsonObject("on_true")),
                    ItemModels.fromJson(json.getAsJsonObject("on_false"))
            );
        }
    }
}
