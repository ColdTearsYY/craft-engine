package net.momirealms.craftengine.core.pack.model.definition.rangedisptach;

import com.google.gson.JsonObject;
import net.momirealms.craftengine.core.plugin.config.ConfigSection;

public final class TimeRangeDispatchProperty implements RangeDispatchProperty {
    public static final RangeDispatchPropertyFactory<TimeRangeDispatchProperty> FACTORY = new Factory();
    public static final RangeDispatchPropertyReader<TimeRangeDispatchProperty> READER = new Reader();
    private final String source;
    private final boolean wobble;

    public TimeRangeDispatchProperty(String source, boolean wobble) {
        this.source = source;
        this.wobble = wobble;
    }

    public String source() {
        return this.source;
    }

    public boolean wobble() {
        return this.wobble;
    }

    @Override
    public void accept(JsonObject jsonObject) {
        jsonObject.addProperty("property", "time");
        jsonObject.addProperty("source", this.source);
        if (!this.wobble) {
            jsonObject.addProperty("wobble", false);
        }
    }

    private static class Factory implements RangeDispatchPropertyFactory<TimeRangeDispatchProperty> {
        @Override
        public TimeRangeDispatchProperty create(ConfigSection section) {
            String source = section.getNonNullString("source");
            boolean wobble = section.getBoolean(true, "wobble");
            return new TimeRangeDispatchProperty(source, wobble);
        }
    }

    private static class Reader implements RangeDispatchPropertyReader<TimeRangeDispatchProperty> {
        @Override
        public TimeRangeDispatchProperty read(JsonObject json) {
            String source = json.get("source").getAsString();
            boolean wobble = !json.has("wobble") || json.get("wobble").getAsBoolean();
            return new TimeRangeDispatchProperty(source, wobble);
        }
    }
}
