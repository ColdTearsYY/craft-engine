package net.momirealms.craftengine.core.pack.model.definition;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.momirealms.craftengine.core.pack.model.definition.tint.Tint;
import net.momirealms.craftengine.core.pack.model.definition.tint.Tints;
import net.momirealms.craftengine.core.pack.model.generation.ModelGeneration;
import net.momirealms.craftengine.core.pack.revision.Revision;
import net.momirealms.craftengine.core.plugin.config.ConfigSection;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.MinecraftVersion;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class BaseItemModel implements ItemModel {
    public static final ItemModelFactory<BaseItemModel> FACTORY = new Factory();
    public static final ItemModelReader<BaseItemModel> READER = new Reader();
    private final Key path;
    private final List<Tint> tints;
    private final ModelGeneration modelGeneration;

    public BaseItemModel(@NotNull Key path,
                         @NotNull List<Tint> tints,
                         @Nullable ModelGeneration modelGeneration) {
        this.path = path;
        this.tints = tints;
        this.modelGeneration = modelGeneration;
    }

    @Nullable
    public ModelGeneration modelGeneration() {
        return this.modelGeneration;
    }

    @NotNull
    public List<Tint> tints() {
        return this.tints;
    }

    public Key path() {
        return this.path;
    }

    @Override
    public JsonObject apply(MinecraftVersion version) {
        JsonObject json = new JsonObject();
        json.addProperty("type", "model");
        json.addProperty("model", this.path.asMinimalString());
        if (!this.tints.isEmpty()) {
            JsonArray array = new JsonArray();
            for (Tint tint : this.tints) {
                array.add(tint.get());
            }
            json.add("tints", array);
        }
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
        return List.of();
    }

    private static class Factory implements ItemModelFactory<BaseItemModel> {

        @Override
        public BaseItemModel create(ConfigSection section) {
            Key modelPath = section.getNonNullIdentifier("path", "model");
            ConfigSection generation = section.getSection("generation");
            ModelGeneration modelGeneration = null;
            if (generation != null) {
                modelGeneration = ModelGeneration.of(modelPath, generation);
            }
            return new BaseItemModel(
                    modelPath,
                    section.parseSectionList(Tints::fromConfig, "tints"),
                    modelGeneration
            );
        }
    }

    private static class Reader implements ItemModelReader<BaseItemModel> {

        @Override
        public BaseItemModel read(JsonObject json) {
            String model = json.get("model").getAsString();
            List<Tint> tints;
            if (json.has("tints")) {
                JsonArray array = json.getAsJsonArray("tints");
                tints = new ArrayList<>(array.size());
                for (JsonElement element : array) {
                    if (element instanceof JsonObject jo) {
                        tints.add(Tints.fromJson(jo));
                    } else {
                        throw new IllegalArgumentException("tint is expected to be a json object");
                    }
                }
            } else {
                tints = Collections.emptyList();
            }
            return new BaseItemModel(Key.of(model), tints, null);
        }
    }
}
