package net.momirealms.craftengine.core.pack.model.definition;

import com.google.gson.JsonObject;
import net.momirealms.craftengine.core.pack.model.generation.ModelGenerationHolder;
import net.momirealms.craftengine.core.pack.revision.Revision;
import net.momirealms.craftengine.core.util.MinecraftVersion;

import java.util.function.Consumer;
import java.util.function.Function;

public interface ItemModel extends Function<MinecraftVersion, JsonObject> {

    void collectRevision(Consumer<Revision> consumer);

    void prepareModelGeneration(Consumer<ModelGenerationHolder> consumer);
}
