package net.momirealms.craftengine.core.pack.model.definition;

import com.google.gson.JsonObject;
import net.momirealms.craftengine.core.pack.Identifier;
import net.momirealms.craftengine.core.pack.model.definition.special.SpecialModel;
import net.momirealms.craftengine.core.pack.model.definition.special.SpecialModels;
import net.momirealms.craftengine.core.pack.model.generation.ModelGeneration;
import net.momirealms.craftengine.core.pack.revision.Revision;
import net.momirealms.craftengine.core.plugin.config.ConfigSection;
import net.momirealms.craftengine.core.plugin.locale.LocalizedResourceConfigException;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.MinecraftVersion;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class SpecialItemModel implements ItemModel {
    public static final ItemModelFactory<SpecialItemModel> FACTORY = new Factory();
    public static final ItemModelReader<SpecialItemModel> READER = new Reader();
    private final SpecialModel specialModel;
    private final Key base;
    private final ModelGeneration modelGeneration;

    public SpecialItemModel(SpecialModel specialModel, Key base, @Nullable ModelGeneration generation) {
        this.specialModel = specialModel;
        this.base = base;
        this.modelGeneration = generation;
    }

    public SpecialModel specialModel() {
        return this.specialModel;
    }

    @Nullable
    public ModelGeneration modelGeneration() {
        return this.modelGeneration;
    }

    public Key base() {
        return this.base;
    }

    @Override
    public JsonObject apply(MinecraftVersion version) {
        JsonObject json = new JsonObject();
        json.addProperty("type", "special");
        json.add("model", this.specialModel.apply(version));
        json.addProperty("base", this.base.asMinimalString());
        return json;
    }

    @Override
    public List<ModelGeneration> modelsToGenerate() {
        if (this.modelGeneration == null) {
            return List.of();
        } else {
            return List.of(this.modelGeneration);
        }
    }

    @Override
    public List<Revision> revisions() {
        return this.specialModel.revisions();
    }

    private static class Factory implements ItemModelFactory<SpecialItemModel> {

        @Override
        public SpecialItemModel create(ConfigSection section) {
            Key base = section.getNonNullIdentifier("base", "path");
            ConfigSection generation = section.getSection("generation");
            ModelGeneration modelGeneration = null;
            if (generation != null) {
                modelGeneration = ModelGeneration.of(base, generation);
            }
            return new SpecialItemModel(SpecialModels.fromMap(section.getNonNullSection("model")), base, modelGeneration);
        }
    }

    private static class Reader implements ItemModelReader<SpecialItemModel> {

        @Override
        public SpecialItemModel read(JsonObject json) {
            return new SpecialItemModel(
                    SpecialModels.fromJson(json.getAsJsonObject("model")),
                    Key.of(json.get("base").getAsString()),
                    null
            );
        }
    }
}
