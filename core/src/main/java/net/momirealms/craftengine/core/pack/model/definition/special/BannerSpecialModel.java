package net.momirealms.craftengine.core.pack.model.definition.special;

import com.google.gson.JsonObject;
import net.momirealms.craftengine.core.pack.revision.Revision;
import net.momirealms.craftengine.core.plugin.config.ConfigSection;
import net.momirealms.craftengine.core.util.MinecraftVersion;

import java.util.List;

public final class BannerSpecialModel implements SpecialModel {
    public static final SpecialModelFactory<BannerSpecialModel> FACTORY = new Factory();
    public static final SpecialModelReader<BannerSpecialModel> READER = new Reader();
    private final String color;

    public BannerSpecialModel(String color) {
        this.color = color;
    }

    public String color() {
        return this.color;
    }

    @Override
    public List<Revision> revisions() {
        return List.of();
    }

    @Override
    public JsonObject apply(MinecraftVersion version) {
        JsonObject json = new JsonObject();
        json.addProperty("type", "banner");
        json.addProperty("color", this.color);
        return json;
    }

    private static class Factory implements SpecialModelFactory<BannerSpecialModel> {
        @Override
        public BannerSpecialModel create(ConfigSection section) {
            String color = section.getNonNullString("color");
            return new BannerSpecialModel(color);
        }
    }

    private static class Reader implements SpecialModelReader<BannerSpecialModel> {
        @Override
        public BannerSpecialModel read(JsonObject json) {
            String color = json.get("color").getAsString();
            return new BannerSpecialModel(color);
        }
    }
}
